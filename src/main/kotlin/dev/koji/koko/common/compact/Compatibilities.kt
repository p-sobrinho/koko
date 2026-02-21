package dev.koji.koko.common.compact

import dev.koji.koko.common.compact.curios.Curios
import net.neoforged.bus.api.IEventBus
import net.neoforged.fml.ModList
import net.neoforged.neoforge.common.NeoForge

object Compatibilities {
    fun register() {
        val modList = ModList.get()

        if (modList.isLoaded("curios")) {
            val curiosCompact = Curios()

            curiosCompact.register()

            NeoForge.EVENT_BUS.register(curiosCompact)
        }
    }
}