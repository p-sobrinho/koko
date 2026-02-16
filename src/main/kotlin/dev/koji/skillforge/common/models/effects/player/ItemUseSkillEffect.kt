package dev.koji.skillforge.common.models.effects.player

import com.mojang.logging.LogUtils
import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import dev.koji.skillforge.common.SkillsHandler
import dev.koji.skillforge.common.events.PlayerEventHandler
import dev.koji.skillforge.common.models.effects.AbstractSkillEffect
import dev.koji.skillforge.common.models.effects.AbstractSkillEffectFilter
import dev.koji.skillforge.common.models.effects.filters.BlockedSkillEffectFilter
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.player.Player

class ItemUseSkillEffect(
    val item: String,
    val filter: AbstractSkillEffectFilter
) : AbstractSkillEffect() {
    override val type: String = TYPE

    override fun doAnyApplies(level: Int): AbstractSkillEffectFilter? = filter.takeIf { it.apply(level) }

    override fun apply(applier: SkillsHandler.SkillEffectApplier, player: Player) {
        PlayerEventHandler.addBlockedItem(
            player.uuid, SkillsHandler.safeParseResource(item), PlayerEventHandler.BlockScope.USE
        )
    }

    companion object {
        const val TYPE = "player/item_use"

        val CODEC = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                Codec.STRING.fieldOf("item").forGetter(ItemUseSkillEffect::item),
                AbstractSkillEffectFilter.CODEC.fieldOf("filter").forGetter(ItemUseSkillEffect::filter)
            ).apply(instance, ::ItemUseSkillEffect)
        }

        val STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, ItemUseSkillEffect::item,
            AbstractSkillEffectFilter.STREAM_CODEC, ItemUseSkillEffect::filter,
            ::ItemUseSkillEffect
        )

        private val LOGGER = LogUtils.getLogger()
    }
}