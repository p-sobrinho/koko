package dev.koji.koko.client

import com.mojang.blaze3d.platform.InputConstants
import dev.koji.koko.Koko
import dev.koji.koko.common.Translatables
import net.minecraft.client.KeyMapping
import net.neoforged.api.distmarker.Dist
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.common.EventBusSubscriber
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent
import org.lwjgl.glfw.GLFW

@EventBusSubscriber(modid = Koko.MOD_ID, value = [Dist.CLIENT])
object ClientRegistry {
    val OPEN_SKILLS = KeyMapping(
        Translatables.KEYS_KOKO_SKILLS,
        InputConstants.Type.KEYSYM,
        GLFW.GLFW_KEY_C,
        Translatables.KEYS_KOKO_CATEGORY
    )

    @SubscribeEvent
    fun register(event: RegisterKeyMappingsEvent) { event.register(OPEN_SKILLS) }
}