package dev.koji.skillforge.common.models.sources.block

import com.mojang.serialization.codecs.RecordCodecBuilder
import dev.koji.skillforge.common.models.sources.SkillSourceFilter
import dev.koji.skillforge.common.models.sources.AbstractSkillSource
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec

class BlockInteractSource(
    override val filters: List<SkillSourceFilter>
) : AbstractSkillSource() {
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