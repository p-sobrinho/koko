package dev.koji.koko.common.compact.ironspells.sources

import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import dev.koji.koko.common.compact.ironspells.IronSpellsCompact
import dev.koji.koko.common.models.sources.AbstractSkillSource
import dev.koji.koko.common.models.sources.SkillSourceFilter
import dev.koji.koko.common.models.sources.player.PlayerAttackedSource
import dev.koji.koko.common.models.sources.player.PlayerConsumedSource
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec

class SpellInscribeSource(
    override val filters: List<SkillSourceFilter>, override val alwaysApply: Boolean, override val alwaysValue: Double
) : AbstractSkillSource() {
    override val type: String = IronSpellsCompact.Sources.PLAYER_SPELL_INSCRIBE

    companion object {
        val CODEC: MapCodec<SpellInscribeSource> = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                SkillSourceFilter.CODEC.listOf().fieldOf("filters").forGetter { it.filters },
                Codec.BOOL.optionalFieldOf("alwaysApply", false).forGetter { it.alwaysApply },
                Codec.DOUBLE.optionalFieldOf("alwaysValue", 0.0).forGetter { it.alwaysValue }
            ).apply(instance, ::SpellInscribeSource)
        }

        val STREAM_CODEC: StreamCodec<RegistryFriendlyByteBuf, SpellInscribeSource> = StreamCodec.composite(
            SkillSourceFilter.STREAM_CODEC.apply(ByteBufCodecs.list()), { it.filters },
            ByteBufCodecs.BOOL, { it.alwaysApply },
            ByteBufCodecs.DOUBLE, { it.alwaysValue },
            ::SpellInscribeSource
        )
    }
}