package dev.koji.koko.common.network.payloads

import dev.koji.koko.Koko
import dev.koji.koko.common.models.SkillData
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.minecraft.resources.ResourceLocation

class SyncSkillPayload(val skill: ResourceLocation, val skillData: SkillData) : CustomPacketPayload {
    companion object {
        val STREAM_CODEC: StreamCodec<RegistryFriendlyByteBuf, SyncSkillPayload> = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC,
            SyncSkillPayload::skill,
            SkillData.STREAM_CODEC,
            SyncSkillPayload::skillData,
            ::SyncSkillPayload
        )

        val TYPE = CustomPacketPayload.Type<SyncSkillPayload>(Koko.namespacePath("skill_payload"))
    }

    override fun type(): CustomPacketPayload.Type<out CustomPacketPayload> = TYPE
}
