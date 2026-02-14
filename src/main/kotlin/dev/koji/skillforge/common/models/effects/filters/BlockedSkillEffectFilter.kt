package dev.koji.skillforge.common.models.effects.filters

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import dev.koji.skillforge.common.models.effects.AbstractSkillEffectFilter
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec

class BlockedSkillEffectFilter(val until: Int): AbstractSkillEffectFilter() {
    override val type: String = TYPE

    override fun apply(level: Int): Boolean = (level < until)

    companion object {
        const val TYPE = "filter/blocked"

        val CODEC = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                Codec.INT.fieldOf("until").forGetter(BlockedSkillEffectFilter::until),
            ).apply(instance, ::BlockedSkillEffectFilter)
        }

        val STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, BlockedSkillEffectFilter::until,
            ::BlockedSkillEffectFilter
        )
    }
}