package dev.koji.koko.common.models.effects.filters

import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import dev.koji.koko.common.models.effects.AbstractSkillEffectFilter
import dev.koji.koko.common.models.effects.Filters
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import net.minecraft.world.entity.ai.attributes.AttributeModifier

class BellowSkillEffectFilter(
    val level: Int, val value: Double, val operation: AttributeModifier.Operation
): AbstractSkillEffectFilter() {
    override val type: String = Filters.BELLOW

    override fun apply(level: Int): Boolean = (level <= this.level)

    companion object {
        val CODEC: MapCodec<BellowSkillEffectFilter> = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                Codec.INT.fieldOf("level").forGetter(BellowSkillEffectFilter::level),
                Codec.DOUBLE.fieldOf("value").forGetter(BellowSkillEffectFilter::value),
                AttributeModifier.Operation.CODEC.fieldOf("operation").forGetter(BellowSkillEffectFilter::operation)
            ).apply(instance, ::BellowSkillEffectFilter)
        }

        val STREAM_CODEC: StreamCodec<RegistryFriendlyByteBuf, BellowSkillEffectFilter> = StreamCodec.composite(
            ByteBufCodecs.INT, BellowSkillEffectFilter::level,
            ByteBufCodecs.DOUBLE, BellowSkillEffectFilter::value,
            AttributeModifier.Operation.STREAM_CODEC, BellowSkillEffectFilter::operation,
            ::BellowSkillEffectFilter
        )
    }
}