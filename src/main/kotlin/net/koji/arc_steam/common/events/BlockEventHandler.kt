package net.koji.arc_steam.common.events

import net.koji.arc_steam.ArcaneSteam
import net.koji.arc_steam.common.SkillsHandler
import net.koji.arc_steam.common.network.payloads.SyncSkillPayload
import net.koji.arc_steam.common.registry.AttachmentsRegistry
import net.minecraft.server.level.ServerPlayer
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.common.EventBusSubscriber
import net.neoforged.neoforge.event.level.BlockEvent
import net.neoforged.neoforge.network.PacketDistributor

@EventBusSubscriber
object BlockEventHandler {
    private val LOGGER = ArcaneSteam.LOGGER

    @SubscribeEvent
    fun onBlockBreak(event: BlockEvent.BreakEvent) {
        val player = event.player as ServerPlayer

        LOGGER.info("{} breaked a block", player.name)

        SkillsHandler.addXp(player, ArcaneSteam.namespacePath("mining"), 10)
    }
}
