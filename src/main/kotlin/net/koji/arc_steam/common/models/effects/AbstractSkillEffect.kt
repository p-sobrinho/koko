package net.koji.arc_steam.common.models.effects

import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import net.koji.arc_steam.common.SkillsHandler
import net.koji.arc_steam.common.models.effects.player.AttributeSkillEffect
import net.koji.arc_steam.common.models.sources.AbstractSkillSource
import net.koji.arc_steam.common.models.sources.SkillSourceFilter
import net.koji.arc_steam.common.models.sources.block.BlockBreakSource
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import net.minecraft.world.entity.player.Player

abstract class AbstractSkillEffect {
    abstract val filters: List<AbstractSkillEffectFilter>
    abstract val type: String

    abstract fun doAnyApplies(level: Int): AbstractSkillEffectFilter?
    abstract fun apply(applier: SkillsHandler.SkillEffectApplier, player: Player)

    companion object {
        private val mapper = mapOf<String, MapCodec<out AbstractSkillEffect>>(
            AttributeSkillEffect.TYPE to AttributeSkillEffect.CODEC
        )

        val CODEC: Codec<AbstractSkillEffect> = Codec.STRING.dispatch(
            { source -> source.type },
            { type -> mapper.getOrElse(type) { throw IllegalArgumentException("$type is not supported") } }
        )

        val STREAM_CODEC: StreamCodec<RegistryFriendlyByteBuf, AbstractSkillEffect> =
            StreamCodec.of(
                { buf, source ->
                    ByteBufCodecs.STRING_UTF8.encode(buf, source.type)
                    when (source) {
                        is BlockBreakSource -> BlockBreakSource.STREAM_CODEC.encode(buf, source)
                        else -> throw IllegalArgumentException("Illegal source type: $source")
                    }
                },
                { buf ->
                    val type = ByteBufCodecs.STRING_UTF8.decode(buf)
                    val codec = STREAM_CODECS[type]
                        ?: throw IllegalArgumentException("Illegal source type: $type")

                    codec.decode(buf)
                }
            )

        private val STREAM_CODECS = mapOf(
            AttributeSkillEffect.TYPE to AttributeSkillEffect.STREAM_CODEC,
        )
    }
}