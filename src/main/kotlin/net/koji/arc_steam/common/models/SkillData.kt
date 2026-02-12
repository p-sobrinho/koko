package net.koji.arc_steam.common.models

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import org.jetbrains.annotations.ApiStatus

class SkillData(@set:ApiStatus.Internal var xp: Int, @set:ApiStatus.Internal var isOverClocked: Boolean) {
    companion object {
        val CODEC = RecordCodecBuilder.create { instance ->
            instance.group(
                Codec.INT.fieldOf("xp").forGetter(SkillData::xp),
                Codec.BOOL.fieldOf("overclocked").forGetter(SkillData::isOverClocked)
            ).apply(instance, ::SkillData)
        }

        val STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            SkillData::xp,
            ByteBufCodecs.BOOL,
            SkillData::isOverClocked,
            ::SkillData
        )
    }

    constructor() : this(0, false)
}
