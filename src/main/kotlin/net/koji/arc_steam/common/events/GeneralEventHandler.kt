package net.koji.arc_steam.common.events

import com.mojang.logging.LogUtils
import net.koji.arc_steam.ArcaneSteam
import net.koji.arc_steam.common.SkillsHandler
import net.koji.arc_steam.common.registry.AttachmentsRegistry
import net.koji.arc_steam.common.registry.KeyRegistry
import net.minecraft.client.Minecraft
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.common.EventBusSubscriber
import net.neoforged.neoforge.client.event.ClientTickEvent
import net.neoforged.neoforge.event.entity.player.PlayerEvent
import net.neoforged.neoforge.event.level.BlockEvent

@EventBusSubscriber(modid = ArcaneSteam.MOD_ID)
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
            val level = mc.level ?: return

            val player = mc.player ?: return

            val playerSkills = player.getData(AttachmentsRegistry.PLAYER_SKILLS.get())

            LOGGER.info("{} has skills level at:", player.getName())

            SkillsHandler.getSkillsModels(player).forEach { skill ->
                print("SKILL DETECTED")
                println(skill.key)
                println(skill.value)
            }

            playerSkills.getAllSkills().forEach { (a, b) ->
                LOGGER.info("{} at {}", a.toString(), SkillsHandler.getLevel(player, a))
            }
        }
    }
}
