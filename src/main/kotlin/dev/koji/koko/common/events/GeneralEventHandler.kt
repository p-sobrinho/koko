package dev.koji.koko.common.events

import com.mojang.logging.LogUtils
import dev.koji.koko.Koko
import dev.koji.koko.common.SkillsHandler
import dev.koji.koko.common.registry.AttachmentsRegistry
import dev.koji.koko.common.registry.KeyRegistry
import net.minecraft.client.Minecraft
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.common.EventBusSubscriber
import net.neoforged.neoforge.client.event.ClientTickEvent
import net.neoforged.neoforge.event.entity.player.PlayerEvent

@EventBusSubscriber(modid = Koko.MOD_ID)
object GeneralEventHandler {
    private val LOGGER = LogUtils.getLogger()

    @SubscribeEvent
    fun onPlayerJoin(event: PlayerEvent.PlayerLoggedInEvent) {
        val player = event.entity

        LOGGER.info("{} joined the game", player.name)

        val playerSkills = player.getData(AttachmentsRegistry.PLAYER_SKILLS)

        SkillsHandler.syncNewSkills(player.level(), playerSkills)

        LOGGER.info("{} has skills level at:", player.name)

        playerSkills.getAllSkills().forEach({ skillResource, skillData ->
            LOGGER.info(
                "{} at {} in level {}",
                skillResource.toString(),
                skillData.xp,
                skillData.isOverClocked
            )
        })
    }

    @SubscribeEvent
    fun onClientTick(event: ClientTickEvent.Post) {
        while (KeyRegistry.OPEN_SKILLS.consumeClick()) {
            val mc = Minecraft.getInstance()
            val player = mc.player ?: return

            val playerSkills = player.getData(AttachmentsRegistry.PLAYER_SKILLS.get())

            LOGGER.info("{} has skills level at:", player.getName())

            SkillsHandler.getSkillsModels(player).forEach { skill ->
                print("SKILL DETECTED")
                println(skill.key)
                println(skill.value)
            }

            playerSkills.getAllSkills().forEach { (a, b) ->
                LOGGER.info("{} - {}", a.toString(), b)
            }
        }
    }
}
