package dev.koji.koko.common.events

import dev.koji.koko.Koko
import dev.koji.koko.common.SkillsHandler
import dev.koji.koko.common.helpers.MainHelper
import dev.koji.koko.common.models.sources.AbstractSkillSource
import dev.koji.koko.common.models.sources.SkillSourceFilter
import dev.koji.koko.common.models.sources.Sources
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceKey
import net.minecraft.server.level.ServerPlayer
import net.minecraft.tags.TagKey
import net.minecraft.world.level.block.state.BlockState
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.common.EventBusSubscriber
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent
import net.neoforged.neoforge.event.level.BlockEvent

@EventBusSubscriber(modid = Koko.MOD_ID)
object BlockEventHandler {
    @SubscribeEvent
    fun onBlockPlace(event: BlockEvent.EntityPlaceEvent) {
        if (event.level.isClientSide) return

        val player = (event.entity as? ServerPlayer) ?: return

        this.processBlockEvaluation(Sources.BLOCK_PLACE, event.state, player)
    }

    @SubscribeEvent
    fun onBlockInteract(event: PlayerInteractEvent.RightClickBlock) {
        if (event.level.isClientSide) return

        this.processBlockEvaluation(
            Sources.BLOCK_INTERACT,
            event.level.getBlockState(event.pos),
            event.entity as ServerPlayer
        )
    }

    @SubscribeEvent
    fun onBlockBreak(event: BlockEvent.BreakEvent) {
        if (event.level.isClientSide) return

        this.processBlockEvaluation(Sources.BLOCK_BREAK, event.state, event.player as ServerPlayer)
    }

    fun processBlockEvaluation(source: String, blockState: BlockState, player: ServerPlayer) {
        val listeners = SkillsHandler.getListenersFor(source, player.level())

        for (listener in listeners) {
            val skillSource = listener.sourceData

            val xp = if (skillSource.alwaysApply)
                skillSource.alwaysValue
            else
                this.blockEvaluate(listener.sourceData, blockState)

            if (xp == 0.0) continue

            SkillsHandler.updateXp(player, listener.skill, xp)
        }
    }

    fun blockEvaluate(skillModel: AbstractSkillSource, blockState: BlockState): Double =
        blockEvaluate(skillModel.filters, blockState)

    fun blockEvaluate(filters: List<SkillSourceFilter>, blockState: BlockState): Double {
        if (filters.isEmpty()) return 0.0

        val whitelists = filters.filter { it.type == SkillSourceFilter.FilterType.WHITELIST }
            .sortedByDescending { it.priority }

        val blacklists = filters.filter { it.type == SkillSourceFilter.FilterType.BLACKLIST }
            .sortedByDescending { it.priority }

        for (blacklist in blacklists) {
            if (this.blockMatches(blockState, blacklist.target)) return 0.0
        }

        if (whitelists.isEmpty()) return 0.0

        for (whitelist in whitelists) {
            if (this.blockMatches(blockState, whitelist.target)) {
                val xp = whitelist.xp

                return if (whitelist.inverse) -xp else xp
            }
        }

        return 0.0
    }

    private fun blockMatches(blockState: BlockState, targetLocation: String): Boolean {
        val resourceLocation = MainHelper.safeParseResource(targetLocation)
        val tagKey = TagKey.create(Registries.BLOCK, resourceLocation)

        return (blockState.`is`(tagKey) || blockState.`is`(ResourceKey.create(Registries.BLOCK, resourceLocation)))
    }
}
