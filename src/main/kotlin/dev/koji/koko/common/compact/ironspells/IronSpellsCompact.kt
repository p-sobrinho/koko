package dev.koji.koko.common.compact.ironspells

import dev.koji.koko.Koko
import dev.koji.koko.common.SkillsHandler
import dev.koji.koko.common.compact.ironspells.effects.SpellCastSkillEffect
import dev.koji.koko.common.compact.ironspells.effects.SpellInscribeSkillEffect
import dev.koji.koko.common.compact.ironspells.effects.filters.SpellCastSkillEffectFilter
import dev.koji.koko.common.compact.ironspells.effects.filters.SpellInscribeSkillEffectFilter
import dev.koji.koko.common.compact.ironspells.sources.SpellCastSource
import dev.koji.koko.common.compact.ironspells.sources.SpellInscribeSource
import dev.koji.koko.common.helpers.MainHelper
import dev.koji.koko.common.models.effects.AbstractSkillEffect
import dev.koji.koko.common.models.effects.AbstractSkillEffectFilter
import dev.koji.koko.common.models.sources.AbstractSkillSource
import dev.koji.koko.common.models.sources.SkillSourceFilter
import io.redspace.ironsspellbooks.api.events.InscribeSpellEvent
import io.redspace.ironsspellbooks.api.events.SpellOnCastEvent
import io.redspace.ironsspellbooks.api.events.SpellPreCastEvent
import io.redspace.ironsspellbooks.api.spells.SpellData
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.player.Player
import net.neoforged.bus.api.SubscribeEvent
import java.util.*

object IronSpellsCompact {
    private val BLOCKED_PLAYER_INSTANCES = mutableSetOf<BlockedSpellInstance>()

    @SubscribeEvent
    fun onSpellPreCast(event: SpellPreCastEvent) {
        val player = event.entity

        Koko.LOGGER.info(event.spellId)
        val spellData = SpellData(MainHelper.safeParseResource(event.spellId), event.spellLevel, false)

        if (!this.isSpellBlockedFor(player, spellData, ISSBlockScope.CAST)) return

        event.isCanceled = true

        // Looks like this event will never fire in client.
        // if (!player.level().isClientSide) return

        MainHelper.sendMessageToPlayer(player, DefaultIronMessages.UNABLE_TO_CAST)
    }

    @SubscribeEvent
    fun onSpellCast(event: SpellOnCastEvent) {

    }

    @SubscribeEvent
    fun onSpellInscribe(event: InscribeSpellEvent) {
        val player = event.entity

        if (this.isSpellBlockedFor(player, event.spellData, ISSBlockScope.INSCRIBE)) {
            event.isCanceled = true

            // Looks like this event will never fire in client.
            // if (!player.level().isClientSide) return

            MainHelper.sendMessageToPlayer(player, DefaultIronMessages.UNABLE_TO_INSCRIBE)

            return
        }

        this.processSpellEvaluate(Sources.PLAYER_SPELL_INSCRIBE, event.spellData, player)
    }

    fun addBlockedSpell(uuid: UUID, spellId: ResourceLocation, spellLevel: Int, scope: ISSBlockScope) {
        val blockedItems = this.getBlockedPlayerInstance(uuid).blockedItems
        var blockedItem = blockedItems.find { it.location == spellId }

        if (blockedItem == null) {
            blockedItem = BlockedSpell(spellId, spellLevel, mutableSetOf())

            blockedItems.add(blockedItem)
        }

        blockedItem.scopes.add(scope)
    }

    fun removeBlockedSpell(uuid: UUID, spellLocation: ResourceLocation, scope: ISSBlockScope) {
        val blockedItem = this.getBlockedPlayerInstance(uuid).blockedItems.find { it.location == spellLocation }

        if (blockedItem == null) return

        blockedItem.scopes.removeIf { it == scope }
    }

    fun clearBlockedItemsFor(uuid: UUID, scope: ISSBlockScope) {
        for (blockedItem in this.getBlockedPlayerInstance(uuid).blockedItems) {
            blockedItem.scopes.removeIf { it == scope }
        }
    }

    fun clearAllBlockedItemsFor(uuid: UUID) = this.getBlockedPlayerInstance(uuid).blockedItems.clear()

    fun isSpellBlockedFor(player: Player, spellData: SpellData, scope: ISSBlockScope): Boolean =
        this.isSpellBlockedFor(player.uuid, spellData, scope)

    fun isSpellBlockedFor(uuid: UUID, spellData: SpellData, scope: ISSBlockScope): Boolean {
        val blockedRecipes = this.getBlockedItemsInScope(uuid, scope)

        val isBlocked = blockedRecipes.find { this.isSpellLevelIsRestricted(spellData, it.level) }

        return (isBlocked != null)
    }

    fun isSpellLevelIsRestricted(spellData: SpellData, level: Int): Boolean = (spellData.level > level)

    fun getBlockedItemsInScope(uuid: UUID, scope: ISSBlockScope): Set<BlockedSpell> {
        val blockedItems = BLOCKED_PLAYER_INSTANCES.find { it.uuid == uuid }
            ?.blockedItems
            ?.filter { it.scopes.contains(scope) }
            ?.toSet()

        return blockedItems ?: emptySet()
    }

    fun getBlockedPlayerInstance(player: Player) =
        this.getBlockedPlayerInstance(player.uuid)

