package dev.koji.koko.common.registry

import dev.koji.koko.Koko
import dev.koji.koko.common.models.SkillModel
import net.minecraft.core.Registry
import net.minecraft.resources.ResourceKey
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.common.EventBusSubscriber
import net.neoforged.neoforge.registries.DataPackRegistryEvent

@EventBusSubscriber(modid = Koko.MOD_ID)
object DatapackRegistry {
    val SKILL_REGISTRY: ResourceKey<Registry<SkillModel>> =
        ResourceKey.createRegistryKey(Koko.namespacePath("skills"))

    @SubscribeEvent
    fun register(event: DataPackRegistryEvent.NewRegistry) {
        event.dataPackRegistry(
            SKILL_REGISTRY,
            SkillModel.CODEC,
            SkillModel.CODEC
        )
    }
}