package dev.koji.koko.common.models.sources.block

import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import dev.koji.koko.common.models.sources.AbstractSkillSource
import dev.koji.koko.common.models.sources.Sources
import dev.koji.koko.common.models.sources.SkillSourceFilter
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec

class BlockInteractSource(
    override val filters: List<SkillSourceFilter>
) : AbstractSkillSource() {
    override val type: String = Sources.BLOCK_INTERACT
    companion object {
        val CODEC: MapCodec<BlockInteractSource> = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                SkillSourceFilter.CODEC.listOf().fieldOf("filters").forGetter(BlockInteractSource::filters)
            ).apply(instance, ::BlockInteractSource)
        }

        val STREAM_CODEC: StreamCodec<RegistryFriendlyByteBuf, BlockInteractSource> = StreamCodec.composite(
            SkillSourceFilter.STREAM_CODEC.apply(ByteBufCodecs.list()), BlockInteractSource::filters,
            ::BlockInteractSource
        )
    }
}