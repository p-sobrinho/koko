package net.koji.arc_steam.common

import net.koji.arc_steam.common.attachments.PlayerSkills
import net.koji.arc_steam.common.skills.SkillModel
import net.koji.arc_steam.network.payloads.SyncSkillPayload
import net.koji.arc_steam.network.payloads.SyncSkillsPayload
import net.koji.arc_steam.registry.AttachmentsRegistry
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.common.EventBusSubscriber
import net.neoforged.neoforge.event.entity.player.PlayerEvent
import net.neoforged.neoforge.network.PacketDistributor
import java.util.*

@EventBusSubscriber
object PlayerSkillsHandler {
    private val playerSkillsCache = HashMap<UUID, PlayerSkills>()

    fun addXp(player: ServerPlayer, skill: ResourceLocation, amount: Int) {
        val playerSkills = playerSkillsCache.computeIfAbsent(player.getUUID()) { ignored ->
            player.getData(AttachmentsRegistry.PLAYER_SKILLS)
        }

        playerSkills.addXp(skill, amount)

        val playerSkill = playerSkills.getSkill(skill)

        PacketDistributor.sendToPlayer(player, SyncSkillPayload(skill, playerSkill.xp, playerSkill.isOverClocked))
    }

    fun removeXp(player: ServerPlayer, skill: ResourceLocation, amount: Int) {
        val playerSkills = playerSkillsCache
            .computeIfAbsent(player.getUUID()) { u: UUID -> player.getData(AttachmentsRegistry.PLAYER_SKILLS) }

        playerSkills.removeXp(skill, amount)

        val playerSkill = playerSkills.getSkill(skill)

        PacketDistributor.sendToPlayer(player, SyncSkillPayload(skill, playerSkill.xp, playerSkill.isOverClocked))
    }

    @SubscribeEvent
    fun onPlayerJoin(event: PlayerEvent.PlayerLoggedInEvent) = replicateToPlayer(event)

    @SubscribeEvent
    fun onPlayerRespawn(event: PlayerEvent.PlayerRespawnEvent) = replicateToPlayer(event)

    @SubscribeEvent
    fun onPlayerChangedDimension(event: PlayerEvent.PlayerChangedDimensionEvent) = replicateToPlayer(event)

    private fun replicateToPlayer(event: PlayerEvent) {
        val player = event.entity as ServerPlayer

        val playerSkills: PlayerSkills = playerSkillsCache
            .computeIfAbsent(player.getUUID()) { u: UUID -> player.getData(AttachmentsRegistry.PLAYER_SKILLS) }

        PacketDistributor.sendToPlayer(player, SyncSkillsPayload(playerSkills.getAllSkills()))
    }
}
