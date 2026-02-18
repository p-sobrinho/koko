package dev.koji.koko.common.models.effects.filters

import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import dev.koji.koko.common.models.effects.AbstractSkillEffectFilter
import io.netty.buffer.ByteBuf
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import net.minecraft.world.entity.ai.attributes.AttributeModifier

class RangeSkillEffectFilter(
    val from: Int, val to: Int, val value: Double, val operation: AttributeModifier.Operation
): AbstractSkillEffectFilter() {
    override val type: String = TYPE

    override fun apply(level: Int): Boolean = (level in from..to)

    companion object {
        const val TYPE = "filter/range"

        val CODEC: MapCodec<RangeSkillEffectFilter> = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                Codec.INT.fieldOf("from").forGetter(RangeSkillEffectFilter::from),
                Codec.INT.fieldOf("to").forGetter(RangeSkillEffectFilter::to),
                Codec.DOUBLE.fieldOf("value").forGetter(RangeSkillEffectFilter::value),
                AttributeModifier.Operation.CODEC.fieldOf("operation").forGetter(RangeSkillEffectFilter::operation)
            ).apply(instance, ::RangeSkillEffectFilter)
        }

        val STREAM_CODEC: StreamCodec<RegistryFriendlyByteBuf, RangeSkillEffectFilter> = StreamCodec.composite(
            ByteBufCodecs.INT, RangeSkillEffectFilter::from,
            ByteBufCodecs.INT, RangeSkillEffectFilter::to,
            ByteBufCodecs.DOUBLE, RangeSkillEffectFilter::value,
            AttributeModifier.Operation.STREAM_CODEC, RangeSkillEffectFilter::operation,
            ::RangeSkillEffectFilter
        )
    }
}