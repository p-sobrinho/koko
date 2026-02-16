package dev.koji.koko.common.models.effects.player

import com.mojang.logging.LogUtils
import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import dev.koji.koko.common.SkillsHandler
import dev.koji.koko.common.events.PlayerEventHandler
import dev.koji.koko.common.models.effects.AbstractSkillEffect
import dev.koji.koko.common.models.effects.AbstractSkillEffectFilter
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import net.minecraft.world.entity.player.Player

class ForgeSkillEffect(
    val recipe: String,
    val filter: AbstractSkillEffectFilter
) : AbstractSkillEffect() {
    override val type: String = TYPE

    override fun doAnyApplies(level: Int): AbstractSkillEffectFilter? = filter.takeIf { it.apply(level) }

    override fun apply(applier: SkillsHandler.SkillEffectApplier, player: Player) {
        PlayerEventHandler.addBlockedItem(
            player.uuid, SkillsHandler.safeParseResource(recipe), PlayerEventHandler.BlockScope.FORGE
        )
    }

    override fun unApply(
        applier: SkillsHandler.SkillEffectApplier,
        player: Player
    ) {
        PlayerEventHandler.removeBlockedItem(
            player.uuid, SkillsHandler.safeParseResource(recipe), PlayerEventHandler.BlockScope.FORGE
        )
    }

    companion object {
        const val TYPE = "player/forge"

        val CODEC = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                Codec.STRING.fieldOf("recipe").forGetter(ForgeSkillEffect::recipe),
                AbstractSkillEffectFilter.CODEC.fieldOf("filter").forGetter(ForgeSkillEffect::filter)
            ).apply(instance, ::ForgeSkillEffect)
        }

        val STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, ForgeSkillEffect::recipe,
            AbstractSkillEffectFilter.STREAM_CODEC, ForgeSkillEffect::filter,
            ::ForgeSkillEffect
        )

        private val LOGGER = LogUtils.getLogger()
    }
}