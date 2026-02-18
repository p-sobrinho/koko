package dev.koji.koko.client.events

import dev.koji.koko.Koko
import dev.koji.koko.client.ClientRegistry
import dev.koji.koko.client.ui.SkillsScreen
import net.minecraft.client.Minecraft
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.common.EventBusSubscriber
import net.neoforged.neoforge.client.event.ClientTickEvent

@EventBusSubscriber(modid = Koko.MOD_ID)
object ClientEventHandler {
    @SubscribeEvent
    fun onClientTick(event: ClientTickEvent.Post) {
        while (ClientRegistry.OPEN_SKILLS.consumeClick()) {
            val mc = Minecraft.getInstance()

            mc.setScreen(SkillsScreen())
        }
    }
}