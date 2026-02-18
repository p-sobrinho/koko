package dev.koji.koko.common.events

import dev.koji.koko.common.SkillsHandler
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.core.registries.Registries
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerLevel
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.tags.TagKey
import net.minecraft.world.Container
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.common.EventBusSubscriber
import net.neoforged.neoforge.event.AnvilUpdateEvent
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent
import net.neoforged.neoforge.event.entity.player.PlayerEvent
import net.neoforged.neoforge.event.entity.player.PlayerEvent.ItemCraftedEvent
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent
import java.time.Instant
import java.util.*


@EventBusSubscriber
object PlayerEventHandler {
    private val BLOCKED_PLAYER_USES = HashMap<UUID, MutableSet<ResourceLocation>>()
    private val BLOCKED_PLAYER_CONSUMABLES = HashMap<UUID, MutableSet<ResourceLocation>>()
    private val BLOCKED_PLAYER_ATTACKABLES = HashMap<UUID, MutableSet<ResourceLocation>>()
    private val BLOCKED_PLAYER_CRAFTABLE = HashMap<UUID, MutableSet<ResourceLocation>>()
    private val BLOCKED_PLAYER_FORGE = HashMap<UUID, MutableSet<ResourceLocation>>()

    private val MESSAGES_COOLDOWNS = HashMap<EventMessage, Instant>()

    @SubscribeEvent
    fun onPlayerJoin(event: PlayerEvent.PlayerLoggedInEvent) = this.doPlayerStuff(event.entity)

    @SubscribeEvent
    fun onPlayerRespawn(event: PlayerEvent.PlayerRespawnEvent) = this.doPlayerStuff(event.entity)

    @SubscribeEvent
    fun onPlayerChangedDimension(event: PlayerEvent.PlayerChangedDimensionEvent) = this.doPlayerStuff(event.entity)

    @SubscribeEvent
    fun onItemUse(event: PlayerInteractEvent.RightClickItem) {
        val player = event.entity

        if (!this.isItemBlockedFor(player, player.mainHandItem, BlockScope.USE)) return

        event.isCanceled = true

        if (!player.level().isClientSide) return

        this.triggerMessage(player, EventMessage.UNABLE_TO_USE)
    }

    @SubscribeEvent
    fun onItemAttack(event: AttackEntityEvent) {
        val player = event.entity

        if (!this.isItemBlockedFor(player, player.mainHandItem, BlockScope.ATTACK)) return

        event.isCanceled = true

        if (!player.level().isClientSide) return
        this.triggerMessage(player, EventMessage.UNABLE_TO_ATTACK)
    }

    @SubscribeEvent
    fun onItemConsume(event: LivingEntityUseItemEvent.Start) {
        val player = (event.entity as? Player) ?: return

        if (!this.isItemBlockedFor(player, event.item, BlockScope.CONSUME)) return

        event.isCanceled = true

        if (!player.level().isClientSide) return
        this.triggerMessage(player, EventMessage.UNABLE_TO_CONSUME)
    }

    @SubscribeEvent
    fun onItemCrafted(event: ItemCraftedEvent) {
        val player = event.entity
        val result = event.crafting

        if (!this.isItemBlockedFor(player, result, BlockScope.CRAFT)) return

        result.count = 0

        if (player.level().isClientSide) {
            this.triggerMessage(player, EventMessage.UNABLE_TO_CRAFT)

            spawnDenyParticles(player)
            playDenySound(player)
        } else { returnIngredients(event.inventory, player) }
    }

    @SubscribeEvent
    fun onAnvilUpdate(event: AnvilUpdateEvent) {
        val player = event.player

        if (!this.isItemBlockedFor(player, event.left, BlockScope.FORGE))
            return

        event.isCanceled = true

        if (!player.level().isClientSide) return

        this.triggerMessage(player, EventMessage.UNABLE_TO_FORGE)
    }

    fun addBlockedItem(uuid: UUID, item: ResourceLocation, scope: BlockScope) {
        when(scope) {
            BlockScope.USE -> BLOCKED_PLAYER_USES.getOrPut(uuid) { mutableSetOf() }.add(item)
            BlockScope.ATTACK -> BLOCKED_PLAYER_ATTACKABLES.getOrPut(uuid) { mutableSetOf() }.add(item)
            BlockScope.CONSUME -> BLOCKED_PLAYER_CONSUMABLES.getOrPut(uuid) { mutableSetOf() }.add(item)
            BlockScope.CRAFT -> BLOCKED_PLAYER_CRAFTABLE.getOrPut(uuid) { mutableSetOf() }.add(item)
            BlockScope.FORGE -> BLOCKED_PLAYER_FORGE.getOrPut(uuid) { mutableSetOf() }.add(item)
        }
    }

