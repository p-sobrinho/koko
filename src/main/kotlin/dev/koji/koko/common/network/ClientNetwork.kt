package dev.koji.koko.common.network

import dev.koji.koko.Koko
import dev.koji.koko.common.SkillsHandler
import dev.koji.koko.common.network.payloads.SyncSkillPayload
import dev.koji.koko.common.network.payloads.SyncSkillsPayload
import net.minecraft.client.Minecraft
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.common.EventBusSubscriber
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent

@EventBusSubscriber(modid = Koko.MOD_ID)
object ClientNetwork {
    @SubscribeEvent
    fun registerPayloadHandlers(event: RegisterPayloadHandlersEvent) {
        val registrar = event.registrar(Koko.namespacePath("payloaders").toString())

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
