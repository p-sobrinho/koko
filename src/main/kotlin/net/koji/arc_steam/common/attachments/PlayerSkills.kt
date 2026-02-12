package net.koji.arc_steam.common.attachments

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.koji.arc_steam.ArcaneSteam
import net.koji.arc_steam.common.skills.SkillData
import net.koji.arc_steam.registry.SkillRegistry
import net.minecraft.resources.ResourceLocation
import net.neoforged.neoforge.capabilities.EntityCapability
import org.jetbrains.annotations.ApiStatus

class PlayerSkills {
    companion object {
        val CODEC = RecordCodecBuilder.create<PlayerSkills> { instance ->
            instance.group(
                Codec.unboundedMap(ResourceLocation.CODEC, SkillData.CODEC)
                    .fieldOf("skills")
                    .forGetter(PlayerSkills::getAllSkills)
            ).apply(instance, ::PlayerSkills)
        }
    }
    private val skillsData = mutableMapOf<ResourceLocation, SkillData>()

    constructor()

    constructor(newSkillsData: MutableMap<ResourceLocation, SkillData>) {
        skillsData.putAll(newSkillsData)
    }

    fun getSkill(skill: ResourceLocation): SkillData {
        return skillsData.getValue(skill)
    }

    fun getLevel(skill: ResourceLocation): Int {
        val skillData = getSkill(skill)
        val skillModel = SkillRegistry.getSkill(skill)

        val xp = skillData.xp
        val maxLevel = if (skillData.isOverClocked) skillModel.overClockedMaxLevel else skillModel.maxLevel
        var total = 0

        for (level in 1..maxLevel) {
            total += this.xpToLevelUp(level)

            if (xp < total) {
                return level
            }
        }

        return maxLevel
    }

    fun getXp(skill: ResourceLocation): Int {
        return getSkill(skill).xp
    }

    fun getAllSkills() = skillsData

    fun isOverclocked(skill: ResourceLocation): Boolean {
        return this.getSkill(skill).isOverClocked
    }

    @ApiStatus.Internal
    fun addXp(skill: ResourceLocation, amount: Int) {
        getSkill(skill).xp = this.getXp(skill) + amount
    }

    @ApiStatus.Internal
    fun removeXp(skill: ResourceLocation, amount: Int) {
        getSkill(skill).xp = this.getXp(skill) - amount
    }

    @ApiStatus.Internal
    fun replace(newSkillsData: MutableMap<ResourceLocation, SkillData>) {
        skillsData.clear()

        skillsData.putAll(newSkillsData)
    }

    private fun xpToLevelUp(level: Int): Int {
        return (100 + level * 25)
    }
}
