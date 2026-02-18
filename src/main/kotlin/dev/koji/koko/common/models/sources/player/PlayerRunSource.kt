package dev.koji.koko.common.models.sources.player

import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import dev.koji.koko.common.models.sources.AbstractSkillSource
import dev.koji.koko.common.models.sources.DefaultSources
import dev.koji.koko.common.models.sources.SkillSourceFilter
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec

class PlayerRunSource(
    override val filters: List<SkillSourceFilter>
) : AbstractSkillSource() {
    override val type: String = DefaultSources.PLAYER_RUN
    companion object {
        val CODEC: MapCodec<PlayerRunSource> = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                SkillSourceFilter.CODEC.listOf().fieldOf("filters").forGetter(PlayerRunSource::filters)
            ).apply(instance, ::PlayerRunSource)
        }

        val STREAM_CODEC: StreamCodec<RegistryFriendlyByteBuf, PlayerRunSource> = StreamCodec.composite(
            SkillSourceFilter.STREAM_CODEC.apply(ByteBufCodecs.list()), PlayerRunSource::filters,
            ::PlayerRunSource
        )
    }
}