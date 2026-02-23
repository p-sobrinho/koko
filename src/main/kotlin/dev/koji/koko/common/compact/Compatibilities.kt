package dev.koji.koko.common.compact

import dev.koji.koko.common.compact.curios.CuriosCompact
import dev.koji.koko.common.compact.ironspells.IronSpellsCompact
import net.neoforged.fml.ModList
import net.neoforged.neoforge.common.NeoForge

object Compatibilities {
    fun register() {
        val modList = ModList.get()

        if (modList.isLoaded("curios")) {
            CuriosCompact.register()

            NeoForge.EVENT_BUS.register(CuriosCompact)
        }

        if (modList.isLoaded("irons_spellbooks")) {
            IronSpellsCompact.register()

            NeoForge.EVENT_BUS.register(IronSpellsCompact)
        }
    }
}