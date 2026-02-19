package dev.koji.koko.common.models.sources.player

import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import dev.koji.koko.common.models.sources.AbstractSkillSource
import dev.koji.koko.common.models.sources.SkillSourceFilter
import dev.koji.koko.common.models.sources.Sources
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec

class PlayerItemUseSource(
    override val filters: List<SkillSourceFilter>,
    override val alwaysApply: Boolean
) : AbstractSkillSource() {
    override val type: String = Sources.PLAYER_ITEM_USE

    companion object {
        val CODEC: MapCodec<PlayerItemUseSource> = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                SkillSourceFilter.CODEC.listOf().fieldOf("filters").forGetter(PlayerItemUseSource::filters),
                Codec.BOOL.optionalFieldOf("alwaysApply", false).forGetter(PlayerItemUseSource::alwaysApply)
            ).apply(instance, ::PlayerItemUseSource)
        }

        val STREAM_CODEC: StreamCodec<RegistryFriendlyByteBuf, PlayerItemUseSource> = StreamCodec.composite(
            SkillSourceFilter.STREAM_CODEC.apply(ByteBufCodecs.list()), PlayerItemUseSource::filters,
            ByteBufCodecs.BOOL, PlayerItemUseSource::alwaysApply,
            ::PlayerItemUseSource
        )
    }
}