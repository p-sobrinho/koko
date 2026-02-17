package dev.koji.koko.common.registry

import com.mojang.blaze3d.platform.InputConstants
import com.mojang.logging.LogUtils
import dev.koji.koko.Koko
import net.minecraft.client.KeyMapping
import net.neoforged.api.distmarker.Dist
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.common.EventBusSubscriber
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent
import org.lwjgl.glfw.GLFW

@EventBusSubscriber(modid = Koko.MOD_ID, value = [Dist.CLIENT])
object KeyRegistry {
    private val LOGGER = LogUtils.getLogger()

    const val CATEGORY = "keys.koko.name"

    val OPEN_SKILLS = KeyMapping(
        "keys.koko.skills", InputConstants.Type.KEYSYM,
        GLFW.GLFW_KEY_C, CATEGORY
    )

    @SubscribeEvent
    fun register(event: RegisterKeyMappingsEvent) { event.register(OPEN_SKILLS) }
}
