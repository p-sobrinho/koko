package net.koji.arc_steam.common.models.sources.player

import com.mojang.serialization.codecs.RecordCodecBuilder
import net.koji.arc_steam.common.models.sources.SkillSourceFilter
import net.koji.arc_steam.common.models.sources.AbstractSkillSource
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec

class PlayerTradeSource(
    override val filters: List<SkillSourceFilter>
) : AbstractSkillSource() {
    override val type: String = TYPE
    companion object {
        const val TYPE = "player/trade"
        val CODEC = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                SkillSourceFilter.CODEC.listOf().fieldOf("filters").forGetter(PlayerTradeSource::filters)
            ).apply(instance, ::PlayerTradeSource)
        }

        val STREAM_CODEC = StreamCodec.composite(
            SkillSourceFilter.STREAM_CODEC.apply(ByteBufCodecs.list()), PlayerTradeSource::filters,
            ::PlayerTradeSource
        )
    }
}