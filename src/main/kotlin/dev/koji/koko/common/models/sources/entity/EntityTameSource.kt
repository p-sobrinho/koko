package dev.koji.koko.common.models.sources.entity

import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import dev.koji.koko.common.models.sources.AbstractSkillSource
import dev.koji.koko.common.models.sources.SkillSourceFilter
import dev.koji.koko.common.models.sources.Sources
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec

class EntityTameSource(
    override val filters: List<SkillSourceFilter>
) : AbstractSkillSource() {
    override val type: String = Sources.ENTITY_TAME

    companion object {
        val CODEC: MapCodec<EntityTameSource> = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                SkillSourceFilter.CODEC.listOf().fieldOf("filters").forGetter(EntityTameSource::filters)
            ).apply(instance, ::EntityTameSource)
        }

        val STREAM_CODEC: StreamCodec<RegistryFriendlyByteBuf, EntityTameSource> = StreamCodec.composite(
            SkillSourceFilter.STREAM_CODEC.apply(ByteBufCodecs.list()), EntityTameSource::filters,
            ::EntityTameSource
        )
    }
}