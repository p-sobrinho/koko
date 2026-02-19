package dev.koji.koko.common.models.sources.player

import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import dev.koji.koko.common.models.sources.AbstractSkillSource
import dev.koji.koko.common.models.sources.Sources
import dev.koji.koko.common.models.sources.SkillSourceFilter
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec

class PlayerDamagedSource(
    override val filters: List<SkillSourceFilter>,
    override val alwaysApply: Boolean
) : AbstractSkillSource() {
    override val type: String = Sources.PLAYER_DAMAGED
    companion object {
        val CODEC: MapCodec<PlayerDamagedSource> = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                SkillSourceFilter.CODEC.listOf().fieldOf("filters").forGetter(PlayerDamagedSource::filters),
                Codec.BOOL.optionalFieldOf("alwaysApply", false).forGetter(PlayerDamagedSource::alwaysApply)
            ).apply(instance, ::PlayerDamagedSource)
        }

        val STREAM_CODEC: StreamCodec<RegistryFriendlyByteBuf, PlayerDamagedSource> = StreamCodec.composite(
            SkillSourceFilter.STREAM_CODEC.apply(ByteBufCodecs.list()), PlayerDamagedSource::filters,
            ByteBufCodecs.BOOL, PlayerDamagedSource::alwaysApply,
            ::PlayerDamagedSource
        )
    }
}