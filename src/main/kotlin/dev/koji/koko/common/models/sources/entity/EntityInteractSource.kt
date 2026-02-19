package dev.koji.koko.common.models.sources.entity

import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import dev.koji.koko.common.models.sources.AbstractSkillSource
import dev.koji.koko.common.models.sources.Sources
import dev.koji.koko.common.models.sources.SkillSourceFilter
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec

class EntityInteractSource(
    override val filters: List<SkillSourceFilter>
) : AbstractSkillSource() {
    override val type: String = Sources.ENTITY_INTERACT
    companion object {
        val CODEC: MapCodec<EntityInteractSource> = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                SkillSourceFilter.CODEC.listOf().fieldOf("filters").forGetter(EntityInteractSource::filters)
            ).apply(instance, ::EntityInteractSource)
        }

        val STREAM_CODEC: StreamCodec<RegistryFriendlyByteBuf, EntityInteractSource> = StreamCodec.composite(
            SkillSourceFilter.STREAM_CODEC.apply(ByteBufCodecs.list()), EntityInteractSource::filters,
            ::EntityInteractSource
        )
    }
}