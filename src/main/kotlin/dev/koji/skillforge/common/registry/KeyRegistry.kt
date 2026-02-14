package dev.koji.skillforge.common.registry

import com.mojang.blaze3d.platform.InputConstants
import com.mojang.logging.LogUtils
import dev.koji.skillforge.SkillForge
import net.minecraft.client.KeyMapping
import net.neoforged.api.distmarker.Dist
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.common.EventBusSubscriber
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent
import org.lwjgl.glfw.GLFW

@EventBusSubscriber(modid = SkillForge.MOD_ID, value = [Dist.CLIENT])
object KeyRegistry {
    private val LOGGER = LogUtils.getLogger()

    const val CATEGORY = "key.categories.arcane_steam"

    val OPEN_SKILLS = KeyMapping(
        "key.arc_steam.open_skills", InputConstants.Type.KEYSYM,
        GLFW.GLFW_KEY_C, CATEGORY
    )

    @SubscribeEvent
    fun register(event: RegisterKeyMappingsEvent) { event.register(OPEN_SKILLS) }
}
