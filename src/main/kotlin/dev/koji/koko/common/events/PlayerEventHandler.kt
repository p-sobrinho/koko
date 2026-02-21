package dev.koji.koko.common.events

import dev.koji.koko.common.SkillsHandler
import dev.koji.koko.common.models.sources.AbstractSkillSource
import dev.koji.koko.common.models.sources.SkillSourceFilter
import dev.koji.koko.common.models.sources.Sources
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.core.registries.Registries
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.tags.TagKey
import net.minecraft.world.Container
import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.effect.MobEffects
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.common.EventBusSubscriber
import net.neoforged.neoforge.event.AnvilUpdateEvent
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent
import net.neoforged.neoforge.event.entity.player.AnvilRepairEvent
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent
import net.neoforged.neoforge.event.entity.player.PlayerEvent
import net.neoforged.neoforge.event.entity.player.PlayerEvent.ItemCraftedEvent
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent
import net.neoforged.neoforge.event.tick.PlayerTickEvent
import java.time.Instant
import java.util.*


@EventBusSubscriber
object PlayerEventHandler {
    private val BLOCKED_PLAYER_INSTANCES = mutableSetOf<BlockedPlayerInstance>()

    private val MESSAGES_COOLDOWNS = HashMap<EventMessage, Instant>()

    @SubscribeEvent
    fun onPlayerJoin(event: PlayerEvent.PlayerLoggedInEvent) = this.doPlayerStuff(event.entity)

    @SubscribeEvent
    fun onPlayerRespawn(event: PlayerEvent.PlayerRespawnEvent) = this.doPlayerStuff(event.entity)

    @SubscribeEvent
    fun onPlayerChangedDimension(event: PlayerEvent.PlayerChangedDimensionEvent) = this.doPlayerStuff(event.entity)

    @SubscribeEvent
    fun onPlayerTick(event: PlayerTickEvent.Post) {
        val player = event.entity
        val level = player.level()

        if (level.isClientSide) return

        this.checkPlayerArmor(player)

        if (level.gameTime % 20 != 0L) return

        SkillsHandler.syncEffects(player)
    }

    @SubscribeEvent
    fun onItemUse(event: PlayerInteractEvent.RightClickItem) {
        val player = event.entity
        val item = player.mainHandItem

        if (this.isItemBlockedFor(player, item, BlockScope.USE)){
            event.isCanceled = true

            if (!player.level().isClientSide) return

            this.triggerMessage(player, EventMessage.UNABLE_TO_USE)
        }

        if (player.level().isClientSide) return

        this.processPlayerEvaluate(Sources.PLAYER_ITEM_USE, item, player as ServerPlayer)
    }

    @SubscribeEvent
    fun onItemAttack(event: AttackEntityEvent) {
        val player = event.entity
        val item = player.mainHandItem

        if (this.isItemBlockedFor(player, item, BlockScope.ATTACK)) {
            event.isCanceled = true

            if (!player.level().isClientSide) return

            this.triggerMessage(player, EventMessage.UNABLE_TO_ATTACK)
        }

        if (player.level().isClientSide) return

        this.processPlayerEvaluate(Sources.PLAYER_ATTACKED, item, player as ServerPlayer)
    }

    @SubscribeEvent
    fun onItemConsume(event: LivingEntityUseItemEvent.Start) {
        val player = (event.entity as? Player) ?: return
        val item = event.item

        if (this.isItemBlockedFor(player, item, BlockScope.CONSUME)) {

            event.isCanceled = true

            if (!player.level().isClientSide) return

            this.triggerMessage(player, EventMessage.UNABLE_TO_CONSUME)

            return
        }

        if (player.level().isClientSide) return

        this.processPlayerEvaluate(Sources.PLAYER_CONSUMED, item, player as ServerPlayer)
    }

