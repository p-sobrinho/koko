package dev.koji.koko.common.models.effects.player

import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import dev.koji.koko.common.SkillsHandler
import dev.koji.koko.common.events.PlayerEventHandler
import dev.koji.koko.common.helpers.MainHelper
import dev.koji.koko.common.models.effects.AbstractSkillEffect
import dev.koji.koko.common.models.effects.AbstractSkillEffectFilter
import dev.koji.koko.common.models.effects.Effects
import dev.koji.koko.common.models.effects.filters.BlockedSkillEffectFilter
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import net.minecraft.world.entity.player.Player

class ItemUseSkillEffect(
    val item: String,
    val filter: AbstractSkillEffectFilter
) : AbstractSkillEffect() {
    override val type: String = Effects.PLAYER_USE

    override fun doAnyApplies(level: Int): AbstractSkillEffectFilter? {
        if (filter !is BlockedSkillEffectFilter)
            throw IllegalArgumentException("${this::class.simpleName} only supports ${BlockedSkillEffectFilter::class.simpleName}")

        return filter.takeIf { it.apply(level) }
    }

    override fun apply(applier: SkillsHandler.SkillEffectApplier, player: Player) {
        PlayerEventHandler.addBlockedItem(
            player.uuid, MainHelper.safeParseResource(item), PlayerEventHandler.PlayerBlockScope.USE
        )
    }

    override fun unApply(
        applier: SkillsHandler.SkillEffectApplier,
        player: Player
    ) {
        PlayerEventHandler.removeBlockedItem(
            player.uuid, MainHelper.safeParseResource(item), PlayerEventHandler.PlayerBlockScope.USE
        )
    }

    companion object {
        val CODEC: MapCodec<ItemUseSkillEffect> = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                Codec.STRING.fieldOf("item").forGetter(ItemUseSkillEffect::item),
                AbstractSkillEffectFilter.CODEC.fieldOf("filter").forGetter(ItemUseSkillEffect::filter)
            ).apply(instance, ::ItemUseSkillEffect)
        }

        val STREAM_CODEC: StreamCodec<RegistryFriendlyByteBuf, ItemUseSkillEffect> = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, ItemUseSkillEffect::item,
            AbstractSkillEffectFilter.STREAM_CODEC, ItemUseSkillEffect::filter,
            ::ItemUseSkillEffect
        )
    }
}