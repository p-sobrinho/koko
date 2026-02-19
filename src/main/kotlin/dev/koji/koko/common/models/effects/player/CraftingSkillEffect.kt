package dev.koji.koko.common.models.effects.player

import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import dev.koji.koko.common.SkillsHandler
import dev.koji.koko.common.events.PlayerEventHandler
import dev.koji.koko.common.models.effects.AbstractSkillEffect
import dev.koji.koko.common.models.effects.AbstractSkillEffectFilter
import dev.koji.koko.common.models.effects.Effects
import dev.koji.koko.common.models.effects.filters.BlockedSkillEffectFilter
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import net.minecraft.world.entity.player.Player

class CraftingSkillEffect(
    val recipe: String,
    val filter: AbstractSkillEffectFilter
) : AbstractSkillEffect() {
    override val type: String = Effects.PLAYER_CRAFT

    override fun doAnyApplies(level: Int): AbstractSkillEffectFilter? {
        if (filter !is BlockedSkillEffectFilter)
            throw IllegalArgumentException("${this::class.simpleName} only supports ${BlockedSkillEffectFilter::class.simpleName}")

        return filter.takeIf { it.apply(level) }
    }

    override fun apply(applier: SkillsHandler.SkillEffectApplier, player: Player) {
        PlayerEventHandler.addBlockedItem(
            player.uuid, SkillsHandler.safeParseResource(recipe), PlayerEventHandler.BlockScope.CRAFT
        )
    }

    override fun unApply(
        applier: SkillsHandler.SkillEffectApplier,
        player: Player
    ) {
        PlayerEventHandler.removeBlockedItem(
            player.uuid, SkillsHandler.safeParseResource(recipe), PlayerEventHandler.BlockScope.CRAFT
        )
    }

    companion object {
        val CODEC: MapCodec<CraftingSkillEffect> = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                Codec.STRING.fieldOf("recipe").forGetter(CraftingSkillEffect::recipe),
                AbstractSkillEffectFilter.CODEC.fieldOf("filter").forGetter(CraftingSkillEffect::filter)
            ).apply(instance, ::CraftingSkillEffect)
        }

        val STREAM_CODEC: StreamCodec<RegistryFriendlyByteBuf, CraftingSkillEffect> = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, CraftingSkillEffect::recipe,
            AbstractSkillEffectFilter.STREAM_CODEC, CraftingSkillEffect::filter,
            ::CraftingSkillEffect
        )
    }
}