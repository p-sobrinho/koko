package dev.koji.koko.common.models.effects

import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import dev.koji.koko.common.SkillsHandler
import dev.koji.koko.common.models.effects.player.*
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import net.minecraft.world.entity.player.Player

abstract class AbstractSkillEffect {
    abstract val type: String

    abstract fun doAnyApplies(level: Int): AbstractSkillEffectFilter?
    abstract fun apply(applier: SkillsHandler.SkillEffectApplier, player: Player)
    abstract fun unApply(applier: SkillsHandler.SkillEffectApplier, player: Player)

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
            Effects.PLAYER_ATTRIBUTE to AttributeSkillEffect.CODEC,
            Effects.PLAYER_CRAFT to CraftingSkillEffect.CODEC,
            Effects.PLAYER_FORGE to ForgeSkillEffect.CODEC,
            Effects.PLAYER_ATTACK to ItemAttackSkillEffect.CODEC,
            Effects.PLAYER_USE to ItemUseSkillEffect.CODEC,
            Effects.PLAYER_CONSUME to ItemConsumeSkillEffect.CODEC
        )

        private val streamMapper = mapOf<String, StreamCodec<RegistryFriendlyByteBuf, out AbstractSkillEffect>>(
            Effects.PLAYER_ATTRIBUTE to AttributeSkillEffect.STREAM_CODEC,
            Effects.PLAYER_CRAFT to CraftingSkillEffect.STREAM_CODEC,
            Effects.PLAYER_FORGE to ForgeSkillEffect.STREAM_CODEC,
            Effects.PLAYER_ATTACK to ItemAttackSkillEffect.STREAM_CODEC,
            Effects.PLAYER_USE to ItemUseSkillEffect.STREAM_CODEC,
            Effects.PLAYER_CONSUME to ItemConsumeSkillEffect.STREAM_CODEC
        )
    }
}