    fun getBlockedPlayerInstance(uuid: UUID): BlockedSpellInstance {
        var foundInstance = BLOCKED_PLAYER_INSTANCES.find {
            it.uuid == uuid
        }

        if (foundInstance == null) {
            foundInstance = BlockedSpellInstance(uuid, mutableSetOf())

            BLOCKED_PLAYER_INSTANCES.add(foundInstance)
        }

        return foundInstance
    }

    fun processSpellEvaluate(
        source: String,
        spell: SpellData,
        player: Player
    ) {
        val listeners = SkillsHandler.getListenersFor(source, player.level())

        for (listener in listeners) {
            val skillSource = listener.sourceData

            val xp = if (skillSource.alwaysApply)
                skillSource.alwaysValue
            else
                this.playerSpellEvaluate(listener.sourceData, spell)

            if (xp == 0.0) continue

            SkillsHandler.updateXp(player, listener.skill, xp)
        }
    }

    fun playerSpellEvaluate(skillModel: AbstractSkillSource, spellData: SpellData): Double =
        this.playerSpellEvaluate(skillModel.filters, spellData)

    fun playerSpellEvaluate(filters: List<SkillSourceFilter>, spellData: SpellData): Double {
        if (filters.isEmpty()) return 0.0

        val whitelists = filters.filter { it.type == SkillSourceFilter.FilterType.WHITELIST }
            .sortedByDescending { it.priority }

        val blacklists = filters.filter { it.type == SkillSourceFilter.FilterType.BLACKLIST }
            .sortedByDescending { it.priority }

        val spellId = spellData.spell.spellId

        for (blacklist in blacklists) {
            if (spellId == blacklist.target) return 0.0
        }

        if (whitelists.isEmpty()) return 0.0

        for (whitelist in whitelists) {
            if (spellId == whitelist.target) {
                val xp = whitelist.xp

                return if (whitelist.inverse) -xp else xp
           }
        }

        return 0.0
    }

    fun register() {
        AbstractSkillSource.codecsMapper[Sources.PLAYER_SPELL_CAST] = SpellCastSource.CODEC
        AbstractSkillSource.streamMapper[Sources.PLAYER_SPELL_CAST] = SpellCastSource.STREAM_CODEC

        AbstractSkillSource.codecsMapper[Sources.PLAYER_SPELL_INSCRIBE] = SpellInscribeSource.CODEC
        AbstractSkillSource.streamMapper[Sources.PLAYER_SPELL_INSCRIBE] = SpellInscribeSource.STREAM_CODEC

        AbstractSkillEffect.codecMapper[Effects.PLAYER_SPELL_CAST] = SpellCastSkillEffect.CODEC
        AbstractSkillEffect.streamMapper[Effects.PLAYER_SPELL_CAST] = SpellCastSkillEffect.STREAM_CODEC

        AbstractSkillEffect.codecMapper[Effects.PLAYER_SPELL_INSCRIBE] = SpellInscribeSkillEffect.CODEC
        AbstractSkillEffect.streamMapper[Effects.PLAYER_SPELL_INSCRIBE] = SpellInscribeSkillEffect.STREAM_CODEC

        AbstractSkillEffectFilter.codecsMapper[EffectsFilters.PLAYER_SPELL_CAST_FILTER] =
            SpellCastSkillEffectFilter.CODEC

        AbstractSkillEffectFilter.codecsMapper[EffectsFilters.PLAYER_SPELL_INSCRIBE_FILTER] =
            SpellInscribeSkillEffectFilter.CODEC

        AbstractSkillEffectFilter.streamMapper[EffectsFilters.PLAYER_SPELL_CAST_FILTER] =
            SpellCastSkillEffectFilter.STREAM_CODEC

        AbstractSkillEffectFilter.streamMapper[EffectsFilters.PLAYER_SPELL_INSCRIBE_FILTER] =
            SpellInscribeSkillEffectFilter.STREAM_CODEC
    }


    data class BlockedSpellInstance(
        val uuid: UUID,
        val blockedItems: MutableSet<BlockedSpell>,
    ) {
        override fun equals(other: Any?): Boolean {
            return other is BlockedSpellInstance && other.uuid == this.uuid
        }

        override fun hashCode(): Int = uuid.hashCode()
    }

    data class BlockedSpell(val location: ResourceLocation, val level: Int, val scopes: MutableSet<ISSBlockScope>)

    enum class ISSBlockScope { CAST, INSCRIBE }

    object Sources {
        const val PLAYER_SPELL_CAST = "player/ispell_cast"
        const val PLAYER_SPELL_INSCRIBE = "player/ispell_inscribe"
    }

    object Effects {
        const val PLAYER_SPELL_CAST = "player/ispell_precast"
        const val PLAYER_SPELL_INSCRIBE = "player/ispell_inscribe"
    }

    object EffectsFilters {
        const val PLAYER_SPELL_CAST_FILTER = "player/ispell_precast_filter"
        const val PLAYER_SPELL_INSCRIBE_FILTER = "player/ispell_inscribe_filter"
    }

    object DefaultIronMessages {
        const val UNABLE_TO_CAST = "&cYou don't feel ready to cast this spell."
        const val UNABLE_TO_INSCRIBE = "&cYour knowledge is too poor to this spell."
    }
}