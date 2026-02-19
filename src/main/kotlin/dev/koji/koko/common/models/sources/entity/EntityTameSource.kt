package dev.koji.koko.common.models.sources.entity

import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import dev.koji.koko.common.models.sources.AbstractSkillSource
import dev.koji.koko.common.models.sources.SkillSourceFilter
import dev.koji.koko.common.models.sources.Sources
import dev.koji.koko.common.models.sources.block.BlockBreakSource
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec

class EntityTameSource(
    override val filters: List<SkillSourceFilter>,
    override val alwaysApply: Boolean
) : AbstractSkillSource() {
    override val type: String = Sources.ENTITY_TAME

    companion object {
        val CODEC: MapCodec<EntityTameSource> = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                SkillSourceFilter.CODEC.listOf().fieldOf("filters").forGetter(EntityTameSource::filters),
                Codec.BOOL.optionalFieldOf("alwaysApply", false).forGetter(EntityTameSource::alwaysApply)
            ).apply(instance, ::EntityTameSource)
        }

        val STREAM_CODEC: StreamCodec<RegistryFriendlyByteBuf, EntityTameSource> = StreamCodec.composite(
            SkillSourceFilter.STREAM_CODEC.apply(ByteBufCodecs.list()), EntityTameSource::filters,
            ByteBufCodecs.BOOL, EntityTameSource::alwaysApply,
            ::EntityTameSource
        )
    }
}