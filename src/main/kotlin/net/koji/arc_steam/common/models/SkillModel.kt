package net.koji.arc_steam.common.models

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.koji.arc_steam.common.ktx.StreamCodecKTX
import net.koji.arc_steam.common.models.sources.SkillSource
import net.minecraft.network.codec.ByteBufCodecs

data class SkillModel(
    val displayName: String, val minLevel: Int, val defaultXp: Double,
    val maxLevel: Int, val overClockedMaxLevel: Int, val skillSources: List<SkillSource>
) {
    companion object {
        val CODEC = RecordCodecBuilder.create { instance ->
            instance.group(
                Codec.STRING.fieldOf("display_name").forGetter(SkillModel::displayName),
                Codec.INT.fieldOf("min_level").forGetter(SkillModel::minLevel),
                Codec.DOUBLE.fieldOf("default_level").forGetter(SkillModel::defaultXp),
                Codec.INT.fieldOf("max_level").forGetter(SkillModel::maxLevel),
                Codec.INT.fieldOf("overclocked_max_level").forGetter(SkillModel::overClockedMaxLevel),
                SkillSource.CODEC.listOf().optionalFieldOf("sources", listOf()).forGetter(SkillModel::skillSources)
            ).apply(instance, ::SkillModel)
        }

        val STREAM_CODEC = StreamCodecKTX.composite(
            ByteBufCodecs.STRING_UTF8, SkillModel::displayName,
            ByteBufCodecs.VAR_INT, SkillModel::minLevel,
            ByteBufCodecs.DOUBLE, SkillModel::defaultXp,
            ByteBufCodecs.VAR_INT, SkillModel::maxLevel,
            ByteBufCodecs.VAR_INT, SkillModel::overClockedMaxLevel,
            SkillSource.STREAM_CODEC.apply(ByteBufCodecs.list()),
            SkillModel::skillSources,
            ::SkillModel
        )
    }
}
