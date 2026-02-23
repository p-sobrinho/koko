package dev.koji.koko.common.models

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import dev.koji.koko.common.helpers.StreamHelper
import dev.koji.koko.common.models.effects.AbstractSkillEffect
import dev.koji.koko.common.models.sources.AbstractSkillSource
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec

data class SkillModel(
    val displayName: String, val icon: String, val description: String,
    val defaultXp: Double, val maxLevel: Int, val unlockedMaxLevel: Int,
    val skillSources: List<AbstractSkillSource>, val effects: List<AbstractSkillEffect>
) {
    companion object {
        val CODEC: Codec<SkillModel> = RecordCodecBuilder.create { instance ->
            instance.group(
                Codec.STRING.fieldOf("display_name").forGetter(SkillModel::displayName),
                Codec.STRING.fieldOf("icon").forGetter(SkillModel::icon),
                Codec.STRING.fieldOf("description").forGetter(SkillModel::description),
                Codec.DOUBLE.fieldOf("default_xp").forGetter(SkillModel::defaultXp),
                Codec.INT.fieldOf("max_level").forGetter(SkillModel::maxLevel),
                Codec.INT.fieldOf("unlocked_max_level").forGetter(SkillModel::unlockedMaxLevel),
                AbstractSkillSource.CODEC.listOf().optionalFieldOf("sources", listOf()).forGetter(SkillModel::skillSources),
                AbstractSkillEffect.CODEC.listOf().optionalFieldOf("effects", listOf()).forGetter(SkillModel::effects)
            ).apply(instance, ::SkillModel)
        }

        val STREAM_CODEC: StreamCodec<RegistryFriendlyByteBuf, SkillModel> = StreamHelper.composite(
            ByteBufCodecs.STRING_UTF8, SkillModel::displayName,
            ByteBufCodecs.STRING_UTF8, SkillModel::icon,
            ByteBufCodecs.STRING_UTF8, SkillModel::description,
            ByteBufCodecs.DOUBLE, SkillModel::defaultXp,
            ByteBufCodecs.VAR_INT, SkillModel::maxLevel,
            ByteBufCodecs.VAR_INT, SkillModel::unlockedMaxLevel,
            AbstractSkillSource.STREAM_CODEC.apply(ByteBufCodecs.list()), SkillModel::skillSources,
            AbstractSkillEffect.STREAM_CODEC.apply(ByteBufCodecs.list()), SkillModel::effects,
            ::SkillModel
        )
    }
}
