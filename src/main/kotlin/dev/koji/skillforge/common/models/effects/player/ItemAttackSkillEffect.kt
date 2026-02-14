package dev.koji.skillforge.common.models.effects.player

import com.mojang.logging.LogUtils
import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import dev.koji.skillforge.common.SkillsHandler
import dev.koji.skillforge.common.events.PlayerEventHandler
import dev.koji.skillforge.common.models.effects.AbstractSkillEffect
import dev.koji.skillforge.common.models.effects.AbstractSkillEffectFilter
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.player.Player

class ItemAttackSkillEffect(
    val item: String,
    val filter: AbstractSkillEffectFilter
) : AbstractSkillEffect() {
    override val type: String = TYPE

    override fun doAnyApplies(level: Int): AbstractSkillEffectFilter? = filter.takeIf { it.apply(level) }

    override fun apply(applier: SkillsHandler.SkillEffectApplier, player: Player) {
        val recipeLocation =
            if (item.contains(":")) ResourceLocation.parse(item)
            else ResourceLocation.fromNamespaceAndPath("minecraft", item)

        PlayerEventHandler.addBlockedItem(player.uuid, recipeLocation, PlayerEventHandler.BlockScope.ATTACK)
    }

    companion object {
        const val TYPE = "player/item_attack"

        val CODEC = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                Codec.STRING.fieldOf("item").forGetter(ItemAttackSkillEffect::item),
                AbstractSkillEffectFilter.CODEC.fieldOf("filter").forGetter(ItemAttackSkillEffect::filter)
            ).apply(instance, ::ItemAttackSkillEffect)
        }

        val STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, ItemAttackSkillEffect::item,
            AbstractSkillEffectFilter.STREAM_CODEC, ItemAttackSkillEffect::filter,
            ::ItemAttackSkillEffect
        )

        private val LOGGER = LogUtils.getLogger()
    }
}