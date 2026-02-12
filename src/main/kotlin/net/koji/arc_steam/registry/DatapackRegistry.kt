package net.koji.arc_steam.registry

import net.koji.arc_steam.common.skills.SkillModel
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.common.EventBusSubscriber
import net.neoforged.neoforge.registries.DataPackRegistryEvent

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD)
object DatapackRegistry {
    @SubscribeEvent
    fun register(event: DataPackRegistryEvent.NewRegistry) {
        event.dataPackRegistry(
            SkillRegistry.SKILL_REGISTRY,
            SkillModel.CODEC
        )
    }
}