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

class PlayerJumpSource(
    override val filters: List<SkillSourceFilter>, override val alwaysApply: Boolean, override val alwaysValue: Double
) : AbstractSkillSource() {
    override val type: String = Sources.PLAYER_JUMP
    companion object {
        val CODEC: MapCodec<PlayerJumpSource> = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                SkillSourceFilter.CODEC.listOf().fieldOf("filters").forGetter { it.filters },
                Codec.BOOL.optionalFieldOf("alwaysApply", false).forGetter { it.alwaysApply },
                Codec.DOUBLE.optionalFieldOf("alwaysValue", 0.0).forGetter { it.alwaysValue }
            ).apply(instance, ::PlayerJumpSource)
        }

        val STREAM_CODEC: StreamCodec<RegistryFriendlyByteBuf, PlayerJumpSource> = StreamCodec.composite(
            SkillSourceFilter.STREAM_CODEC.apply(ByteBufCodecs.list()), { it.filters },
            ByteBufCodecs.BOOL, { it.alwaysApply },
            ByteBufCodecs.DOUBLE, { it.alwaysValue },
            ::PlayerJumpSource
        )
    }
}