    @SubscribeEvent
    fun onItemCrafted(event: ItemCraftedEvent) {
        val player = event.entity
        val result = event.crafting

        if (this.isItemBlockedFor(player, result, BlockScope.CRAFT)) {

            result.count = 0

            if (player.level().isClientSide) {
                this.triggerMessage(player, EventMessage.UNABLE_TO_CRAFT)

                this.spawnDenyParticles(player)
                this.playDenySound(player)
            } else { this.returnIngredients(event.inventory, player) }

            return
        }

        if (player.level().isClientSide) return

        this.processPlayerEvaluate(Sources.PLAYER_CRAFTED, result, player as ServerPlayer)
    }

    @SubscribeEvent
    fun onAnvilRepair(event: AnvilRepairEvent) {
        val player = event.entity

        if (player.level().isClientSide) return

        this.processPlayerEvaluate(Sources.PLAYER_FORGED, event.output, player as ServerPlayer)
    }

    @SubscribeEvent
    fun onAnvilUpdate(event: AnvilUpdateEvent) {
        val player = event.player

        if (!this.isItemBlockedFor(player, event.left, BlockScope.FORGE)) return

        event.isCanceled = true

        if (!player.level().isClientSide) return

        this.triggerMessage(player, EventMessage.UNABLE_TO_FORGE)
    }

    fun isItemBlockedFor(player: Player, item: ItemStack, scope: BlockScope): Boolean {
        if (item.isEmpty) return false

        return this.isItemBlockedFor(player.uuid, item, scope)
    }

    fun isItemBlockedFor(uuid: UUID, item: ItemStack, scope: BlockScope): Boolean {
        val blockedRecipes = this.getBlockedItemsInScope(uuid, scope)

        val isBlocked = blockedRecipes.find {
            if (item.`is`(TagKey.create(Registries.ITEM, it.location))) return@find true

            val keyOptional = item.itemHolder.unwrapKey()

            if (keyOptional.isEmpty) return false

            return@find keyOptional.get().location() == it
        }

        return (isBlocked != null)
    }

    fun getBlockedItemsInScope(uuid: UUID, scope: BlockScope): Set<BlockedItem> {
        val blockedItems = BLOCKED_PLAYER_INSTANCES.find { it.uuid == uuid }
            ?.blockedItems
            ?.filter { it.scopes.contains(scope) }
            ?.toSet()

        return blockedItems ?: emptySet()
    }

    fun getBlockedPlayerInstance(player: Player) =
        this.getBlockedPlayerInstance(player.uuid)

    fun getBlockedPlayerInstance(uuid: UUID): BlockedPlayerInstance {
        var foundInstance = BLOCKED_PLAYER_INSTANCES.find { it.uuid == uuid }

        if (foundInstance == null) {
            foundInstance = BlockedPlayerInstance(uuid, mutableSetOf())

            BLOCKED_PLAYER_INSTANCES.add(foundInstance)
        }

        return foundInstance
    }

    fun checkPlayerArmor(player: Player) {
        val playerInventory = player.inventory

        for (i in 36..39) {
            val armor = playerInventory.getItem(i)

            if (!this.isItemBlockedFor(player, armor, BlockScope.ARMOR)) continue

            player.addEffect(MobEffectInstance(
                MobEffects.MOVEMENT_SLOWDOWN,
                20, 3
            ))

            this.triggerMessage(player, EventMessage.UNABLE_TO_ARMOR)
        }
    }

    fun processPlayerEvaluate(
        source: String,
        item: ItemStack,
        player: Player
    ) {
        val listeners = SkillsHandler.getListenersFor(source, player.level())

        for (listener in listeners) {
            val xp = this.playerActionEvaluate(listener.sourceData, item)

            if (xp == 0.0) continue

            SkillsHandler.updateXp(player, listener.skill, xp)
        }
    }

    fun playerActionEvaluate(skillModel: AbstractSkillSource, item: ItemStack): Double =
        playerActionEvaluate(skillModel.filters, item)

