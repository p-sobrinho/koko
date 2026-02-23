package dev.koji.koko.common.compact.ironspells.sources

import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import dev.koji.koko.common.compact.ironspells.IronSpellsCompact
import dev.koji.koko.common.models.sources.AbstractSkillSource
import dev.koji.koko.common.models.sources.SkillSourceFilter
import dev.koji.koko.common.models.sources.player.PlayerAttackedSource
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec

class SpellInscribeSource(
    override val filters: List<SkillSourceFilter>,
    override val alwaysApply: Boolean
) : AbstractSkillSource() {
    override val type: String = IronSpellsCompact.Sources.PLAYER_SPELL_INSCRIBE

    companion object {
        val CODEC: MapCodec<PlayerAttackedSource> = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                SkillSourceFilter.CODEC.listOf().fieldOf("filters").forGetter(PlayerAttackedSource::filters),
                Codec.BOOL.optionalFieldOf("alwaysApply", false).forGetter(PlayerAttackedSource::alwaysApply)
            ).apply(instance, ::PlayerAttackedSource)
        }

        val STREAM_CODEC: StreamCodec<RegistryFriendlyByteBuf, PlayerAttackedSource> = StreamCodec.composite(
            SkillSourceFilter.STREAM_CODEC.apply(ByteBufCodecs.list()), PlayerAttackedSource::filters,
            ByteBufCodecs.BOOL, PlayerAttackedSource::alwaysApply,
            ::PlayerAttackedSource
        )
    }
}