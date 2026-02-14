package dev.koji.skillforge.common.events

import net.minecraft.core.particles.ParticleTypes
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerLevel
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.world.Container
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.common.EventBusSubscriber
import net.neoforged.neoforge.event.AnvilUpdateEvent
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent
import net.neoforged.neoforge.event.entity.player.PlayerEvent.ItemCraftedEvent
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent
import java.util.*


@EventBusSubscriber
object PlayerEventHandler {
    private val BLOCKED_PLAYER_USES = HashMap<UUID, MutableSet<ResourceLocation>>()
    private val BLOCKED_PLAYER_CONSUMABLES = HashMap<UUID, MutableSet<ResourceLocation>>()
    private val BLOCKED_PLAYER_ATTACKABLES = HashMap<UUID, MutableSet<ResourceLocation>>()
    private val BLOCKED_PLAYER_CRAFTABLE = HashMap<UUID, MutableSet<ResourceLocation>>()

    @SubscribeEvent
    fun onItemUse(event: PlayerInteractEvent.RightClickItem) {
        val player = event.entity

        if (player.level().isClientSide || !this.isItemBlockedFor(player, player.mainHandItem, BlockScope.USE)) return

        triggerMessage(player, EventMessage.UNABLE_TO_USE)
        event.isCanceled = true
    }

    @SubscribeEvent
    fun onItemAttack(event: AttackEntityEvent) {
        val player = event.entity

        if (player.level().isClientSide || !this.isItemBlockedFor(player, player.mainHandItem, BlockScope.ATTACK)) return

        triggerMessage(player, EventMessage.UNABLE_TO_ATTACK)
        event.isCanceled = true
    }

    @SubscribeEvent
    fun onItemConsume(event: LivingEntityUseItemEvent.Start) {
        val player = (event.entity as? Player) ?: return

        if (player.level().isClientSide || !this.isItemBlockedFor(player, event.item, BlockScope.CONSUME)) return

        triggerMessage(player, EventMessage.UNABLE_TO_CONSUME)
        event.isCanceled = true
    }

    @SubscribeEvent
    fun onItemCrafted(event: ItemCraftedEvent) {
        val player = event.entity
        val result = event.crafting

        if (player.level().isClientSide || !isItemBlockedFor(player, result, BlockScope.CRAFT)) return

        result.count = 0

        triggerMessage(player, EventMessage.UNABLE_TO_CRAFT)

        returnIngredients(event.inventory, player)
        spawnDenyParticles(player)
        playDenySound(player)
    }

    @SubscribeEvent
    fun onAnvilUpdate(event: AnvilUpdateEvent) {
        val player = event.player

        if (player.level().isClientSide || !isItemBlockedFor(player.getUUID(), event.output, BlockScope.CRAFT)) return

        event.output = ItemStack.EMPTY
    }

    fun addBlockedItem(uuid: UUID, item: ResourceLocation, scope: BlockScope) {
        when(scope) {
            BlockScope.USE -> BLOCKED_PLAYER_USES.getOrPut(uuid) { mutableSetOf() }.add(item)
            BlockScope.ATTACK -> BLOCKED_PLAYER_ATTACKABLES.getOrPut(uuid) { mutableSetOf() }.add(item)
            BlockScope.CONSUME -> BLOCKED_PLAYER_CONSUMABLES.getOrPut(uuid) { mutableSetOf() }.add(item)
            BlockScope.CRAFT -> BLOCKED_PLAYER_CRAFTABLE.getOrPut(uuid) { mutableSetOf() }.add(item)
        }
    }

    fun removeBlockedItem(uuid: UUID, item: ResourceLocation, scope: BlockScope) {
        when(scope) {
            BlockScope.USE -> BLOCKED_PLAYER_USES.getOrPut(uuid) { mutableSetOf() }.remove(item)
            BlockScope.ATTACK -> BLOCKED_PLAYER_ATTACKABLES.getOrPut(uuid) { mutableSetOf() }.remove(item)
            BlockScope.CONSUME -> BLOCKED_PLAYER_CONSUMABLES.getOrPut(uuid) { mutableSetOf() }.remove(item)
            BlockScope.CRAFT -> BLOCKED_PLAYER_CRAFTABLE.getOrPut(uuid) { mutableSetOf() }.remove(item)
        }
    }

