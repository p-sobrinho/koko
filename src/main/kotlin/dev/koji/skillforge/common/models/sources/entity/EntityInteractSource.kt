package dev.koji.skillforge.common.models.sources.entity

import com.mojang.serialization.codecs.RecordCodecBuilder
import dev.koji.skillforge.common.models.sources.SkillSourceFilter
import dev.koji.skillforge.common.models.sources.AbstractSkillSource
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec

class EntityInteractSource(
    override val filters: List<SkillSourceFilter>
) : AbstractSkillSource() {
    override val type: String = TYPE
    companion object {
        const val TYPE = "entity/interact"
        val CODEC = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                SkillSourceFilter.CODEC.listOf().fieldOf("filters").forGetter(EntityInteractSource::filters)
            ).apply(instance, ::EntityInteractSource)
        }

        val STREAM_CODEC = StreamCodec.composite(
            SkillSourceFilter.STREAM_CODEC.apply(ByteBufCodecs.list()), EntityInteractSource::filters,
            ::EntityInteractSource
        )
    }
}