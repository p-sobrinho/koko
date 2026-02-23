package dev.koji.koko.common.models.sources

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import net.minecraft.util.StringRepresentable
import net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs
import java.util.*

data class SkillSourceFilter(
    val type: FilterType, val target: String,
    val priority: Int, val xp: Double, val inverse: Boolean,
) {
    enum class FilterType : StringRepresentable {
        WHITELIST, BLACKLIST;

        companion object{
            val CODEC: StringRepresentable.EnumCodec<FilterType> = StringRepresentable.fromEnum(FilterType::values)

            val STREAM_CODEC: StreamCodec<RegistryFriendlyByteBuf, FilterType> = NeoForgeStreamCodecs.enumCodec(
                FilterType::class.java
            )
        }

        override fun getSerializedName(): String {
            return this.name.lowercase(Locale.ROOT)
        }
    }

    companion object {
        val CODEC: Codec<SkillSourceFilter> = RecordCodecBuilder.create { instance ->
            instance.group(
                FilterType.CODEC.fieldOf("type").forGetter { it.type },
                Codec.STRING.fieldOf("target").forGetter { it.target },
                Codec.INT.optionalFieldOf("priority", 0).forGetter { it.priority },
                Codec.DOUBLE.fieldOf("xp").forGetter { it.xp },
                Codec.BOOL.optionalFieldOf("inverse", false).forGetter { it.inverse },
            ).apply(instance, ::SkillSourceFilter)
        }

        val STREAM_CODEC: StreamCodec<RegistryFriendlyByteBuf, SkillSourceFilter> = StreamCodec.composite(
            FilterType.STREAM_CODEC, { it.type },
            ByteBufCodecs.STRING_UTF8, { it.target },
            ByteBufCodecs.VAR_INT, { it.priority },
            ByteBufCodecs.DOUBLE, { it.xp },
            ByteBufCodecs.BOOL, { it.inverse },
            ::SkillSourceFilter
        )
    }
}