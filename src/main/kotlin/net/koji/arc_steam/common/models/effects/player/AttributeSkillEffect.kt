package net.koji.arc_steam.common.models.effects.player

import com.mojang.logging.LogUtils
import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.koji.arc_steam.ArcaneSteam
import net.koji.arc_steam.common.SkillsHandler
import net.koji.arc_steam.common.models.effects.AbstractSkillEffect
import net.koji.arc_steam.common.models.effects.AbstractSkillEffectFilter
import net.koji.arc_steam.common.models.effects.filters.SkillEffectAboveFilter
import net.koji.arc_steam.common.models.effects.filters.SkillEffectBellowFilter
import net.koji.arc_steam.common.models.effects.filters.SkillEffectRangeFilter
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraft.util.StringRepresentable
import net.minecraft.world.entity.ai.attributes.AttributeModifier
import net.minecraft.world.entity.player.Player
import net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs
import java.util.Locale

class AttributeSkillEffect(
    val attribute: String,
    override val filters: List<AbstractSkillEffectFilter>
) : AbstractSkillEffect() {
    override val type: String = TYPE

    override fun doAnyApplies(level: Int): AbstractSkillEffectFilter? {
        val applicableFilters = filters.filter { it.apply(level) }

        return when (applicableFilters.size) {
            0 -> null
            1 -> applicableFilters.first()
            else -> throw IllegalArgumentException("Filter overlapping is not supported.")
        }
    }

    override fun apply(applier: SkillsHandler.SkillEffectApplier, player: Player) {
        val attributes = player.attributes

        val rl =
            if (attribute.contains(":")) ResourceLocation.parse(attribute)
            else ResourceLocation.fromNamespaceAndPath("minecraft", attribute)

        LOGGER.info(rl.toString())

        val attributeHolder = BuiltInRegistries.ATTRIBUTE.getHolder(ResourceKey.create(Registries.ATTRIBUTE, rl))

        if (attributeHolder.isEmpty) return LOGGER.warn("Unable to find holder for $attribute")

        val attributeInstance = attributes.getInstance(attributeHolder.get())
            ?: return LOGGER.warn("Unable to find instance for $attribute")

        val modifier = when(val filter = applier.filter) {
            is SkillEffectAboveFilter -> AttributeModifier(
                ArcaneSteam.namespacePath("attribute_${rl.path}"), filter.value, AttributeModifier.Operation.ADD_VALUE
            )

            is SkillEffectRangeFilter -> AttributeModifier(
                ArcaneSteam.namespacePath("attribute_${rl.path}"), filter.value, AttributeModifier.Operation.ADD_VALUE
            )

            is SkillEffectBellowFilter -> AttributeModifier(
                ArcaneSteam.namespacePath("attribute_${rl.path}"), filter.value, AttributeModifier.Operation.ADD_VALUE
            )

            else -> return LOGGER.warn("Couldn't identify modifier.")
        }

        attributeInstance.addOrUpdateTransientModifier(modifier)
    }

    companion object {
        const val TYPE = "player/attribute"

        val CODEC = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                Codec.STRING.fieldOf("attribute").forGetter(AttributeSkillEffect::attribute),
                AbstractSkillEffectFilter.CODEC.listOf().fieldOf("filters").forGetter(AttributeSkillEffect::filters)
            ).apply(instance, ::AttributeSkillEffect)
        }

        val STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, AttributeSkillEffect::attribute,
            AbstractSkillEffectFilter.STREAM_CODEC.apply(ByteBufCodecs.list()), AttributeSkillEffect::filters,
            ::AttributeSkillEffect
        )

        private val LOGGER = LogUtils.getLogger()
    }
}