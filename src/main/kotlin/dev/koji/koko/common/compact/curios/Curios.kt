package dev.koji.koko.common.compact.curios

import dev.koji.koko.common.SkillsHandler
import dev.koji.koko.common.compact.curios.effects.CuriosEquipSkillEffect
import dev.koji.koko.common.compact.curios.sources.CuriosTickSource
import dev.koji.koko.common.events.PlayerEventHandler
import dev.koji.koko.common.events.PlayerEventHandler.BlockScope
import dev.koji.koko.common.events.PlayerEventHandler.EventMessage
import dev.koji.koko.common.models.effects.AbstractSkillEffect
import dev.koji.koko.common.models.sources.AbstractSkillSource
import dev.koji.koko.common.models.sources.SkillSourceFilter
import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.effect.MobEffects
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.neoforge.common.util.TriState
import net.neoforged.neoforge.event.tick.PlayerTickEvent
import top.theillusivec4.curios.api.CuriosApi
import top.theillusivec4.curios.api.event.CurioCanEquipEvent

class Curios {
    @SubscribeEvent
    fun onPlayerTick(event: PlayerTickEvent.Post) {
        val player = event.entity

        if (player.level().isClientSide) return

        val optional = CuriosApi.getCuriosInventory(player)

        if (optional.isEmpty) return

        val inventory = optional.get()
        val equippedCurios = inventory.equippedCurios

        for (i in 1..equippedCurios.slots) {
            val curioAt = equippedCurios.getStackInSlot(i)

            if (curioAt == ItemStack.EMPTY) continue

            if (!PlayerEventHandler.isItemBlockedFor(player, curioAt, BlockScope.CURIOS)) continue

            player.addEffect(MobEffectInstance(
                MobEffects.MOVEMENT_SLOWDOWN,
                20, 10
            ))

            PlayerEventHandler.triggerMessage(player, EventMessage.UNABLE_TO_CURIOS)

            break
        }
    }

    @SubscribeEvent
    fun onCuriosSlotEquipTry(event: CurioCanEquipEvent) {
        val player = (event.entity as? Player) ?: return

        if (!PlayerEventHandler.isItemBlockedFor(player, event.stack, BlockScope.CURIOS)) return

        event.equipResult = TriState.FALSE

        if (!player.level().isClientSide) return

        PlayerEventHandler.triggerMessage(player, EventMessage.UNABLE_TO_CURIOS)
    }

    fun processCuriosEvaluate(
        source: String,
        item: ItemStack,
        player: Player
    ) {
        val listeners = SkillsHandler.getListenersFor(source, player.level())

        for (listener in listeners) {
            val xp = this.playerCuriosEvaluate(listener.sourceData, item)

            if (xp == 0.0) continue

            SkillsHandler.updateXp(player, listener.skill, xp)
        }
    }

    fun playerCuriosEvaluate(skillModel: AbstractSkillSource, item: ItemStack): Double =
        playerCuriosEvaluate(skillModel.filters, item)

    fun playerCuriosEvaluate(filters: List<SkillSourceFilter>, item: ItemStack): Double {
        if (filters.isEmpty()) return 0.0

        val whitelists = filters.filter { it.type == SkillSourceFilter.FilterType.WHITELIST }
            .sortedByDescending { it.priority }

        val blacklists = filters.filter { it.type == SkillSourceFilter.FilterType.BLACKLIST }
            .sortedByDescending { it.priority }

        for (blacklist in blacklists) {
            if (PlayerEventHandler.itemMatches(item, blacklist.target)) return 0.0
        }

        if (whitelists.isEmpty()) return 0.0

        for (whitelist in whitelists) {
            if (PlayerEventHandler.itemMatches(item, whitelist.target)) {
                val xp = whitelist.xp

                return if (whitelist.inverse) -xp else xp
            }
        }

        return 0.0
    }

    fun register() {
        AbstractSkillSource.codecsMapper[CuriosSources.PLAYER_CURIOUS_USE] = CuriosTickSource.CODEC
        AbstractSkillSource.streamMapper[CuriosSources.PLAYER_CURIOUS_USE] = CuriosTickSource.STREAM_CODEC

        AbstractSkillEffect.codecMapper[CuriosEffects.PLAYER_CURIOS_EQUIP] = CuriosEquipSkillEffect.CODEC
        AbstractSkillEffect.streamMapper[CuriosEffects.PLAYER_CURIOS_EQUIP] = CuriosEquipSkillEffect.STREAM_CODEC
    }
}