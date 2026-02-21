package dev.koji.koko.common.models.sources

import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import dev.koji.koko.common.models.sources.block.BlockBreakSource
import dev.koji.koko.common.models.sources.block.BlockInteractSource
import dev.koji.koko.common.models.sources.block.BlockPlaceSource
import dev.koji.koko.common.models.sources.entity.EntityInteractSource
import dev.koji.koko.common.models.sources.entity.EntityKillSource
import dev.koji.koko.common.models.sources.entity.EntityTameSource
import dev.koji.koko.common.models.sources.player.*
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec

abstract class AbstractSkillSource {
    abstract val filters: List<SkillSourceFilter>
    abstract val alwaysApply: Boolean
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

                    val streamCodec = streamMapper[type]
                        ?: throw IllegalArgumentException("$type is not supported")

                    (streamCodec as StreamCodec<RegistryFriendlyByteBuf, AbstractSkillSource>).encode(buf, source)
                },
                { buf ->
                    val type = ByteBufCodecs.STRING_UTF8.decode(buf)
                    val codec = streamMapper[type]
                        ?: throw IllegalArgumentException("Illegal source type: $type")

                    codec.decode(buf)
                }
            )

        val codecsMapper = mutableMapOf<String, MapCodec<out AbstractSkillSource>>(
            Sources.BLOCK_PLACE to BlockPlaceSource.CODEC,
            Sources.BLOCK_INTERACT to BlockBreakSource.CODEC,
            Sources.BLOCK_BREAK to BlockInteractSource.CODEC,
            Sources.ENTITY_INTERACT to EntityInteractSource.CODEC,
            Sources.ENTITY_TAME to EntityTameSource.CODEC,
            Sources.ENTITY_KILL to EntityKillSource.CODEC,
            Sources.PLAYER_RUN to PlayerRunSource.CODEC,
            Sources.PLAYER_JUMP to PlayerJumpSource.CODEC,
            Sources.PLAYER_CRAFTED to PlayerCraftedSource.CODEC,
            Sources.PLAYER_TRADE to PlayerTradeSource.CODEC,
            Sources.PLAYER_STACK_DROP to PlayerStackDropSource.CODEC,
            Sources.PLAYER_DAMAGED to PlayerDamagedSource.CODEC,
            Sources.PLAYER_DIED to PlayerDiedSource.CODEC
        )

        val streamMapper = mutableMapOf<String, StreamCodec<RegistryFriendlyByteBuf, out AbstractSkillSource>>(
            Sources.BLOCK_PLACE to BlockPlaceSource.STREAM_CODEC,
            Sources.BLOCK_INTERACT to BlockBreakSource.STREAM_CODEC,
            Sources.BLOCK_BREAK to BlockInteractSource.STREAM_CODEC,
            Sources.ENTITY_INTERACT to EntityInteractSource.STREAM_CODEC,
            Sources.ENTITY_TAME to EntityTameSource.STREAM_CODEC,
            Sources.ENTITY_KILL to EntityKillSource.STREAM_CODEC,
            Sources.PLAYER_RUN to PlayerRunSource.STREAM_CODEC,
            Sources.PLAYER_JUMP to PlayerJumpSource.STREAM_CODEC,
            Sources.PLAYER_CRAFTED to PlayerCraftedSource.STREAM_CODEC,
            Sources.PLAYER_TRADE to PlayerTradeSource.STREAM_CODEC,
            Sources.PLAYER_STACK_DROP to PlayerStackDropSource.STREAM_CODEC,
            Sources.PLAYER_DAMAGED to PlayerDamagedSource.STREAM_CODEC,
            Sources.PLAYER_DIED to PlayerDiedSource.STREAM_CODEC
        )
    }
}