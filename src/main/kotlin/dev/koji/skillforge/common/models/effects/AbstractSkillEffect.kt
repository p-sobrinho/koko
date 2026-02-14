package dev.koji.skillforge.common.models.effects

import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import dev.koji.skillforge.common.SkillsHandler
import dev.koji.skillforge.common.models.effects.player.AttributeSkillEffect
import dev.koji.skillforge.common.models.effects.player.CraftingSkillEffect
import dev.koji.skillforge.common.models.effects.player.ItemUseSkillEffect
import dev.koji.skillforge.common.models.sources.block.BlockBreakSource
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import net.minecraft.world.entity.player.Player

abstract class AbstractSkillEffect {
    abstract val type: String

    abstract fun doAnyApplies(level: Int): AbstractSkillEffectFilter?
    abstract fun apply(applier: SkillsHandler.SkillEffectApplier, player: Player)

    companion object {
        val CODEC: Codec<AbstractSkillEffect> = Codec.STRING.dispatch(
            { source -> source.type },
            { type -> codecMapper.getOrElse(type) { throw IllegalArgumentException("$type is not supported") } }
        )

        val STREAM_CODEC: StreamCodec<RegistryFriendlyByteBuf, AbstractSkillEffect> =
            StreamCodec.of(
                { buf, source ->
                    val type = source.type

                    ByteBufCodecs.STRING_UTF8.encode(buf, type)

                    val streamCodec = streamMapper[type] ?: throw IllegalArgumentException("Illegal source type: $type")

                    (streamCodec as StreamCodec<RegistryFriendlyByteBuf, AbstractSkillEffect>).encode(buf, source)
                },
                { buf ->
                    val type = ByteBufCodecs.STRING_UTF8.decode(buf)
                    val streamCodec = streamMapper[type] ?: throw IllegalArgumentException("Illegal source type: $type")

                    streamCodec.decode(buf)
                }
            )

        private val codecMapper = mapOf<String, MapCodec<out AbstractSkillEffect>>(
            AttributeSkillEffect.TYPE to AttributeSkillEffect.CODEC,
            CraftingSkillEffect.TYPE to CraftingSkillEffect.CODEC,
            ItemUseSkillEffect.TYPE to ItemUseSkillEffect.CODEC
        )

        private val streamMapper = mapOf(
            AttributeSkillEffect.TYPE to AttributeSkillEffect.STREAM_CODEC,
            CraftingSkillEffect.TYPE to CraftingSkillEffect.STREAM_CODEC,
            ItemUseSkillEffect.TYPE to ItemUseSkillEffect.STREAM_CODEC
        )
    }
}