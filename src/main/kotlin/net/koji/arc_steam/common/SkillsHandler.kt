package net.koji.arc_steam.common

import net.koji.arc_steam.ArcaneSteam
import net.koji.arc_steam.common.attachments.PlayerSkills
import net.koji.arc_steam.common.models.SkillData
import net.koji.arc_steam.common.models.SkillModel
import net.koji.arc_steam.common.models.sources.SkillSource
import net.koji.arc_steam.common.network.payloads.SyncSkillPayload
import net.koji.arc_steam.common.network.payloads.SyncSkillsPayload
import net.koji.arc_steam.common.registry.AttachmentsRegistry
import net.koji.arc_steam.common.registry.DatapackRegistry
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.Level
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.common.EventBusSubscriber
import net.neoforged.neoforge.event.entity.player.PlayerEvent
import net.neoforged.neoforge.network.PacketDistributor
import java.util.*

@EventBusSubscriber
object SkillsHandler {
    private val LOGGER = ArcaneSteam.LOGGER
    private val playerSkillsCache = HashMap<UUID, PlayerSkills>()

    fun syncNewSkills(level: Level, playerSkills: PlayerSkills) {
        val skillModels = getSkillsModels(level)

        for (entry in skillModels) {
            playerSkills.putIfAbsent(entry.key.location(), SkillData(entry.value.defaultXp, false))
        }
    }

    fun updateXp(player: Player, skill: ResourceLocation, amount: Double) {
        if (player.level().isClientSide) return;

        val playerSkills = playerSkillsCache.computeIfAbsent(player.getUUID()) { _ ->
            player.getData(AttachmentsRegistry.PLAYER_SKILLS)
        }

        LOGGER.info("Updating {} xp from skill {} to {}.", player.name, skill, amount)
        playerSkills.updateXp(skill, amount)

        val playerSkill = playerSkills.getSkill(skill)

        PacketDistributor.sendToPlayer(
            player as ServerPlayer, SyncSkillPayload(skill, playerSkill)
        )
    }

    fun getSkill(player: Player, skill: ResourceLocation) = this.getSkills(player).getSkill(skill)

    fun getSkills(player: Player): PlayerSkills = player.getData(AttachmentsRegistry.PLAYER_SKILLS)

    fun getLevel(player: Player, skill: ResourceLocation): Int {
        val skills = this.getSkills(player)
        val skillData = skills.getSkill(skill)
        val skillModel = this.getSkillModel(player.level(), skill)

        val xp = skillData.xp
        val maxLevel = if (skillData.isOverClocked) skillModel.overClockedMaxLevel else skillModel.maxLevel
        var total = 0

        for (level in 1..maxLevel) {
            total += this.xpToLevelUp(level)

            if (xp < total) {
                return level
            }
        }

        return maxLevel
    }

    fun getSkillModel(level: Level, skill: ResourceLocation): SkillModel {
        val registry = level.registryAccess().registryOrThrow(DatapackRegistry.SKILL_REGISTRY);

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

    fun replaceSkill(player: Player, skill: ResourceLocation, data: SkillData) {
        val playerSkills = this.getSkills(player)

        playerSkills.put(skill, data)
    }

    fun replaceSkills(player: Player, skills: Map<ResourceLocation, SkillData>) {
        val playerSkills = this.getSkills(player)

        playerSkills.replace(skills)
    }

    @SubscribeEvent
    fun onPlayerJoin(event: PlayerEvent.PlayerLoggedInEvent) = replicateToPlayer(event)

    @SubscribeEvent
    fun onPlayerRespawn(event: PlayerEvent.PlayerRespawnEvent) = replicateToPlayer(event)

    @SubscribeEvent
    fun onPlayerChangedDimension(event: PlayerEvent.PlayerChangedDimensionEvent) = replicateToPlayer(event)

    private fun replicateToPlayer(event: PlayerEvent) {
        val player = event.entity

        // Making sure that player will never be a LocalPlayer
        if (player.level().isClientSide) return

        val playerSkills = playerSkillsCache.computeIfAbsent(player.getUUID()) { _ ->
            player.getData(AttachmentsRegistry.PLAYER_SKILLS)
        }

        PacketDistributor.sendToPlayer(player as ServerPlayer, SyncSkillsPayload(playerSkills.getAllSkills()))
    }

    private fun xpToLevelUp(level: Int): Int {
        return (100 + level * 25)
    }

    data class SkillModelSourceListener(val skill: ResourceLocation, val sourceData: SkillSource)
}