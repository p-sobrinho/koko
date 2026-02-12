package net.koji.arc_steam.common.network.payloads

import net.koji.arc_steam.ArcaneSteam
import net.koji.arc_steam.common.models.SkillData
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.minecraft.resources.ResourceLocation

class SyncSkillPayload(val skill: ResourceLocation, val skillData: SkillData) : CustomPacketPayload {
    companion object {
        val ID = ArcaneSteam.namespacePath("skill_payload")

        val STREAM_CODEC = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC,
            SyncSkillPayload::skill,
            SkillData.STREAM_CODEC,
            SyncSkillPayload::skillData,
            ::SyncSkillPayload
        )

        val TYPE = CustomPacketPayload.Type<SyncSkillPayload>(ID)
    }

    override fun type(): CustomPacketPayload.Type<out CustomPacketPayload?> {
        return TYPE
    }
}
