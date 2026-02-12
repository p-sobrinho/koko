package net.koji.arc_steam.common.registry

import net.koji.arc_steam.ArcaneSteam
import net.koji.arc_steam.common.models.SkillModel
import net.minecraft.core.Registry
import net.minecraft.resources.ResourceKey
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.common.EventBusSubscriber
import net.neoforged.neoforge.registries.DataPackRegistryEvent

@EventBusSubscriber(modid = ArcaneSteam.MOD_ID)
object DatapackRegistry {
    val SKILL_REGISTRY: ResourceKey<Registry<SkillModel>> =
        ResourceKey.createRegistryKey(ArcaneSteam.namespacePath("skills"))

    @SubscribeEvent
    fun register(event: DataPackRegistryEvent.NewRegistry) {
        event.dataPackRegistry(
            SKILL_REGISTRY,
            SkillModel.CODEC,
            SkillModel.CODEC
        )
    }
}