package dev.koji.koko.common

import dev.koji.koko.Koko
import dev.koji.koko.common.attachments.PlayerSkills
import dev.koji.koko.common.models.SkillData
import dev.koji.koko.common.models.SkillModel
import dev.koji.koko.common.models.effects.AbstractSkillEffect
import dev.koji.koko.common.models.effects.AbstractSkillEffectFilter
import dev.koji.koko.common.models.sources.AbstractSkillSource
import dev.koji.koko.common.network.payloads.SyncSkillPayload
import dev.koji.koko.common.network.payloads.SyncSkillsPayload
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.Level
import net.neoforged.neoforge.network.PacketDistributor

object SkillsHandler {
    fun safeParseResource(resource: String, allowTag: Boolean = true): ResourceLocation {
        if (!allowTag && resource.contains("#"))
            throw RuntimeException("$resource must not be a tag.")

        return if (resource.contains(":"))
            ResourceLocation.parse(resource.removePrefix("#"))
        else
            ResourceLocation.fromNamespaceAndPath("minecraft", resource)
    }

    fun syncNewSkills(player: Player) {
        val level = player.level()

        if (level.isClientSide) return

        val skillModels = this.getSkillsModels(level) ?:
            return Koko.LOGGER.error("Unable to find SkillsModels in level context!")

        val playerSkills = this.getSkills(player)

        for (entry in skillModels)
            playerSkills.putIfAbsent(entry.key.location(), SkillData(entry.value.defaultXp, false))
    }

    fun syncEffects(player: Player) {
        if (player.level().isClientSide) return

        val pastAppliedEffects = this.getPastEffectsForPlayer(player)
        val toApplyEffects = this.getEffectsForPlayer(player)

        for (applier in pastAppliedEffects.filter { !toApplyEffects.contains(it) })
            applier.sourceData.unApply(applier, player)

        for (applier in toApplyEffects) applier.sourceData.apply(applier, player)
    }

    fun syncPlayerSkills(player: Player) {
        if (player.level().isClientSide) return

        val playerSkills = player.getData(CommonRegistry.PLAYER_SKILLS)

        PacketDistributor.sendToPlayer(player as ServerPlayer, SyncSkillsPayload(playerSkills.getAllSkills()))
    }

    fun updateXp(player: Player, skill: ResourceLocation, amount: Double) {
        if (player.level().isClientSide) return

        val playerSkills = player.getData(CommonRegistry.PLAYER_SKILLS)
        val playerSkill = playerSkills.getSkill(skill)
            ?: return Koko.LOGGER.warn("Unable to find skill with location $skill!")

        val currentXp = playerSkill.xp

        Koko.LOGGER.debug("Updating {} xp of skill {} from {} to {}.", player.name.string, currentXp, skill, currentXp + amount)

        val skillModel = this.getSkillModel(player.level(), skill)
            ?: return Koko.LOGGER.warn("Unable to find skill model for location $skill!")

        val maxLevel = if (playerSkill.isUnlocked) skillModel.unlockedMaxLevel else skillModel.maxLevel
        val maxXp = xpToLevelUp(maxLevel).toDouble()

        playerSkill.xp = (playerSkill.xp + amount).coerceIn(0.0, maxXp)

        PacketDistributor.sendToPlayer(player as ServerPlayer, SyncSkillPayload(skill, playerSkill))

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

    fun getSkills(player: Player): PlayerSkills = player.getData(CommonRegistry.PLAYER_SKILLS)

    fun getSkillModel(level: Level, skill: ResourceLocation): SkillModel? {
        val registry = level.registryAccess().registry(CommonRegistry.SKILL_REGISTRY)

        return registry.map { it.get(skill) }.orElse(null)
    }

    fun getSkillModel(player: Player, skill: ResourceLocation): SkillModel? =
        this.getSkillModel(player.level(), skill)

    fun getSkillsModels(level: Level): Set<Map.Entry<ResourceKey<SkillModel>, SkillModel>>? {
        val registry = level.registryAccess().registry(CommonRegistry.SKILL_REGISTRY)

        return registry.map { it.entrySet() }.orElse(null)
    }

    fun getSkillsModels(player: Player): Set<Map.Entry<ResourceKey<SkillModel>, SkillModel>>? =
        this.getSkillsModels(player.level())

    fun getListenersFor(skillSourceType: String, level: Level): Set<SkillModelSourceListener> {
        val skillsModels = this.getSkillsModels(level)
            ?: emptySet()

        return skillsModels.asSequence()
            .flatMap { (key, model) ->
                model.skillSources.asSequence()
                    .filter { it.type == skillSourceType }
                    .map { SkillModelSourceListener(key.location(), it) }
            }
            .toSet()
    }

    fun getLevel(player: Player, skill: ResourceLocation): Int {
        val skills = this.getSkills(player)
        val skillData = skills.getSkill(skill)
            ?: return 0

        val skillModel = this.getSkillModel(player.level(), skill)
            ?: return 0

        val xp = skillData.xp
        val maxLevel = if (skillData.isUnlocked) skillModel.unlockedMaxLevel else skillModel.maxLevel
        var total: Int

        for (level in 1..maxLevel) {
            total = this.xpToLevelUp(level)

            if (xp <= total) return level
        }

        return maxLevel
    }

    fun getEffectsForPlayer(player: Player): Set<SkillEffectApplier> {
        val skillsModels = this.getSkillsModels(player)
            ?: return emptySet()

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
            ?: return emptySet()

        return skillsModels.mapNotNull { (location, model) ->
            val playerLevel = this.getLevel(player, location.location())

            model.effects.mapNotNull { effect ->
                effect.doAnyApplies((playerLevel - 1).coerceAtMost(0))?.let { applies ->
                    SkillEffectApplier(location.location(), effect, applies)
                }
            }.takeIf { it.isNotEmpty() }
        }.flatten().toSet()
    }

    fun xpToLevelUp(level: Int): Int = (100 + 25 * level + 5 * level * level)

    data class SkillModelSourceListener(val skill: ResourceLocation, val sourceData: AbstractSkillSource)

    data class SkillEffectApplier(
        val skill: ResourceLocation, val sourceData: AbstractSkillEffect, val filter: AbstractSkillEffectFilter
    )
}