package net.koji.arc_steam.common.models

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec

data class SkillModel(
    val displayName: String, val minLevel: Int, val defaultLevel: Int,
    val maxLevel: Int, val overClockedMaxLevel: Int
) {
    companion object {
        val CODEC = RecordCodecBuilder.create { instance ->
            instance.group(
                Codec.STRING.fieldOf("display_name").forGetter(SkillModel::displayName),
                Codec.INT.fieldOf("min_level").forGetter(SkillModel::minLevel),
                Codec.INT.fieldOf("default_level").forGetter(SkillModel::defaultLevel),
                Codec.INT.fieldOf("max_level").forGetter(SkillModel::maxLevel),
                Codec.INT.fieldOf("overclocked_max_level").forGetter(SkillModel::overClockedMaxLevel)
            ).apply(instance, ::SkillModel)
        }

        val STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8,
            SkillModel::displayName,
            ByteBufCodecs.VAR_INT,
            SkillModel::minLevel,
            ByteBufCodecs.VAR_INT,
            SkillModel::defaultLevel,
            ByteBufCodecs.VAR_INT,
            SkillModel::maxLevel,
            ByteBufCodecs.VAR_INT,
            SkillModel::overClockedMaxLevel,
            ::SkillModel
        )
    }
}
