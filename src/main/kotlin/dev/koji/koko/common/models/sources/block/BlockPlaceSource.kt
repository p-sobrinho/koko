package dev.koji.koko.common.models.sources.block

import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import dev.koji.koko.common.models.sources.AbstractSkillSource
import dev.koji.koko.common.models.sources.DefaultSources
import dev.koji.koko.common.models.sources.SkillSourceFilter
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec

class BlockPlaceSource(
    override val filters: List<SkillSourceFilter>
) : AbstractSkillSource() {
    override val type: String = DefaultSources.BLOCK_PLACE
    companion object {
        val CODEC: MapCodec<BlockPlaceSource> = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                SkillSourceFilter.CODEC.listOf().fieldOf("filters").forGetter(BlockPlaceSource::filters)
            ).apply(instance, ::BlockPlaceSource)
        }

        val STREAM_CODEC: StreamCodec<RegistryFriendlyByteBuf, BlockPlaceSource> = StreamCodec.composite(
            SkillSourceFilter.STREAM_CODEC.apply(ByteBufCodecs.list()), BlockPlaceSource::filters,
            ::BlockPlaceSource
        )
    }
}