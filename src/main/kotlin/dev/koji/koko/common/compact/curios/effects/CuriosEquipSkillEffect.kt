package dev.koji.koko.common.compact.curios.effects

import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import dev.koji.koko.common.SkillsHandler
import dev.koji.koko.common.compact.curios.CuriosCompact
import dev.koji.koko.common.events.PlayerEventHandler
import dev.koji.koko.common.helpers.MainHelper
import dev.koji.koko.common.models.effects.AbstractSkillEffect
import dev.koji.koko.common.models.effects.AbstractSkillEffectFilter
import dev.koji.koko.common.models.effects.filters.BlockedSkillEffectFilter
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import net.minecraft.world.entity.player.Player

class CuriosEquipSkillEffect(
    val curio: String,
    val filter: AbstractSkillEffectFilter
) : AbstractSkillEffect() {
    override val type: String = CuriosCompact.Effects.PLAYER_CURIOS_EQUIP

    override fun doAnyApplies(level: Int): AbstractSkillEffectFilter? {
        if (filter !is BlockedSkillEffectFilter)
            throw IllegalArgumentException("${this::class.simpleName} only supports ${BlockedSkillEffectFilter::class.simpleName}")

        return filter.takeIf { it.apply(level) }
    }

    override fun apply(applier: SkillsHandler.SkillEffectApplier, player: Player) {
        PlayerEventHandler.addBlockedItem(
            player.uuid, MainHelper.safeParseResource(curio), PlayerEventHandler.PlayerBlockScope.CURIOS
        )
    }

    override fun unApply(
        applier: SkillsHandler.SkillEffectApplier,
        player: Player
    ) {
        PlayerEventHandler.removeBlockedItem(
            player.uuid, MainHelper.safeParseResource(curio), PlayerEventHandler.PlayerBlockScope.CURIOS
        )
    }

    companion object {
        val CODEC: MapCodec<CuriosEquipSkillEffect> = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                Codec.STRING.fieldOf("curio").forGetter(CuriosEquipSkillEffect::curio),
                AbstractSkillEffectFilter.CODEC.fieldOf("filter").forGetter(CuriosEquipSkillEffect::filter)
            ).apply(instance, ::CuriosEquipSkillEffect)
        }

        val STREAM_CODEC: StreamCodec<RegistryFriendlyByteBuf, CuriosEquipSkillEffect> = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, CuriosEquipSkillEffect::curio,
            AbstractSkillEffectFilter.STREAM_CODEC, CuriosEquipSkillEffect::filter,
            ::CuriosEquipSkillEffect
        )
    }
}