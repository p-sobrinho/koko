package dev.koji.koko.common.compact

import dev.koji.koko.common.compact.curios.CuriosCompact
import net.neoforged.fml.ModList
import net.neoforged.neoforge.common.NeoForge

object Compatibilities {
    fun register() {
        val modList = ModList.get()

        if (modList.isLoaded("curios")) {
            val curiosCompact = CuriosCompact()

            curiosCompact.register()

            NeoForge.EVENT_BUS.register(curiosCompact)
        }
    }
}