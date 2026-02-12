package net.koji.arc_steam.registry

import net.koji.arc_steam.ArcaneSteam
import net.koji.arc_steam.common.skills.SkillModel
import net.minecraft.core.Registry
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.neoforged.neoforge.server.ServerLifecycleHooks
import kotlin.jvm.optionals.getOrNull

object SkillRegistry {
    val SKILL_REGISTRY: ResourceKey<Registry<SkillModel>> =
        ResourceKey.createRegistryKey(ArcaneSteam.namespacePath("skills"))

    fun getSkill(skill: ResourceLocation): SkillModel {
        val server = ServerLifecycleHooks.getCurrentServer()
            ?: throw IllegalStateException("Server isn't initialized yet.")

        return server.registryAccess().registryOrThrow(SKILL_REGISTRY).get(skill)
            ?: throw NullPointerException("Unknown skill.")
    }

    fun getAllAvailableSkills(): Set<Map.Entry<ResourceKey<SkillModel>,SkillModel>> {
        val server = ServerLifecycleHooks.getCurrentServer()
            ?: throw IllegalStateException("Server isn't initialized yet.")

        return server.registryAccess().registry(SKILL_REGISTRY).getOrNull()?.entrySet()
            ?: throw NullPointerException("Unknown skill.")
    }
}
