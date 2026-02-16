package dev.koji.koko.common.attachments

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import dev.koji.koko.common.models.SkillData
import net.minecraft.resources.ResourceLocation
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

    fun getAllSkills(): Map<ResourceLocation, SkillData> = skillsData

    @ApiStatus.Internal
    fun updateXp(skill: ResourceLocation, amount: Double) {
        this.getSkill(skill).xp += amount
    }

    @ApiStatus.Internal
    fun replace(newSkillsData: Map<ResourceLocation, SkillData>) {
        skillsData.clear()

        skillsData.putAll(newSkillsData)
    }

    fun put(skill: ResourceLocation, data: SkillData) {
        skillsData[skill] = data
    }

    fun putIfAbsent(skill: ResourceLocation, data: SkillData) {
        if (skillsData[skill] != null) return;

        put(skill, data)
    }
}
