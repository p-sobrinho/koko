package dev.koji.koko.common.helpers

import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.player.Player
import java.time.Instant

object MainHelper {
    private val MESSAGES_COOLDOWNS = HashMap<String, Instant>()

    fun sendMessageToPlayer(player: Player, message: String) {
        val cooldown = MESSAGES_COOLDOWNS.get(message)

        if (cooldown != null && cooldown.isAfter(Instant.now())) return

        MESSAGES_COOLDOWNS[message] = Instant.now().plusSeconds(1)

        player.sendSystemMessage(Component.translatable(message))
    }

    fun safeParseResource(resource: String, allowTag: Boolean = true): ResourceLocation {
        if (!allowTag && resource.contains("#"))
            throw RuntimeException("$resource must not be a tag.")

        return if (resource.contains(":"))
            ResourceLocation.parse(resource.removePrefix("#"))
        else
            ResourceLocation.fromNamespaceAndPath("minecraft", resource)
    }
}