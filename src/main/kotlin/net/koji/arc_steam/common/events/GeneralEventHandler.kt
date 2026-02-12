package net.koji.arc_steam.common.events

import net.koji.arc_steam.ArcaneSteam
import net.koji.arc_steam.common.skills.SkillModel
import net.koji.arc_steam.registry.AttachmentsRegistry
import net.koji.arc_steam.registry.KeyRegistry
import net.koji.arc_steam.registry.SkillRegistry
import net.minecraft.client.Minecraft
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.common.EventBusSubscriber
import net.neoforged.neoforge.client.event.ClientTickEvent
import net.neoforged.neoforge.event.entity.player.PlayerEvent
import net.neoforged.neoforge.event.level.BlockEvent
import net.neoforged.neoforge.registries.DataPackRegistryEvent

@EventBusSubscriber(modid = ArcaneSteam.MOD_ID)
object GeneralEventHandler {
    private val LOGGER = ArcaneSteam.LOGGER

    @SubscribeEvent
    fun onPlayerJoin(event: PlayerEvent.PlayerLoggedInEvent) {
        val player = event.entity

        LOGGER.info("{} joined the game", player.name)

        val playerSkills = player.getData(AttachmentsRegistry.PLAYER_SKILLS)

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
    fun onBlockBreak(event: BlockEvent.BreakEvent) {
        event.player.getData(AttachmentsRegistry.PLAYER_SKILLS).addXp(ArcaneSteam.namespacePath("mining"), 10)
    }

    @SubscribeEvent
    fun onClientTick(event: ClientTickEvent.Post) {
        while (KeyRegistry.OPEN_SKILLS.consumeClick()) {
            val mc = Minecraft.getInstance()

            val player = mc.player ?: return

            val playerSkills = player.getData(AttachmentsRegistry.PLAYER_SKILLS.get())

            LOGGER.info("{} has skills level at:", player.getName())

            playerSkills.getAllSkills().forEach { (a, b) ->

                LOGGER.info("{} at {}", a.toString(), playerSkills.getLevel(a))
            }

            //for (model in SkillRegistry.getAllAvailableSkills()) {
                //LOGGER.info("{} - {}", model.key.registry(), model.value.toString())
            //}
        }
    }
}
