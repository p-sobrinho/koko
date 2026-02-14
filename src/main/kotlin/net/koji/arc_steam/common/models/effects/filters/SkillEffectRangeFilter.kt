package net.koji.arc_steam.common.models.effects.filters

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.koji.arc_steam.ArcaneSteam
import net.koji.arc_steam.common.models.effects.AbstractSkillEffectFilter
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import net.minecraft.world.entity.ai.attributes.AttributeModifier

class SkillEffectRangeFilter(
    val from: Int, val to: Int, val value: Double, val operation: AttributeModifier.Operation
): AbstractSkillEffectFilter() {
    override val type: String = TYPE

    override fun apply(level: Int): Boolean = (level in from..to)

    companion object {
        const val TYPE = "range"

        val CODEC = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                Codec.INT.fieldOf("from").forGetter(SkillEffectRangeFilter::from),
                Codec.INT.fieldOf("to").forGetter(SkillEffectRangeFilter::to),
                Codec.DOUBLE.fieldOf("value").forGetter(SkillEffectRangeFilter::value),
                AttributeModifier.Operation.CODEC.fieldOf("operation").forGetter(SkillEffectRangeFilter::operation)
            ).apply(instance, ::SkillEffectRangeFilter)
        }

        val STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, SkillEffectRangeFilter::from,
            ByteBufCodecs.INT, SkillEffectRangeFilter::to,
            ByteBufCodecs.DOUBLE, SkillEffectRangeFilter::value,
            AttributeModifier.Operation.STREAM_CODEC, SkillEffectRangeFilter::operation,
            ::SkillEffectRangeFilter
        )
    }
}