package dev.koji.koko

import com.mojang.logging.LogUtils
import dev.koji.koko.common.CommonRegistry
import dev.koji.koko.common.compact.Compatibilities
import net.minecraft.resources.ResourceLocation
import net.neoforged.bus.api.IEventBus
import net.neoforged.fml.ModContainer
import net.neoforged.fml.common.Mod

@Mod(Koko.MOD_ID)
class Koko {
    companion object {
        const val MOD_ID = "koko"
        val LOGGER = LogUtils.getLogger()

        fun namespacePath(path: String): ResourceLocation {
            return ResourceLocation.fromNamespaceAndPath(MOD_ID, path)
        }
    }

    constructor(modEventBus: IEventBus, modContainer: ModContainer) {
        LOGGER.info("Koko is loading...")

        CommonRegistry.register(modEventBus)
        Compatibilities.register()

        LOGGER.info("Koko has been successfully loaded.")
    }
}