    fun removeBlockedItem(uuid: UUID, item: ResourceLocation, scope: BlockScope) {
        when(scope) {
            BlockScope.USE -> BLOCKED_PLAYER_USES.getOrPut(uuid) { mutableSetOf() }.remove(item)
            BlockScope.ATTACK -> BLOCKED_PLAYER_ATTACKABLES.getOrPut(uuid) { mutableSetOf() }.remove(item)
            BlockScope.CONSUME -> BLOCKED_PLAYER_CONSUMABLES.getOrPut(uuid) { mutableSetOf() }.remove(item)
            BlockScope.CRAFT -> BLOCKED_PLAYER_CRAFTABLE.getOrPut(uuid) { mutableSetOf() }.remove(item)
            BlockScope.FORGE -> BLOCKED_PLAYER_FORGE.getOrPut(uuid) { mutableSetOf() }.remove(item)
        }
    }

    fun clearBlockedItemsFor(uuid: UUID, scope: BlockScope) {
        when(scope) {
            BlockScope.USE -> BLOCKED_PLAYER_USES[uuid]?.clear()
            BlockScope.ATTACK -> BLOCKED_PLAYER_ATTACKABLES[uuid]?.clear()
            BlockScope.CONSUME -> BLOCKED_PLAYER_CONSUMABLES[uuid]?.clear()
            BlockScope.CRAFT -> BLOCKED_PLAYER_CRAFTABLE[uuid]?.clear()
            BlockScope.FORGE -> BLOCKED_PLAYER_FORGE[uuid]?.clear()
        }
    }

    fun clearAllBlockedItemsFor(uuid: UUID) {
        BLOCKED_PLAYER_USES[uuid]?.clear()
        BLOCKED_PLAYER_ATTACKABLES[uuid]?.clear()
        BLOCKED_PLAYER_CONSUMABLES[uuid]?.clear()
        BLOCKED_PLAYER_CRAFTABLE[uuid]?.clear()
        BLOCKED_PLAYER_FORGE[uuid]?.clear()
    }

    fun triggerMessage(player: Player, eventMessage: EventMessage) {
        val cooldown = MESSAGES_COOLDOWNS.get(eventMessage)

        if (cooldown != null && cooldown.isAfter(Instant.now())) return

        val message = when (eventMessage) {
            EventMessage.UNABLE_TO_USE -> "§cYou don't know how to use this item..."
            EventMessage.UNABLE_TO_ATTACK -> "§cYou feel too weak to wield this weapon..."
            EventMessage.UNABLE_TO_CONSUME -> "§cYou don't know how to properly consume this item..."
            EventMessage.UNABLE_TO_CRAFT -> "§cYou have no idea what to do with these items..."
            EventMessage.UNABLE_TO_FORGE -> "§cYou don't know how to enchant this item..."
        }

        MESSAGES_COOLDOWNS[eventMessage] = Instant.now().plusSeconds(1)

        player.sendSystemMessage(Component.literal(message))
    }

    fun isItemBlockedFor(player: Player, item: ItemStack, scope: BlockScope): Boolean {
        if (item.isEmpty) return false

        return isItemBlockedFor(player.uuid, item, scope)
    }

    fun isItemBlockedFor(uuid: UUID, item: ItemStack, scope: BlockScope): Boolean {
        val blockedRecipes = this.getBlockedItems(uuid, scope)

        val isBlocked = blockedRecipes.find {
            if (item.`is`(TagKey.create(Registries.ITEM, it))) return@find true

            val keyOptional = item.itemHolder.unwrapKey()

            if (keyOptional.isEmpty) return false

            return@find keyOptional.get().location() == it
        }

        return (isBlocked != null)
    }

    fun getBlockedItems(uuid: UUID, scope: BlockScope): Set<ResourceLocation> {
        val blockedItems = when(scope) {
            BlockScope.USE -> BLOCKED_PLAYER_USES.getOrElse(uuid) { mutableSetOf() }
            BlockScope.ATTACK -> BLOCKED_PLAYER_ATTACKABLES.getOrElse(uuid) { mutableSetOf() }
            BlockScope.CONSUME -> BLOCKED_PLAYER_CONSUMABLES.getOrElse(uuid) { mutableSetOf() }
            BlockScope.CRAFT -> BLOCKED_PLAYER_CRAFTABLE.getOrElse(uuid) { mutableSetOf() }
            BlockScope.FORGE -> BLOCKED_PLAYER_FORGE.getOrElse(uuid) { mutableSetOf() }
        }

        return blockedItems
    }

    private fun doPlayerStuff(player: Player) {
        SkillsHandler.syncNewSkills(player)
        SkillsHandler.syncPlayerSkills(player)
        SkillsHandler.syncEffects(player)
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
    enum class BlockScope { USE, ATTACK, CONSUME, CRAFT, FORGE }
}