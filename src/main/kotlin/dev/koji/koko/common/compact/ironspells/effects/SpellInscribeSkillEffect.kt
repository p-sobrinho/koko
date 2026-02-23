package dev.koji.koko.common.compact.ironspells.effects

import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import dev.koji.koko.Koko
import dev.koji.koko.common.SkillsHandler
import dev.koji.koko.common.compact.ironspells.IronSpellsCompact
import dev.koji.koko.common.compact.ironspells.effects.filters.SpellInscribeSkillEffectFilter
import dev.koji.koko.common.helpers.MainHelper
import dev.koji.koko.common.models.effects.AbstractSkillEffect
import dev.koji.koko.common.models.effects.AbstractSkillEffectFilter
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import net.minecraft.world.entity.player.Player

class SpellInscribeSkillEffect(
    val spell: String,
    val filters: List<AbstractSkillEffectFilter>
) : AbstractSkillEffect() {
    override val type: String = IronSpellsCompact.Effects.PLAYER_SPELL_INSCRIBE

    override fun doAnyApplies(level: Int): AbstractSkillEffectFilter? {
        val applicableFilters = filters.filter { it.apply(level) }

        return when (applicableFilters.size) {
            0 -> null
            1 -> applicableFilters.first()
            else -> {
                Koko.LOGGER.warn("Filter overlapping is not supported.")

                applicableFilters.first()
            }
        }
    }

    override fun apply(applier: SkillsHandler.SkillEffectApplier, player: Player) {
        val filter = applier.filter as? SpellInscribeSkillEffectFilter ?: return

        IronSpellsCompact.addBlockedSpell(
            player.uuid, MainHelper.safeParseResource(spell), filter.spellLevel, IronSpellsCompact.ISSBlockScope.INSCRIBE
        )
    }

    override fun unApply(applier: SkillsHandler.SkillEffectApplier, player: Player) {
        IronSpellsCompact.removeBlockedSpell(
            player.uuid, MainHelper.safeParseResource(spell), IronSpellsCompact.ISSBlockScope.INSCRIBE
        )
    }

    companion object {
        val CODEC: MapCodec<SpellInscribeSkillEffect> = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                Codec.STRING.fieldOf("spell").forGetter(SpellInscribeSkillEffect::spell),
                AbstractSkillEffectFilter.CODEC.listOf().fieldOf("filters").forGetter(SpellInscribeSkillEffect::filters)
            ).apply(instance, ::SpellInscribeSkillEffect)
        }

        val STREAM_CODEC: StreamCodec<RegistryFriendlyByteBuf, SpellInscribeSkillEffect> = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, SpellInscribeSkillEffect::spell,
            AbstractSkillEffectFilter.STREAM_CODEC.apply(ByteBufCodecs.list()), SpellInscribeSkillEffect::filters,
            ::SpellInscribeSkillEffect
        )
    }
}