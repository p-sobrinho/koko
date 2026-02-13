package net.koji.arc_steam.common.models.sources.blocks

import com.mojang.serialization.codecs.RecordCodecBuilder
import net.koji.arc_steam.common.models.sources.SkillSourceFilter
import net.koji.arc_steam.common.models.sources.SkillSource
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec

class BlockInteractSource(
    override val filters: List<SkillSourceFilter>
) : SkillSource() {
    override val type: String = TYPE
    companion object {
        const val TYPE = "block/interact"
        val CODEC = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                SkillSourceFilter.CODEC.listOf().fieldOf("filters").forGetter(BlockInteractSource::filters)
            ).apply(instance, ::BlockInteractSource)
        }

        val STREAM_CODEC = StreamCodec.composite(
            SkillSourceFilter.STREAM_CODEC.apply(ByteBufCodecs.list()), BlockInteractSource::filters,
            ::BlockInteractSource
        )
    }
}