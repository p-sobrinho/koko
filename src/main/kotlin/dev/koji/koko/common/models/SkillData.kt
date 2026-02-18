package dev.koji.koko.common.models

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import io.netty.buffer.ByteBuf
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import org.jetbrains.annotations.ApiStatus

class SkillData(@set:ApiStatus.Internal var xp: Double, @set:ApiStatus.Internal var isOverClocked: Boolean) {
    companion object {
        val CODEC: Codec<SkillData> = RecordCodecBuilder.create { instance ->
            instance.group(
                Codec.DOUBLE.fieldOf("xp").forGetter(SkillData::xp),
                Codec.BOOL.fieldOf("overclocked").forGetter(SkillData::isOverClocked)
            ).apply(instance, ::SkillData)
        }

        val STREAM_CODEC: StreamCodec<ByteBuf, SkillData> = StreamCodec.composite(
            ByteBufCodecs.DOUBLE, SkillData::xp,
            ByteBufCodecs.BOOL, SkillData::isOverClocked,
            ::SkillData
        )
    }

    // Checking if I can remove this.
    //constructor() : this(0.0, false)
}
