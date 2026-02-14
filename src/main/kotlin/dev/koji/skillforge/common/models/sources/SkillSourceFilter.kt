package dev.koji.skillforge.common.models.sources

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import net.minecraft.util.StringRepresentable
import net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs
import java.util.Locale

data class SkillSourceFilter(
    val type: FilterType, val target: String,
    val priority: Int, val xp: Double, val inverse: Boolean,
) {
    enum class FilterType : StringRepresentable {
        WHITELIST, BLACKLIST;

        companion object{
            val CODEC = StringRepresentable.fromEnum(FilterType::values)

            val STREAM_CODEC: StreamCodec<RegistryFriendlyByteBuf, FilterType> = NeoForgeStreamCodecs.enumCodec(
                FilterType::class.java
            )
        }

        override fun getSerializedName(): String {
            return this.name.lowercase(Locale.ROOT)
        }
    }

    companion object {
        val CODEC = RecordCodecBuilder.create { instance ->
            instance.group(
                FilterType.CODEC.fieldOf("type").forGetter(SkillSourceFilter::type),
                Codec.STRING.fieldOf("target").forGetter(SkillSourceFilter::target),
                Codec.INT.optionalFieldOf("priority", 0).forGetter(SkillSourceFilter::priority),
                Codec.DOUBLE.fieldOf("xp").forGetter(SkillSourceFilter::xp),
                Codec.BOOL.optionalFieldOf("inverse", false).forGetter(SkillSourceFilter::inverse),
            ).apply(instance, ::SkillSourceFilter)
        }

        val STREAM_CODEC = StreamCodec.composite(
            FilterType.STREAM_CODEC, SkillSourceFilter::type,
            ByteBufCodecs.STRING_UTF8, SkillSourceFilter::target,
            ByteBufCodecs.VAR_INT, SkillSourceFilter::priority,
            ByteBufCodecs.DOUBLE, SkillSourceFilter::xp,
            ByteBufCodecs.BOOL, SkillSourceFilter::inverse,
            ::SkillSourceFilter
        )
    }
}