package dev.koji.skillforge.common.models.effects

import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import dev.koji.skillforge.common.models.effects.filters.SkillEffectAboveFilter
import dev.koji.skillforge.common.models.effects.filters.SkillEffectBellowFilter
import dev.koji.skillforge.common.models.effects.filters.SkillEffectRangeFilter
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import kotlin.String

abstract class AbstractSkillEffectFilter {
    abstract val type: String

    abstract fun apply(level: Int): Boolean

    companion object {
        val CODEC: Codec<AbstractSkillEffectFilter> = Codec.STRING.dispatch(
            { source -> source.type },
            { type -> codecsMapper[type] ?: throw IllegalArgumentException("$type is not supported") }
        )

        val STREAM_CODEC: StreamCodec<RegistryFriendlyByteBuf, AbstractSkillEffectFilter> =
            StreamCodec.of(
                { buf, source ->
                    val type = source.type

                    ByteBufCodecs.STRING_UTF8.encode(buf, source.type)

                    val streamCodec = streamCodecsMapper[type]
                        ?: throw IllegalArgumentException("$type is not supported")

                    (streamCodec as StreamCodec<RegistryFriendlyByteBuf, AbstractSkillEffectFilter>).encode(buf, source)
                },
                { buf ->
                    val type = ByteBufCodecs.STRING_UTF8.decode(buf)
                    val codec = streamCodecsMapper[type] ?: throw IllegalArgumentException("Illegal source type: $type")

                    codec.decode(buf)
                }
            )

        val codecsMapper = mapOf<String, MapCodec<out AbstractSkillEffectFilter>>(
            SkillEffectAboveFilter.TYPE to SkillEffectAboveFilter.CODEC,
            SkillEffectRangeFilter.TYPE to SkillEffectRangeFilter.CODEC,
            SkillEffectBellowFilter.TYPE to SkillEffectBellowFilter.CODEC
        )

        val streamCodecsMapper = mapOf(
            SkillEffectAboveFilter.TYPE to SkillEffectAboveFilter.STREAM_CODEC,
            SkillEffectRangeFilter.TYPE to SkillEffectRangeFilter.STREAM_CODEC,
            SkillEffectBellowFilter.TYPE to SkillEffectBellowFilter.STREAM_CODEC
        )
    }
}