package net.koji.arc_steam.network

import net.koji.arc_steam.ArcaneSteam
import net.koji.arc_steam.network.payloads.SyncSkillsPayload
import net.koji.arc_steam.registry.AttachmentsRegistry
import net.minecraft.client.Minecraft
import net.minecraft.client.player.LocalPlayer
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.common.EventBusSubscriber
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent
import net.neoforged.neoforge.network.handling.IPayloadContext
import net.neoforged.neoforge.network.handling.IPayloadHandler
import net.neoforged.neoforge.network.registration.PayloadRegistrar

@EventBusSubscriber(modid = ArcaneSteam.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
object ClientNetwork {
    @SubscribeEvent
    fun registerPayloadHandlers(event: RegisterPayloadHandlersEvent) {
        val registrar: PayloadRegistrar = event.registrar(ArcaneSteam.namespacePath("payloaders").toString())

        registrar.playToClient(SyncSkillsPayload.TYPE, SyncSkillsPayload.STREAM_CODEC) { payload, context ->
            val minecraft: Minecraft = Minecraft.getInstance()
            minecraft.execute {
                val player = minecraft.player ?: return@execute

                val skills = player.getData(AttachmentsRegistry.PLAYER_SKILLS)
                skills.replace(payload.skillData)
            }
        }
    }
}
