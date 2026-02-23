package dev.koji.koko.common.compact.ironspells.effects.filters

import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import dev.koji.koko.common.compact.ironspells.IronSpellsCompact
import dev.koji.koko.common.models.effects.AbstractSkillEffectFilter
import dev.koji.koko.common.models.effects.Filters
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec

class SpellInscribeSkillEffectFilter(
    val spellLevel: Int, val level: Int
): AbstractSkillEffectFilter() {
    override val type: String = IronSpellsCompact.EffectsFilters.PLAYER_SPELL_INSCRIBE_FILTER

    override fun apply(level: Int): Boolean = (this.level > level)

    companion object {
        val CODEC: MapCodec<SpellInscribeSkillEffectFilter> = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                Codec.INT.fieldOf("spellLevel").forGetter(SpellInscribeSkillEffectFilter::spellLevel),
                Codec.INT.fieldOf("level").forGetter(SpellInscribeSkillEffectFilter::level),
            ).apply(instance, ::SpellInscribeSkillEffectFilter)
        }

        val STREAM_CODEC: StreamCodec<RegistryFriendlyByteBuf, SpellInscribeSkillEffectFilter> = StreamCodec.composite(
            ByteBufCodecs.INT, SpellInscribeSkillEffectFilter::spellLevel,
            ByteBufCodecs.INT, SpellInscribeSkillEffectFilter::level,
            ::SpellInscribeSkillEffectFilter
        )
    }
}