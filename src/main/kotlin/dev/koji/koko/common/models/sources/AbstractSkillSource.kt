package dev.koji.koko.common.models.sources

import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import dev.koji.koko.common.models.sources.block.BlockBreakSource
import dev.koji.koko.common.models.sources.block.BlockInteractSource
import dev.koji.koko.common.models.sources.block.BlockPlaceSource
import dev.koji.koko.common.models.sources.entity.EntityInteractSource
import dev.koji.koko.common.models.sources.entity.EntityKillSource
import dev.koji.koko.common.models.sources.player.*
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec

abstract class AbstractSkillSource {
    abstract val filters: List<SkillSourceFilter>
    abstract val type: String

    companion object {
        val CODEC: Codec<AbstractSkillSource> = Codec.STRING.dispatch(
            { source -> source.type },
            { type -> codecsMapper.getOrElse(type) { throw IllegalArgumentException("$type is not supported") } }
        )

        val STREAM_CODEC: StreamCodec<RegistryFriendlyByteBuf, AbstractSkillSource> =
            StreamCodec.of(
                { buf, source ->
                    val type = source.type
                    ByteBufCodecs.STRING_UTF8.encode(buf, type)

                    val streamCodec = streamCodecsMapper[type]
                        ?: throw IllegalArgumentException("$type is not supported")

                    (streamCodec as StreamCodec<RegistryFriendlyByteBuf, AbstractSkillSource>).encode(buf, source)
                },
                { buf ->
                    val type = ByteBufCodecs.STRING_UTF8.decode(buf)
                    val codec = streamCodecsMapper[type]
                        ?: throw IllegalArgumentException("Illegal source type: $type")

                    codec.decode(buf)
                }
            )

        val codecsMapper = mapOf<String, MapCodec<out AbstractSkillSource>>(
            BlockPlaceSource.TYPE to BlockPlaceSource.CODEC,
            BlockBreakSource.TYPE to BlockBreakSource.CODEC,
            BlockInteractSource.TYPE to BlockInteractSource.CODEC,
            EntityInteractSource.TYPE to EntityInteractSource.CODEC,
            EntityKillSource.TYPE to EntityKillSource.CODEC,
            PlayerRunSource.TYPE to PlayerRunSource.CODEC,
            PlayerJumpSource.TYPE to PlayerJumpSource.CODEC,
            PlayerTradeSource.TYPE to PlayerTradeSource.CODEC,
            PlayerStackDropSource.TYPE to PlayerStackDropSource.CODEC,
            PlayerDamagedSource.TYPE to PlayerDamagedSource.CODEC,
            PlayerDiedSource.TYPE to PlayerDiedSource.CODEC
        )

        val streamCodecsMapper = mapOf<String, StreamCodec<RegistryFriendlyByteBuf, out AbstractSkillSource>>(
            BlockPlaceSource.TYPE to BlockPlaceSource.STREAM_CODEC,
            BlockBreakSource.TYPE to BlockBreakSource.STREAM_CODEC,
            BlockInteractSource.TYPE to BlockInteractSource.STREAM_CODEC,
            EntityInteractSource.TYPE to EntityInteractSource.STREAM_CODEC,
            EntityKillSource.TYPE to EntityKillSource.STREAM_CODEC,
            PlayerRunSource.TYPE to PlayerRunSource.STREAM_CODEC,
            PlayerJumpSource.TYPE to PlayerJumpSource.STREAM_CODEC,
            PlayerTradeSource.TYPE to PlayerTradeSource.STREAM_CODEC,
            PlayerStackDropSource.TYPE to PlayerStackDropSource.STREAM_CODEC,
            PlayerDamagedSource.TYPE to PlayerDamagedSource.STREAM_CODEC,
            PlayerDiedSource.TYPE to PlayerDiedSource.STREAM_CODEC
        )
    }
}