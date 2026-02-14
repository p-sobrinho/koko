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

class CraftingSkillEffect(
    val recipe: String,
    val filter: AbstractSkillEffectFilter
) : AbstractSkillEffect() {
    override val type: String = TYPE

    override fun doAnyApplies(level: Int): AbstractSkillEffectFilter? = filter.takeIf { it.apply(level) }

    override fun apply(applier: SkillsHandler.SkillEffectApplier, player: Player) {
        val recipeLocation =
            if (recipe.contains(":")) ResourceLocation.parse(recipe)
            else ResourceLocation.fromNamespaceAndPath("minecraft", recipe)

        PlayerEventHandler.addBlockedItem(
            player.uuid, recipeLocation.toString(), (applier.filter as BlockedSkillEffectFilter).until
        )
    }

    companion object {
        const val TYPE = "player/crafting"

        val CODEC = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                Codec.STRING.fieldOf("recipe").forGetter(CraftingSkillEffect::recipe),
                AbstractSkillEffectFilter.CODEC.fieldOf("filter").forGetter(CraftingSkillEffect::filter)
            ).apply(instance, ::CraftingSkillEffect)
        }

        val STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, CraftingSkillEffect::recipe,
            AbstractSkillEffectFilter.STREAM_CODEC, CraftingSkillEffect::filter,
            ::CraftingSkillEffect
        )

        private val LOGGER = LogUtils.getLogger()
    }
}