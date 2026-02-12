package net.koji.arc_steam.registry

import com.mojang.blaze3d.platform.InputConstants
import net.koji.arc_steam.ArcaneSteam
import net.koji.arc_steam.common.PlayerSkillsHandler
import net.koji.arc_steam.common.attachments.PlayerSkills
import net.minecraft.client.KeyMapping
import net.minecraft.client.Minecraft
import net.neoforged.api.distmarker.Dist
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.common.EventBusSubscriber
import net.neoforged.neoforge.client.event.ClientTickEvent
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent
import net.neoforged.neoforge.event.level.BlockEvent
import org.lwjgl.glfw.GLFW

@EventBusSubscriber(modid = ArcaneSteam.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = [Dist.CLIENT])
object KeyRegistry {
    private val LOGGER = ArcaneSteam.LOGGER
    const val CATEGORY = "key.categories.arcane_steam"

    val OPEN_SKILLS = KeyMapping(
        "key.arc_steam.open_skills", InputConstants.Type.KEYSYM,
        GLFW.GLFW_KEY_C, CATEGORY
    )

    @SubscribeEvent
    fun register(event: RegisterKeyMappingsEvent) { event.register(OPEN_SKILLS) }
}
