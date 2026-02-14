package net.koji.arc_steam.common.models.effects.filters

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.koji.arc_steam.ArcaneSteam
import net.koji.arc_steam.common.models.effects.AbstractSkillEffectFilter
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import net.minecraft.world.entity.ai.attributes.AttributeModifier

class SkillEffectBellowFilter(
    val level: Int, val value: Double, val operation: AttributeModifier.Operation
): AbstractSkillEffectFilter() {
    override val type: String = TYPE

    override fun apply(level: Int): Boolean = (level < this.level)

    companion object {
        const val TYPE = "bellow"

        val CODEC = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                Codec.INT.fieldOf("level").forGetter(SkillEffectBellowFilter::level),
                Codec.DOUBLE.fieldOf("value").forGetter(SkillEffectBellowFilter::value),
                AttributeModifier.Operation.CODEC.fieldOf("operation").forGetter(SkillEffectBellowFilter::operation)
            ).apply(instance, ::SkillEffectBellowFilter)
        }

        val STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, SkillEffectBellowFilter::level,
            ByteBufCodecs.DOUBLE, SkillEffectBellowFilter::value,
            AttributeModifier.Operation.STREAM_CODEC, SkillEffectBellowFilter::operation,
            ::SkillEffectBellowFilter
        )
    }
}