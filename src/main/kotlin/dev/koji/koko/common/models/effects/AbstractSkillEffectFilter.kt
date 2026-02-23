package dev.koji.koko.common.models.effects

import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import dev.koji.koko.common.models.effects.filters.AboveSkillEffectFilter
import dev.koji.koko.common.models.effects.filters.BellowSkillEffectFilter
import dev.koji.koko.common.models.effects.filters.BlockedSkillEffectFilter
import dev.koji.koko.common.models.effects.filters.RangeSkillEffectFilter
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec

abstract class AbstractSkillEffectFilter {
    abstract val type: String

    abstract fun apply(level: Int): Boolean

    companion object {
        val CODEC: Codec<AbstractSkillEffectFilter> = Codec.STRING.dispatch(
            { source -> source.type },
            { type -> codecMapper[type] ?: throw IllegalArgumentException("$type is not supported") }
        )

        val STREAM_CODEC: StreamCodec<RegistryFriendlyByteBuf, AbstractSkillEffectFilter> =
            StreamCodec.of(
                { buf, source ->
                    val type = source.type

                    ByteBufCodecs.STRING_UTF8.encode(buf, source.type)

                    val streamCodec = streamMapper[type]
                        ?: throw IllegalArgumentException("$type is not supported")

                    (streamCodec as StreamCodec<RegistryFriendlyByteBuf, AbstractSkillEffectFilter>).encode(buf, source)
                },
                { buf ->
                    val type = ByteBufCodecs.STRING_UTF8.decode(buf)
                    val codec = streamMapper[type] ?: throw IllegalArgumentException("Illegal source type: $type")

                    codec.decode(buf)
                }
            )

        val codecMapper = mutableMapOf<String, MapCodec<out AbstractSkillEffectFilter>>(
            Filters.ABOVE to AboveSkillEffectFilter.CODEC,
            Filters.RANGE to RangeSkillEffectFilter.CODEC,
            Filters.BELLOW to BellowSkillEffectFilter.CODEC,
            Filters.BLOCKED to BlockedSkillEffectFilter.CODEC
        )

        val streamMapper = mutableMapOf<String, StreamCodec<RegistryFriendlyByteBuf, out AbstractSkillEffectFilter>>(
            Filters.ABOVE to AboveSkillEffectFilter.STREAM_CODEC,
            Filters.RANGE to RangeSkillEffectFilter.STREAM_CODEC,
            Filters.BELLOW to BellowSkillEffectFilter.STREAM_CODEC,
            Filters.BLOCKED to BlockedSkillEffectFilter.STREAM_CODEC
        )

        fun registerCodec(path: String, codec: MapCodec<out AbstractSkillEffectFilter>) { codecMapper[path] = codec }

        fun registerStream(path: String, streamCodec: StreamCodec<RegistryFriendlyByteBuf, out AbstractSkillEffectFilter>) {
           streamMapper[path] = streamCodec
        }
    }
}