package dev.koji.skillforge.common.models.effects.player

import com.mojang.logging.LogUtils
import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import dev.koji.skillforge.SkillForge
import dev.koji.skillforge.common.SkillsHandler
import dev.koji.skillforge.common.models.effects.AbstractSkillEffect
import dev.koji.skillforge.common.models.effects.AbstractSkillEffectFilter
import dev.koji.skillforge.common.models.effects.filters.AboveSkillEffectFilter
import dev.koji.skillforge.common.models.effects.filters.BellowSkillEffectFilter
import dev.koji.skillforge.common.models.effects.filters.RangeSkillEffectFilter
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.ai.attributes.AttributeModifier
import net.minecraft.world.entity.player.Player

class AttributeSkillEffect(
    val attribute: String,
    val filters: List<AbstractSkillEffectFilter>
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

        val attributeLocation = SkillsHandler.safeParseResource(attribute)

        val attributeHolder = BuiltInRegistries.ATTRIBUTE.getHolder(
            ResourceKey.create(Registries.ATTRIBUTE, attributeLocation)
        )

        if (attributeHolder.isEmpty) return LOGGER.warn("Unable to find holder for $attribute")

        val attributeInstance = attributes.getInstance(attributeHolder.get())
            ?: return LOGGER.warn("Unable to find instance for $attribute")

        val modifier = when(val filter = applier.filter) {
            is AboveSkillEffectFilter -> AttributeModifier(
                SkillForge.namespacePath("attribute_${attributeLocation.path}"), filter.value, filter.operation
            )

            is RangeSkillEffectFilter -> AttributeModifier(
                SkillForge.namespacePath("attribute_${attributeLocation.path}"), filter.value, filter.operation
            )

            is BellowSkillEffectFilter -> AttributeModifier(
                SkillForge.namespacePath("attribute_${attributeLocation.path}"), filter.value, filter.operation
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