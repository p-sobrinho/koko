package dev.koji.koko.common.network.payloads

import dev.koji.koko.Koko
import dev.koji.koko.common.models.SkillData
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.minecraft.resources.ResourceLocation

class SyncSkillsPayload(val skillData: Map<ResourceLocation, SkillData>) : CustomPacketPayload {
    companion object {
        val ID: ResourceLocation = Koko.namespacePath("skills_payload")
        val STREAM_CODEC: StreamCodec<RegistryFriendlyByteBuf, SyncSkillsPayload> = StreamCodec.of<RegistryFriendlyByteBuf, SyncSkillsPayload>(
            { buf, payload ->
                buf.writeVarInt(payload.skillData.size)
                payload.skillData.forEach { (id, data) ->
                    buf.writeResourceLocation(id)

                    ByteBufCodecs.fromCodec(SkillData.CODEC).encode(buf, data)
                }
            },
            { buf ->
                val size = buf.readVarInt()
                val map = HashMap<ResourceLocation, SkillData>()

                for (i in 0..<size) {
                    val id: ResourceLocation = buf.readResourceLocation()
                    val data: SkillData = ByteBufCodecs.fromCodec(SkillData.CODEC).decode(buf)

                    map[id] = data
                }
                SyncSkillsPayload(map)
            }
        )

        val TYPE = CustomPacketPayload.Type<SyncSkillsPayload>(ID)
    }

    override fun type(): CustomPacketPayload.Type<out CustomPacketPayload> = TYPE
}
