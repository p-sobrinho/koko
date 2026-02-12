package net.koji.arc_steam.network.payloads

import net.koji.arc_steam.ArcaneSteam
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.minecraft.resources.ResourceLocation

class SyncSkillPayload(val skill: ResourceLocation, val skillXp: Int, val overclocked: Boolean) : CustomPacketPayload {
    companion object {
        val ID = ArcaneSteam.namespacePath("skill_payload")
        val STREAM_CODEC = StreamCodec.of<RegistryFriendlyByteBuf, SyncSkillPayload>(
            { buf, payload ->
                buf.writeResourceLocation(payload.skill)
                buf.writeVarInt(payload.skillXp)
                buf.writeBoolean(payload.overclocked)
            },
            { buf ->
                val skill = buf.readResourceLocation()
                val skillXp = buf.readVarInt()
                val overclocked = buf.readBoolean()
                SyncSkillPayload(skill, skillXp, overclocked)
            }
        )

        val TYPE = CustomPacketPayload.Type<SyncSkillsPayload>(ID)
    }

    override fun type(): CustomPacketPayload.Type<out CustomPacketPayload?> {
        return SyncSkillPayload.Companion.TYPE
    }
}
