package net.koji.arc_steam

import com.mojang.logging.LogUtils
import net.koji.arc_steam.common.registry.AttachmentsRegistry
import net.minecraft.resources.ResourceLocation
import net.neoforged.bus.api.IEventBus
import net.neoforged.fml.ModContainer
import net.neoforged.fml.common.Mod

@Mod(ArcaneSteam.MOD_ID)
class ArcaneSteam {
    companion object {
        const val MOD_ID = "arcane_steam"
        val LOGGER = LogUtils.getLogger()

        fun namespacePath(path: String): ResourceLocation {
            return ResourceLocation.fromNamespaceAndPath(MOD_ID, path)
        }
    }

    constructor(modEventBus: IEventBus, modContainer: ModContainer) {

        LOGGER.info("ArcaneSteam is loading...")

        AttachmentsRegistry.register(modEventBus)

        LOGGER.info("ArcaneSteam has been successfully loaded.")
    }
}
