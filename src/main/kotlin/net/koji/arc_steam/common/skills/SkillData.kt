package net.koji.arc_steam.common.skills

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import org.jetbrains.annotations.ApiStatus
import java.util.function.BiFunction
import java.util.function.Function

class SkillData(@set:ApiStatus.Internal var xp: Int, @set:ApiStatus.Internal var isOverClocked: Boolean) {
    companion object {
        val CODEC = RecordCodecBuilder.create { instance ->
            instance.group(
                Codec.INT.fieldOf("xp").forGetter(SkillData::xp),
                Codec.BOOL.fieldOf("overclocked").forGetter(SkillData::isOverClocked)
            ).apply(instance, ::SkillData)
        }
    }
}
