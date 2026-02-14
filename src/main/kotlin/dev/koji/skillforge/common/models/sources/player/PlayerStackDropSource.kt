package dev.koji.skillforge.common.models.sources.player

import com.mojang.serialization.codecs.RecordCodecBuilder
import dev.koji.skillforge.common.models.sources.SkillSourceFilter
import dev.koji.skillforge.common.models.sources.AbstractSkillSource
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec

class PlayerStackDropSource(
    override val filters: List<SkillSourceFilter>
) : AbstractSkillSource() {
    override val type: String = TYPE
    companion object {
        const val TYPE = "player/stack_drop"
        val CODEC = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                SkillSourceFilter.CODEC.listOf().fieldOf("filters").forGetter(PlayerStackDropSource::filters)
            ).apply(instance, ::PlayerStackDropSource)
        }

        val STREAM_CODEC = StreamCodec.composite(
            SkillSourceFilter.STREAM_CODEC.apply(ByteBufCodecs.list()), PlayerStackDropSource::filters,
            ::PlayerStackDropSource
        )
    }
}