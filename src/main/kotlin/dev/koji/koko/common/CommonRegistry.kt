package dev.koji.koko.common

import dev.koji.koko.Koko
import dev.koji.koko.common.attachments.PlayerSkills
import dev.koji.koko.common.models.SkillModel
import net.minecraft.core.Registry
import net.minecraft.resources.ResourceKey
import net.neoforged.bus.api.IEventBus
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.common.EventBusSubscriber
import net.neoforged.neoforge.attachment.AttachmentType
import net.neoforged.neoforge.registries.DataPackRegistryEvent
import net.neoforged.neoforge.registries.DeferredHolder
import net.neoforged.neoforge.registries.DeferredRegister
import net.neoforged.neoforge.registries.NeoForgeRegistries

@EventBusSubscriber(modid = Koko.MOD_ID)
object CommonRegistry {
    val SKILL_REGISTRY: ResourceKey<Registry<SkillModel>> =
        ResourceKey.createRegistryKey(Koko.namespacePath("skills"))
    val ATTACHMENTS: DeferredRegister<AttachmentType<*>> =
        DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, Koko.MOD_ID)

    val PLAYER_SKILLS: DeferredHolder<AttachmentType<*>, AttachmentType<PlayerSkills>> =
        ATTACHMENTS.register("player_skills") { ->
            AttachmentType.builder(::PlayerSkills).serialize(PlayerSkills.CODEC).copyOnDeath().build()
        }

    @SubscribeEvent
    fun register(event: DataPackRegistryEvent.NewRegistry) {
        event.dataPackRegistry(
            SKILL_REGISTRY,
            SkillModel.CODEC,
            SkillModel.CODEC
        )
    }

    fun register(modEventBus: IEventBus) { ATTACHMENTS.register(modEventBus) }
}