    fun clearBlockedItemsFor(uuid: UUID, scope: BlockScope) {
        when(scope) {
            BlockScope.USE -> BLOCKED_PLAYER_USES[uuid]?.clear()
            BlockScope.ATTACK -> BLOCKED_PLAYER_ATTACKABLES[uuid]?.clear()
            BlockScope.CONSUME -> BLOCKED_PLAYER_CONSUMABLES[uuid]?.clear()
            BlockScope.CRAFT -> BLOCKED_PLAYER_CRAFTABLE[uuid]?.clear()
        }
    }

    fun clearAllBlockedItemsFor(uuid: UUID) {
        BLOCKED_PLAYER_USES[uuid]?.clear()
        BLOCKED_PLAYER_ATTACKABLES[uuid]?.clear()
        BLOCKED_PLAYER_CONSUMABLES[uuid]?.clear()
        BLOCKED_PLAYER_CRAFTABLE[uuid]?.clear()
    }

    fun triggerMessage(player: Player, eventMessage: EventMessage) {
        val message = when (eventMessage) {
            EventMessage.UNABLE_TO_USE -> "§cSeems like you don't know how to use this item..."
            EventMessage.UNABLE_TO_ATTACK -> "§cYou feel to weak to use this weapon..."
            EventMessage.UNABLE_TO_CONSUME -> "§cYou don't know a proper way to consume this item..."
            EventMessage.UNABLE_TO_CRAFT -> "§cSeems like you don't know what do with theses items..."
            EventMessage.UNABLE_TO_FORGE -> "§cSeems like you don't know how to enchant this item..."
        }

        player.sendSystemMessage(Component.literal(message))
    }

    fun isItemBlockedFor(player: Player, item: ItemStack, scope: BlockScope): Boolean {
        if (item.isEmpty) return false

        return isItemBlockedFor(player.uuid, item, scope)
    }

    fun isItemBlockedFor(uuid: UUID, item: ItemStack, scope: BlockScope): Boolean {
        val keyOptional = item.itemHolder.unwrapKey()

        if (keyOptional.isEmpty) return false

        return isItemBlockedFor(uuid, keyOptional.get().location(), scope)
    }

    fun isItemBlockedFor(uuid: UUID, recipe: ResourceLocation, scope: BlockScope): Boolean {
        val blockedRecipes = this.getBlockedRecipes(uuid, scope)

        // TODO("Add tag support")
        val isBlocked = blockedRecipes.find { it == recipe }

        return (isBlocked != null)
    }

    fun getBlockedRecipes(uuid: UUID, scope: BlockScope): Set<ResourceLocation> {
        val blockedItems = when(scope) {
            BlockScope.USE -> BLOCKED_PLAYER_USES.getOrElse(uuid) { mutableSetOf() }
            BlockScope.ATTACK -> BLOCKED_PLAYER_ATTACKABLES.getOrElse(uuid) { mutableSetOf() }
            BlockScope.CONSUME -> BLOCKED_PLAYER_CONSUMABLES.getOrElse(uuid) { mutableSetOf() }
            BlockScope.CRAFT -> BLOCKED_PLAYER_CRAFTABLE.getOrElse(uuid) { mutableSetOf() }
        }

        return blockedItems
    }

    private fun returnIngredients(inventory: Container, player: Player) {
        for (i in 0..<inventory.containerSize) {
            val stack = inventory.getItem(i)

            if (!stack.isEmpty) {
                player.getInventory().placeItemBackInInventory(stack)
            }
        }
    }

    private fun spawnDenyParticles(player: Player) {
        val level = player.level() as ServerLevel

        level.sendParticles(
            ParticleTypes.SMOKE, player.x, player.y + 1, player.z, 20, 0.5, 0.5, 0.5, 0.05
        )
    }

    private fun playDenySound(player: Player) {
        val level = player.level()

        level.playSound(null, player.blockPosition(), SoundEvents.ANVIL_DESTROY, SoundSource.PLAYERS)
        level.playSound(null, player.blockPosition(), SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS)
    }

    enum class EventMessage {
        UNABLE_TO_USE, UNABLE_TO_ATTACK, UNABLE_TO_CONSUME, UNABLE_TO_CRAFT, UNABLE_TO_FORGE
    }
    enum class BlockScope { USE, ATTACK, CONSUME, CRAFT }
}