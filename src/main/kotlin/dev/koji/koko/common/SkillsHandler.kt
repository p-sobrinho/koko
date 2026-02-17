package dev.koji.koko.common

import com.mojang.logging.LogUtils
import dev.koji.koko.common.attachments.PlayerSkills
import dev.koji.koko.common.models.SkillData
import dev.koji.koko.common.models.SkillModel
import dev.koji.koko.common.models.effects.AbstractSkillEffect
import dev.koji.koko.common.models.effects.AbstractSkillEffectFilter
import dev.koji.koko.common.models.sources.AbstractSkillSource
import dev.koji.koko.common.network.payloads.SyncSkillPayload
import dev.koji.koko.common.network.payloads.SyncSkillsPayload
import dev.koji.koko.common.registry.AttachmentsRegistry
import dev.koji.koko.common.registry.DatapackRegistry
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.Level
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.common.EventBusSubscriber
import net.neoforged.neoforge.event.entity.player.PlayerEvent
import net.neoforged.neoforge.network.PacketDistributor


@EventBusSubscriber
object SkillsHandler {
    private val LOGGER = LogUtils.getLogger()

    fun safeParseResource(resource: String): ResourceLocation {
        return if (resource.contains(":")) ResourceLocation.parse(resource.removePrefix("#"))
        else ResourceLocation.fromNamespaceAndPath("minecraft", resource)
    }

    fun syncNewSkills(level: Level, playerSkills: PlayerSkills) {
        val skillModels = getSkillsModels(level)

        for (entry in skillModels) {
            playerSkills.putIfAbsent(entry.key.location(), SkillData(entry.value.defaultXp, false))
        }
    }

    fun syncEffects(player: Player) {
        if (player.level().isClientSide) return

        val pastAppliedEffects = this.getPastEffectsForPlayer(player)
        val toApplyEffects = this.getEffectsForPlayer(player)

        for (applier in pastAppliedEffects.filter { !toApplyEffects.contains(it) })
            applier.sourceData.unApply(applier, player)

        for (applier in toApplyEffects) applier.sourceData.apply(applier, player)
    }

    fun updateXp(player: Player, skill: ResourceLocation, amount: Double) {
        if (player.level().isClientSide) return;

        val playerSkills = player.getData(AttachmentsRegistry.PLAYER_SKILLS)

        LOGGER.info("Updating {} xp from skill {} to {}.", player.name, skill, amount)

        val playerSkill = playerSkills.getSkill(skill)
        val skillModel = this.getSkillModel(player.level(), skill)
        val maxLevel = if (playerSkill.isOverClocked) skillModel.overClockedMaxLevel else skillModel.maxLevel
        val maxXp = xpToLevelUp(maxLevel).toDouble()

        if (this.getLevel(player, skill) == maxLevel) {
            playerSkill.xp = maxXp
        } else {
            playerSkill.xp += maxXp.coerceAtMost(playerSkill.xp)
        }

        PacketDistributor.sendToPlayer(
            player as ServerPlayer, SyncSkillPayload(skill, playerSkill)
        )

        this.syncEffects(player)
    }

    fun replaceSkill(player: Player, skill: ResourceLocation, data: SkillData) {
        val playerSkills = this.getSkills(player)

        playerSkills.put(skill, data)
    }

    fun replaceSkills(player: Player, skills: Map<ResourceLocation, SkillData>) {
        val playerSkills = this.getSkills(player)

        playerSkills.replace(skills)
    }

    fun getSkill(player: Player, skill: ResourceLocation) = this.getSkills(player).getSkill(skill)

    fun getSkills(player: Player): PlayerSkills = player.getData(AttachmentsRegistry.PLAYER_SKILLS)

    fun getSkillModel(level: Level, skill: ResourceLocation): SkillModel {
        val registry = level.registryAccess().registryOrThrow(DatapackRegistry.SKILL_REGISTRY);

        return registry.get(skill) ?: throw RuntimeException("Unable to find skill model for $skill.")
    }