    fun playerActionEvaluate(filters: List<SkillSourceFilter>, item: ItemStack): Double {
        if (filters.isEmpty()) return 0.0

        val whitelists = filters.filter { it.type == SkillSourceFilter.FilterType.WHITELIST }
            .sortedByDescending { it.priority }

        val blacklists = filters.filter { it.type == SkillSourceFilter.FilterType.BLACKLIST }
            .sortedByDescending { it.priority }

        for (blacklist in blacklists) {
            if (this.itemMatches(item, blacklist.target)) return 0.0
        }

        if (whitelists.isEmpty()) return 0.0

        for (whitelist in whitelists) {
            if (this.itemMatches(item, whitelist.target)) {
                val xp = whitelist.xp

                return if (whitelist.inverse) -xp else xp
            }
        }

        return 0.0
    }

    fun addBlockedItem(uuid: UUID, item: ResourceLocation, scope: BlockScope) {
        val blockedItems = this.getBlockedPlayerInstance(uuid).blockedItems
        var blockedItem = blockedItems.find { it.location == item }

        if (blockedItem == null) {
            blockedItem = BlockedItem(item, mutableSetOf())

            blockedItems.add(blockedItem)
        }

        blockedItem.scopes.add(scope)
    }

    fun removeBlockedItem(uuid: UUID, item: ResourceLocation, scope: BlockScope) {
        val blockedItem = this.getBlockedPlayerInstance(uuid).blockedItems.find { it.location == item }

        if (blockedItem == null) return

        blockedItem.scopes.removeIf { it == scope }
    }

    fun clearBlockedItemsFor(uuid: UUID, scope: BlockScope) {
        for (blockedItem in this.getBlockedPlayerInstance(uuid).blockedItems) {
            blockedItem.scopes.removeIf { it == scope }
        }
    }

    fun clearAllBlockedItemsFor(uuid: UUID) = this.getBlockedPlayerInstance(uuid).blockedItems.clear()

    fun triggerMessage(player: Player, eventMessage: EventMessage) {
        val cooldown = MESSAGES_COOLDOWNS.get(eventMessage)

        if (cooldown != null && cooldown.isAfter(Instant.now())) return

        val message = when (eventMessage) {
            EventMessage.UNABLE_TO_USE -> "§cYou don't know how to use this item..."
            EventMessage.UNABLE_TO_ATTACK -> "§cYou feel too weak to wield this weapon..."
            EventMessage.UNABLE_TO_CONSUME -> "§cYou don't know how to properly consume this item..."
            EventMessage.UNABLE_TO_CRAFT -> "§cYou have no idea what to do with these items..."
            EventMessage.UNABLE_TO_FORGE -> "§cYou don't know how to enchant this item..."
            EventMessage.UNABLE_TO_ARMOR -> "§cYou feel to weak to support this armor..."
            EventMessage.UNABLE_TO_CURIOS -> "§cYou aren't worth of using this curio..."
        }

        MESSAGES_COOLDOWNS[eventMessage] = Instant.now().plusSeconds(1)

        player.sendSystemMessage(Component.literal(message))
    }

    fun itemMatches(item: ItemStack, targetLocation: String): Boolean {
        val resourceLocation = SkillsHandler.safeParseResource(targetLocation)

        if (item.`is`(TagKey.create(Registries.ITEM, resourceLocation))) return true

        val keyOptional = item.itemHolder.unwrapKey()

        if (keyOptional.isEmpty) return false

        return keyOptional.get().location() == resourceLocation
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
        UNABLE_TO_USE, UNABLE_TO_ATTACK, UNABLE_TO_CONSUME, UNABLE_TO_CRAFT, UNABLE_TO_FORGE, UNABLE_TO_ARMOR,
        UNABLE_TO_CURIOS
    }

    enum class BlockScope {
        USE, ATTACK, CONSUME, CRAFT, FORGE, ARMOR, CURIOS, ISPELL
    }

    data class BlockedPlayerInstance(
        val uuid: UUID,
        val blockedItems: MutableSet<BlockedItem>,
    ) {
        override fun equals(other: Any?): Boolean {
            return other is BlockedPlayerInstance && other.uuid == this.uuid
        }

        override fun hashCode(): Int = uuid.hashCode()
    }

    data class BlockedItem(val location: ResourceLocation, val scopes: MutableSet<BlockScope>)
}