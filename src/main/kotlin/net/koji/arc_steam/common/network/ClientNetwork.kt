package net.koji.arc_steam.common.network

import net.koji.arc_steam.ArcaneSteam
import net.koji.arc_steam.common.network.payloads.SyncSkillsPayload
import net.koji.arc_steam.common.SkillsHandler
import net.koji.arc_steam.common.network.payloads.SyncSkillPayload
import net.minecraft.client.Minecraft
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.common.EventBusSubscriber
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent
import net.neoforged.neoforge.network.registration.PayloadRegistrar

@EventBusSubscriber(modid = ArcaneSteam.MOD_ID)
object ClientNetwork {
    @SubscribeEvent
    fun registerPayloadHandlers(event: RegisterPayloadHandlersEvent) {
        val registrar = event.registrar(ArcaneSteam.namespacePath("payloaders").toString())

        registrar.playToClient(SyncSkillsPayload.TYPE, SyncSkillsPayload.STREAM_CODEC) { payload, context ->
            val minecraft = Minecraft.getInstance()
            minecraft.execute {
                val player = minecraft.player ?: return@execute

                SkillsHandler.replaceSkills(player, payload.skillData)
            }
        }

        registrar.playToClient(SyncSkillPayload.TYPE, SyncSkillPayload.STREAM_CODEC) { payload, context ->
            val minecraft = Minecraft.getInstance()

            minecraft.execute {
                val player = minecraft.player ?: return@execute

                SkillsHandler.replaceSkill(player, payload.skill, payload.skillData)
            }
        }
    }
}
