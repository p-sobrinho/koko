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

class PlayerCraftedSource(
    override val filters: List<SkillSourceFilter>,
    override val alwaysApply: Boolean
) : AbstractSkillSource() {
    override val type: String = Sources.PLAYER_CRAFTED

    companion object {
        val CODEC: MapCodec<PlayerCraftedSource> = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                SkillSourceFilter.CODEC.listOf().fieldOf("filters").forGetter(PlayerCraftedSource::filters),
                Codec.BOOL.optionalFieldOf("alwaysApply", false).forGetter(PlayerCraftedSource::alwaysApply)
            ).apply(instance, ::PlayerCraftedSource)
        }

        val STREAM_CODEC: StreamCodec<RegistryFriendlyByteBuf, PlayerCraftedSource> = StreamCodec.composite(
            SkillSourceFilter.STREAM_CODEC.apply(ByteBufCodecs.list()), PlayerCraftedSource::filters,
            ByteBufCodecs.BOOL, PlayerCraftedSource::alwaysApply,
            ::PlayerCraftedSource
        )
    }
}