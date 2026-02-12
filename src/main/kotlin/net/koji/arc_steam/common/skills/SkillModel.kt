package net.koji.arc_steam.common.skills

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder

data class SkillModel(
    val display: String, val minLevel: Int, val defaultLevel: Int,
    val maxLevel: Int, val overClockedMaxLevel: Int
) {
    companion object {
        val CODEC = RecordCodecBuilder.create { instance ->
            instance.group(
                Codec.STRING.fieldOf("display").forGetter(SkillModel::display),
                Codec.INT.fieldOf("min_level").forGetter(SkillModel::minLevel),
                Codec.INT.fieldOf("default_level").forGetter(SkillModel::defaultLevel),
                Codec.INT.fieldOf("max_level").forGetter(SkillModel::maxLevel),
                Codec.INT.fieldOf("over_clocked_max_level").forGetter(SkillModel::overClockedMaxLevel)
            ).apply(instance, ::SkillModel)
        }
    }
}
