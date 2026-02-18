package dev.koji.koko.common.models.sources.player

import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import dev.koji.koko.common.models.sources.AbstractSkillSource
import dev.koji.koko.common.models.sources.DefaultSources
import dev.koji.koko.common.models.sources.SkillSourceFilter
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec

class PlayerDamagedSource(
    override val filters: List<SkillSourceFilter>
) : AbstractSkillSource() {
    override val type: String = DefaultSources.PLAYER_DAMAGED
    companion object {
        val CODEC: MapCodec<PlayerDamagedSource> = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                SkillSourceFilter.CODEC.listOf().fieldOf("filters").forGetter(PlayerDamagedSource::filters)
            ).apply(instance, ::PlayerDamagedSource)
        }

        val STREAM_CODEC: StreamCodec<RegistryFriendlyByteBuf, PlayerDamagedSource> = StreamCodec.composite(
            SkillSourceFilter.STREAM_CODEC.apply(ByteBufCodecs.list()), PlayerDamagedSource::filters,
            ::PlayerDamagedSource
        )
    }
}