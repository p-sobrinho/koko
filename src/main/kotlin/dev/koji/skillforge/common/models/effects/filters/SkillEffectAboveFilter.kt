package dev.koji.skillforge.common.models.effects.filters

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import dev.koji.skillforge.common.models.effects.AbstractSkillEffectFilter
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import net.minecraft.world.entity.ai.attributes.AttributeModifier

class SkillEffectAboveFilter(
    val level: Int, val value: Double, val operation: AttributeModifier.Operation
): AbstractSkillEffectFilter() {
    override val type: String = TYPE

    override fun apply(level: Int): Boolean = (level > this.level)

    companion object {
        const val TYPE = "above"

        val CODEC = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                Codec.INT.fieldOf("level").forGetter(SkillEffectAboveFilter::level),
                Codec.DOUBLE.fieldOf("value").forGetter(SkillEffectAboveFilter::value),
                AttributeModifier.Operation.CODEC.fieldOf("operation").forGetter(SkillEffectAboveFilter::operation)
            ).apply(instance, ::SkillEffectAboveFilter)
        }

        val STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, SkillEffectAboveFilter::level,
            ByteBufCodecs.DOUBLE, SkillEffectAboveFilter::value,
            AttributeModifier.Operation.STREAM_CODEC, SkillEffectAboveFilter::operation,
            ::SkillEffectAboveFilter
        )
    }
}