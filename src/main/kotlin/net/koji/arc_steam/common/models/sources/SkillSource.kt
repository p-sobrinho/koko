package net.koji.arc_steam.common.models.sources

import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import net.koji.arc_steam.common.models.sources.blocks.BlockBreakSource
import net.koji.arc_steam.common.models.sources.blocks.BlockInteractSource
import net.koji.arc_steam.common.models.sources.blocks.BlockPlaceSource
import net.koji.arc_steam.common.models.sources.entities.EntityInteractSource
import net.koji.arc_steam.common.models.sources.entities.EntityKillSource
import net.koji.arc_steam.common.models.sources.player.PlayerDamagedSource
import net.koji.arc_steam.common.models.sources.player.PlayerDiedSource
import net.koji.arc_steam.common.models.sources.player.PlayerJumpSource
import net.koji.arc_steam.common.models.sources.player.PlayerRunSource
import net.koji.arc_steam.common.models.sources.player.PlayerStackDropSource
import net.koji.arc_steam.common.models.sources.player.PlayerTradeSource
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec

abstract class SkillSource {
    abstract val filters: List<SkillSourceFilter>
    abstract val type: String

    companion object {
        private val mapper = mapOf<String, MapCodec<out SkillSource>>(
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

        val CODEC: Codec<SkillSource> = Codec.STRING.dispatch(
            { source -> source.type },
            { type -> mapper.getOrElse(type) { throw IllegalArgumentException("$type is not supported") } }
        )

        val STREAM_CODEC: StreamCodec<RegistryFriendlyByteBuf, SkillSource> =
            StreamCodec.of(
                { buf, source ->
                    ByteBufCodecs.STRING_UTF8.encode(buf, source.type)
                    when (source) {
                        is BlockBreakSource -> BlockBreakSource.STREAM_CODEC.encode(buf, source)
                        else -> throw IllegalArgumentException("Illegal source type: $source")
                    }
                },
                { buf ->
                    val type = ByteBufCodecs.STRING_UTF8.decode(buf)
                    val codec = STREAM_CODECS[type]
                        ?: throw IllegalArgumentException("Illegal source type: $type")

                    codec.decode(buf)
                }
            )

        private val STREAM_CODECS = mapOf(
            BlockBreakSource.TYPE to BlockBreakSource.STREAM_CODEC,
        )
    }
}