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

class EntityKillSource(
    override val filters: List<SkillSourceFilter>,
    override val alwaysApply: Boolean
) : AbstractSkillSource() {
    override val type: String = Sources.ENTITY_KILL
    companion object {
        val CODEC: MapCodec<EntityKillSource> = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                SkillSourceFilter.CODEC.listOf().fieldOf("filters").forGetter(EntityKillSource::filters),
                Codec.BOOL.optionalFieldOf("alwaysApply", false).forGetter(EntityKillSource::alwaysApply)
            ).apply(instance, ::EntityKillSource)
        }

        val STREAM_CODEC: StreamCodec<RegistryFriendlyByteBuf, EntityKillSource> = StreamCodec.composite(
            SkillSourceFilter.STREAM_CODEC.apply(ByteBufCodecs.list()), EntityKillSource::filters,
            ByteBufCodecs.BOOL, EntityKillSource::alwaysApply,
            ::EntityKillSource
        )
    }
}