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

class AboveSkillEffectFilter(
    val level: Int, val value: Double, val operation: AttributeModifier.Operation
): AbstractSkillEffectFilter() {
    override val type: String = Filters.ABOVE

    override fun apply(level: Int): Boolean = (level >= this.level)

    companion object {
        val CODEC: MapCodec<AboveSkillEffectFilter> = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                Codec.INT.fieldOf("level").forGetter(AboveSkillEffectFilter::level),
                Codec.DOUBLE.fieldOf("value").forGetter(AboveSkillEffectFilter::value),
                AttributeModifier.Operation.CODEC.fieldOf("operation").forGetter(AboveSkillEffectFilter::operation)
            ).apply(instance, ::AboveSkillEffectFilter)
        }

        val STREAM_CODEC: StreamCodec<RegistryFriendlyByteBuf, AboveSkillEffectFilter> = StreamCodec.composite(
            ByteBufCodecs.INT, AboveSkillEffectFilter::level,
            ByteBufCodecs.DOUBLE, AboveSkillEffectFilter::value,
            AttributeModifier.Operation.STREAM_CODEC, AboveSkillEffectFilter::operation,
            ::AboveSkillEffectFilter
        )
    }
}