package dev.koji.koko.common.models.sources.player

import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import dev.koji.koko.common.models.sources.AbstractSkillSource
import dev.koji.koko.common.models.sources.DefaultSources
import dev.koji.koko.common.models.sources.SkillSourceFilter
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec

class PlayerStackDropSource(
    override val filters: List<SkillSourceFilter>
) : AbstractSkillSource() {
    override val type: String = DefaultSources.PLAYER_STACK_DROP
    companion object {
        val CODEC: MapCodec<PlayerStackDropSource> = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                SkillSourceFilter.CODEC.listOf().fieldOf("filters").forGetter(PlayerStackDropSource::filters)
            ).apply(instance, ::PlayerStackDropSource)
        }

        val STREAM_CODEC: StreamCodec<RegistryFriendlyByteBuf, PlayerStackDropSource> = StreamCodec.composite(
            SkillSourceFilter.STREAM_CODEC.apply(ByteBufCodecs.list()), PlayerStackDropSource::filters,
            ::PlayerStackDropSource
        )
    }
}