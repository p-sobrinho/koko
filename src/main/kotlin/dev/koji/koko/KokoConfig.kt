package dev.koji.koko

import dev.koji.koko.common.Translatables
import net.neoforged.neoforge.common.ModConfigSpec

object KokoConfig {
    val BUILDER = ModConfigSpec.Builder()
    val SPEC: ModConfigSpec
    val MESSAGES_CONFIG: Map<String, ModConfigSpec.ConfigValue<String>>

    init {
        BUILDER.push("messages")

        val currentMessagesConfig = mutableMapOf<String, ModConfigSpec.ConfigValue<String>>()

        this.put(currentMessagesConfig, "unable_to_use", Translatables.MESSAGES_UNABLE_TO_USE)
        this.put(currentMessagesConfig, "unable_to_attack", Translatables.MESSAGES_UNABLE_TO_ATTACK)
        this.put(currentMessagesConfig, "unable_to_consume", Translatables.MESSAGES_UNABLE_TO_CONSUME)
        this.put(currentMessagesConfig, "unable_to_craft", Translatables.MESSAGES_UNABLE_TO_CRAFT)
        this.put(currentMessagesConfig, "unable_to_forge", Translatables.MESSAGES_UNABLE_TO_FORGE)
        this.put(currentMessagesConfig, "unable_to_armor", Translatables.MESSAGES_UNABLE_TO_ARMOR)
        this.put(currentMessagesConfig, "curios_unable_to_wear", Translatables.MESSAGES_CURIOS_UNABLE_TO_WEAR)
        this.put(currentMessagesConfig, "iss_unable_to_cast", Translatables.MESSAGES_ISS_UNABLE_TO_CAST)
        this.put(currentMessagesConfig, "iss_unable_to_inscribe", Translatables.MESSAGES_ISS_UNABLE_TO_INSCRIBE)

        BUILDER.pop()

        SPEC = BUILDER.build()
        MESSAGES_CONFIG = currentMessagesConfig
    }

    private fun put(map: MutableMap<String, ModConfigSpec.ConfigValue<String>>, key: String, value: String) {
        map[value] = BUILDER.define(key, value)
    }
}