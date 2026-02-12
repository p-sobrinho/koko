package net.koji.arc_steam.common.registry

import net.koji.arc_steam.ArcaneSteam
import net.koji.arc_steam.common.attachments.PlayerSkills
import net.neoforged.bus.api.IEventBus
import net.neoforged.neoforge.attachment.AttachmentType
import net.neoforged.neoforge.registries.DeferredRegister
import net.neoforged.neoforge.registries.NeoForgeRegistries

object AttachmentsRegistry {
    private val ATTACHMENTS = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, ArcaneSteam.MOD_ID)

    val PLAYER_SKILLS = ATTACHMENTS.register("player_skills") { ->
        AttachmentType.builder(::PlayerSkills).serialize(PlayerSkills.CODEC).copyOnDeath().build()
    }

    fun register(modEventBus: IEventBus) { ATTACHMENTS.register(modEventBus) }
}
