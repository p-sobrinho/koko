package net.koji.arc_steam.common.models

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import org.jetbrains.annotations.ApiStatus

class SkillData(@set:ApiStatus.Internal var xp: Double, @set:ApiStatus.Internal var isOverClocked: Boolean) {
    companion object {
        val CODEC = RecordCodecBuilder.create { instance ->
            instance.group(
                Codec.DOUBLE.fieldOf("xp").forGetter(SkillData::xp),
                Codec.BOOL.fieldOf("overclocked").forGetter(SkillData::isOverClocked)
            ).apply(instance, ::SkillData)
        }

        val STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.DOUBLE, SkillData::xp,
            ByteBufCodecs.BOOL, SkillData::isOverClocked,
            ::SkillData
        )
    }

    constructor() : this(0.0, false)
}
