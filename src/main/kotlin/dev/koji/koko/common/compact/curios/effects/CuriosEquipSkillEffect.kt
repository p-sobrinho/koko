package dev.koji.koko.common.compact.curios.effects

import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import dev.koji.koko.common.SkillsHandler
import dev.koji.koko.common.compact.curios.CuriosSources
import dev.koji.koko.common.events.PlayerEventHandler
import dev.koji.koko.common.models.effects.AbstractSkillEffect
import dev.koji.koko.common.models.effects.AbstractSkillEffectFilter
import dev.koji.koko.common.models.effects.Effects
import dev.koji.koko.common.models.effects.filters.BlockedSkillEffectFilter
import dev.koji.koko.common.models.effects.player.ArmorEquipSkillEffect
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import net.minecraft.world.entity.player.Player

class CuriosEquipSkillEffect(
    val curio: String,
    val filter: AbstractSkillEffectFilter
) : AbstractSkillEffect() {
    override val type: String = CuriosSources.PLAYER_CURIOUS_USE

    override fun doAnyApplies(level: Int): AbstractSkillEffectFilter? {
        if (filter !is BlockedSkillEffectFilter)
            throw IllegalArgumentException("${this::class.simpleName} only supports ${BlockedSkillEffectFilter::class.simpleName}")

        return filter.takeIf { it.apply(level) }
    }

    override fun apply(applier: SkillsHandler.SkillEffectApplier, player: Player) {
        PlayerEventHandler.addBlockedItem(
            player.uuid, SkillsHandler.safeParseResource(curio), PlayerEventHandler.BlockScope.CURIOS
        )
    }

    override fun unApply(
        applier: SkillsHandler.SkillEffectApplier,
        player: Player
    ) {
        PlayerEventHandler.removeBlockedItem(
            player.uuid, SkillsHandler.safeParseResource(curio), PlayerEventHandler.BlockScope.CURIOS
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