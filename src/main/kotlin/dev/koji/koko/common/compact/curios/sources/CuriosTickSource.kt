package dev.koji.koko.common.compact.curios.sources

import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import dev.koji.koko.common.compact.curios.CuriosCompact
import dev.koji.koko.common.models.sources.AbstractSkillSource
import dev.koji.koko.common.models.sources.SkillSourceFilter
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec

class CuriosTickSource(
    override val filters: List<SkillSourceFilter>,
    override val alwaysApply: Boolean
) : AbstractSkillSource() {
    override val type: String = CuriosCompact.Sources.PLAYER_CURIOUS_USE

    companion object {
        val CODEC: MapCodec<CuriosTickSource> = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                SkillSourceFilter.CODEC.listOf().fieldOf("filters").forGetter(CuriosTickSource::filters),
                Codec.BOOL.optionalFieldOf("alwaysApply", false).forGetter(CuriosTickSource::alwaysApply)
            ).apply(instance, ::CuriosTickSource)
        }

        val STREAM_CODEC: StreamCodec<RegistryFriendlyByteBuf, CuriosTickSource> = StreamCodec.composite(
            SkillSourceFilter.STREAM_CODEC.apply(ByteBufCodecs.list()), CuriosTickSource::filters,
            ByteBufCodecs.BOOL, CuriosTickSource::alwaysApply,
            ::CuriosTickSource
        )
    }
}