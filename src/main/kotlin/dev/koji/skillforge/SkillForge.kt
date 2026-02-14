package dev.koji.skillforge

import com.mojang.logging.LogUtils
import dev.koji.skillforge.common.registry.AttachmentsRegistry
import net.minecraft.resources.ResourceLocation
import net.neoforged.bus.api.IEventBus
import net.neoforged.fml.ModContainer
import net.neoforged.fml.common.Mod

@Mod(SkillForge.MOD_ID)
class SkillForge {
    companion object {
        const val MOD_ID = "skillforge"
        val LOGGER = LogUtils.getLogger()

        fun namespacePath(path: String): ResourceLocation {
            return ResourceLocation.fromNamespaceAndPath(MOD_ID, path)
        }
    }

    constructor(modEventBus: IEventBus, modContainer: ModContainer) {
        LOGGER.info("SkillForge is loading...")

        AttachmentsRegistry.register(modEventBus)

        LOGGER.info("SkillForge has been successfully loaded.")
    }
}
