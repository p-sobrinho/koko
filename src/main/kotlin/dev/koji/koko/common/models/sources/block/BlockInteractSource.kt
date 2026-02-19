package dev.koji.koko.common.models.sources.block

import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import dev.koji.koko.common.models.sources.AbstractSkillSource
import dev.koji.koko.common.models.sources.SkillSourceFilter
import dev.koji.koko.common.models.sources.Sources
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec

class BlockInteractSource(
    override val filters: List<SkillSourceFilter>,
    override val alwaysApply: Boolean
) : AbstractSkillSource() {
    override val type: String = Sources.BLOCK_INTERACT
    companion object {
        val CODEC: MapCodec<BlockInteractSource> = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                SkillSourceFilter.CODEC.listOf().fieldOf("filters").forGetter(BlockInteractSource::filters),
                Codec.BOOL.optionalFieldOf("alwaysApply", false).forGetter(BlockInteractSource::alwaysApply)
            ).apply(instance, ::BlockInteractSource)
        }

        val STREAM_CODEC: StreamCodec<RegistryFriendlyByteBuf, BlockInteractSource> = StreamCodec.composite(
            SkillSourceFilter.STREAM_CODEC.apply(ByteBufCodecs.list()), BlockInteractSource::filters,
            ByteBufCodecs.BOOL, BlockInteractSource::alwaysApply,
            ::BlockInteractSource
        )
    }
}