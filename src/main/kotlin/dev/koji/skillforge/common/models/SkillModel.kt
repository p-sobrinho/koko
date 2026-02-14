package dev.koji.skillforge.common.models

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import dev.koji.skillforge.common.ktx.StreamCodecKTX
import dev.koji.skillforge.common.models.effects.AbstractSkillEffect
import dev.koji.skillforge.common.models.sources.AbstractSkillSource
import net.minecraft.network.codec.ByteBufCodecs

data class SkillModel(
    val displayName: String, val minLevel: Int, val defaultXp: Double,
    val maxLevel: Int, val overClockedMaxLevel: Int,
    val skillSources: List<AbstractSkillSource>, val effects: List<AbstractSkillEffect>
) {
    companion object {
        val CODEC = RecordCodecBuilder.create { instance ->
            instance.group(
                Codec.STRING.fieldOf("display_name").forGetter(SkillModel::displayName),
                Codec.INT.fieldOf("min_level").forGetter(SkillModel::minLevel),
                Codec.DOUBLE.fieldOf("default_level").forGetter(SkillModel::defaultXp),
                Codec.INT.fieldOf("max_level").forGetter(SkillModel::maxLevel),
                Codec.INT.fieldOf("overclocked_max_level").forGetter(SkillModel::overClockedMaxLevel),
                AbstractSkillSource.CODEC.listOf().optionalFieldOf("sources", listOf()).forGetter(SkillModel::skillSources),
                AbstractSkillEffect.CODEC.listOf().optionalFieldOf("effects", listOf()).forGetter(SkillModel::effects)
            ).apply(instance, ::SkillModel)
        }

        val STREAM_CODEC = StreamCodecKTX.composite(
            ByteBufCodecs.STRING_UTF8, SkillModel::displayName,
            ByteBufCodecs.VAR_INT, SkillModel::minLevel,
            ByteBufCodecs.DOUBLE, SkillModel::defaultXp,
            ByteBufCodecs.VAR_INT, SkillModel::maxLevel,
            ByteBufCodecs.VAR_INT, SkillModel::overClockedMaxLevel,
            AbstractSkillSource.STREAM_CODEC.apply(ByteBufCodecs.list()), SkillModel::skillSources,
            AbstractSkillEffect.STREAM_CODEC.apply(ByteBufCodecs.list()), SkillModel::effects,
            ::SkillModel
        )
    }
}