    fun getSkillModel(player: Player, skill: ResourceLocation): SkillModel {
        val registry = player.registryAccess().registryOrThrow(DatapackRegistry.SKILL_REGISTRY);

        return registry.get(skill) ?: throw RuntimeException("Unable to find skill model for $skill.")
    }

    fun getSkillsModels(level: Level): Set<Map.Entry<ResourceKey<SkillModel>, SkillModel>> {
        val registry = level.registryAccess().registryOrThrow(DatapackRegistry.SKILL_REGISTRY);

        return registry.entrySet()
    }

    fun getSkillsModels(player: Player): Set<Map.Entry<ResourceKey<SkillModel>, SkillModel>> {
        val registry = player.registryAccess().registryOrThrow(DatapackRegistry.SKILL_REGISTRY);

        return registry.entrySet()
    }

    fun getListenersFor(skillSourceType: String, level: Level): Set<SkillModelSourceListener> {
        val skillsModels = getSkillsModels(level).asSequence()
            .flatMap { (key, model) ->
                model.skillSources.asSequence()
                    .filter { it.type == skillSourceType }
                    .map { SkillModelSourceListener(key.location(), it) }
            }.toSet()


        return skillsModels
    }

    fun getLevel(player: Player, skill: ResourceLocation): Int {
        val skills = this.getSkills(player)
        val skillData = skills.getSkill(skill)
        val skillModel = this.getSkillModel(player.level(), skill)

        val xp = skillData.xp
        val maxLevel = if (skillData.isOverClocked) skillModel.overClockedMaxLevel else skillModel.maxLevel
        var total: Int

        for (level in 1..maxLevel) {
            total = this.xpToLevelUp(level)

            if (xp <= total) {
                return level
            }
        }

        return maxLevel
    }

    fun getEffectsForPlayer(player: Player): Set<SkillEffectApplier> {
        val skillsModels = this.getSkillsModels(player)

        return skillsModels.mapNotNull { (location, model) ->
            val playerLevel = this.getLevel(player, location.location())

            model.effects.mapNotNull { effect ->
                effect.doAnyApplies(playerLevel)?.let { applies ->
                    SkillEffectApplier(location.location(), effect, applies)
                }
            }.takeIf { it.isNotEmpty() }
        }.flatten().toSet()
    }

    fun getPastEffectsForPlayer(player: Player): Set<SkillEffectApplier> {
        val skillsModels = this.getSkillsModels(player)

        return skillsModels.mapNotNull { (location, model) ->
            val playerLevel = this.getLevel(player, location.location())

            model.effects.mapNotNull { effect ->
                effect.doAnyApplies((playerLevel - 1).coerceAtMost(0))?.let { applies ->
                    SkillEffectApplier(location.location(), effect, applies)
                }
            }.takeIf { it.isNotEmpty() }
        }.flatten().toSet()
    }

    @SubscribeEvent
    fun onPlayerJoin(event: PlayerEvent.PlayerLoggedInEvent) {
        replicateToPlayer(event)
        syncEffects(event.entity)
    }

    @SubscribeEvent
    fun onPlayerRespawn(event: PlayerEvent.PlayerRespawnEvent) {
        replicateToPlayer(event)
        syncEffects(event.entity)
    }

    @SubscribeEvent
    fun onPlayerChangedDimension(event: PlayerEvent.PlayerChangedDimensionEvent) {
        replicateToPlayer(event)
        syncEffects(event.entity)
    }

    fun xpToLevelUp(level: Int): Int {
        return 100 + 25 * level + 5 * level * level
    }

    private fun replicateToPlayer(event: PlayerEvent) {
        val player = event.entity

        // Making sure that player will never be a LocalPlayer
        if (player.level().isClientSide) return

        val playerSkills = player.getData(AttachmentsRegistry.PLAYER_SKILLS)

        PacketDistributor.sendToPlayer(player as ServerPlayer, SyncSkillsPayload(playerSkills.getAllSkills()))
    }

    data class SkillModelSourceListener(val skill: ResourceLocation, val sourceData: AbstractSkillSource)

    data class SkillEffectApplier(
        val skill: ResourceLocation, val sourceData: AbstractSkillEffect, val filter: AbstractSkillEffectFilter
    )
}