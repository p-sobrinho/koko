package dev.koji.koko.common.compact.ironspells.effects.filters

import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import dev.koji.koko.common.models.effects.AbstractSkillEffectFilter
import dev.koji.koko.common.models.effects.Filters
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec

class SpellCastSkillEffectFilter(
    val spellLevel: Int, val level: Int
): AbstractSkillEffectFilter() {
    override val type: String = Filters.ABOVE

    override fun apply(level: Int): Boolean = (level >= this.level)

    companion object {
        val CODEC: MapCodec<SpellCastSkillEffectFilter> = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                Codec.INT.fieldOf("spellLevel").forGetter(SpellCastSkillEffectFilter::spellLevel),
                Codec.INT.fieldOf("level").forGetter(SpellCastSkillEffectFilter::level),
            ).apply(instance, ::SpellCastSkillEffectFilter)
        }

        val STREAM_CODEC: StreamCodec<RegistryFriendlyByteBuf, SpellCastSkillEffectFilter> = StreamCodec.composite(
            ByteBufCodecs.INT, SpellCastSkillEffectFilter::spellLevel,
            ByteBufCodecs.INT, SpellCastSkillEffectFilter::level,
            ::SpellCastSkillEffectFilter
        )
    }
}