package dev.koji.koko.common.events

import dev.koji.koko.Koko
import dev.koji.koko.common.SkillsHandler
import dev.koji.koko.common.models.sources.AbstractSkillSource
import dev.koji.koko.common.models.sources.DefaultSources
import dev.koji.koko.common.models.sources.SkillSourceFilter
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer
import net.minecraft.tags.TagKey
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.Mob
import net.minecraft.world.entity.animal.Animal
import net.minecraft.world.entity.boss.enderdragon.EnderDragon
import net.minecraft.world.entity.boss.wither.WitherBoss
import net.minecraft.world.entity.monster.ElderGuardian
import net.minecraft.world.entity.monster.warden.Warden
import net.minecraft.world.entity.player.Player
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.common.EventBusSubscriber
import net.neoforged.neoforge.event.ItemStackedOnOtherEvent
import net.neoforged.neoforge.event.entity.living.AnimalTameEvent
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent

@EventBusSubscriber(modid = Koko.MOD_ID)
object EntityEventHandler {
    @SubscribeEvent
    fun onEntityDeath(event: LivingDeathEvent) {
        if (event.entity.level().isClientSide) return

        val source = event.source
        val attacker = source.entity

        if (attacker !is ServerPlayer) return

        val killedEntity = event.entity

        processEntityEvaluation(DefaultSources.ENTITY_KILL, killedEntity, attacker)
    }

    @SubscribeEvent
    fun onEntityInteract(event: PlayerInteractEvent.EntityInteract) {
        if (event.entity.level().isClientSide) return

        val player = event.entity
        val targetEntity = event.target

        if (player !is ServerPlayer) return

        processEntityEvaluation("entity/interact", targetEntity, player)
    }

    @SubscribeEvent
    fun onItemTrade(event: ItemStackedOnOtherEvent) {
        val player = event.player

        if (player.level().isClientSide) return

        val container = event.slot.container

        if (container !is Entity || player !is ServerPlayer) return

        processEntityEvaluation("entity/trade", container, player)
    }

    @SubscribeEvent
    fun onEntityTame(event: AnimalTameEvent) {
        val player = event.tamer

        if (player.level().isClientSide) return

        val tamedEntity = event.entity

        if (player !is ServerPlayer) return

        processEntityEvaluation("entity/tame", tamedEntity, player)
    }

    fun processEntityEvaluation(
        source: String,
        entity: Entity,
        player: ServerPlayer
    ) {
        val listeners = SkillsHandler.getListenersFor(source, player.level())

        for (listener in listeners) {
            val xp = this.entityEvaluate(listener.sourceData, entity)

            if (xp == 0.0) continue

            SkillsHandler.updateXp(player, listener.skill, xp)
        }
    }

    fun entityEvaluate(skillModel: AbstractSkillSource, entity: Entity): Double =
        entityEvaluate(skillModel.filters, entity)

    fun entityEvaluate(filters: List<SkillSourceFilter>, entity: Entity): Double {
        if (filters.isEmpty()) return 0.0

        val whitelists = filters.filter { it.type == SkillSourceFilter.FilterType.WHITELIST }
            .sortedByDescending { it.priority }

        val blacklists = filters.filter { it.type == SkillSourceFilter.FilterType.BLACKLIST }
            .sortedByDescending { it.priority }

        for (blacklist in blacklists) {
            if (matchesEntity(entity, blacklist.target)) {
                return 0.0
            }
        }

        if (whitelists.isEmpty()) return 0.0

        for (whitelist in whitelists) {
            if (matchesEntity(entity, whitelist.target)) {
                val xp = whitelist.xp
                return if (whitelist.inverse) -xp else xp
            }
        }

        return 0.0
    }

    private fun matchesEntity(entity: Entity, target: String): Boolean {
        val resourceLocation = if (target.contains(":")) {
            ResourceLocation.parse(target)
        } else {
            ResourceLocation.fromNamespaceAndPath("minecraft", target)
        }

        val entityTypeKey = ResourceKey.create(Registries.ENTITY_TYPE, resourceLocation)
        val entityType = BuiltInRegistries.ENTITY_TYPE.get(entityTypeKey)

        if (entity.type == entityType) return true

        val tagKey = TagKey.create(Registries.ENTITY_TYPE, resourceLocation)

        if (entity.type.`is`(tagKey)) return true

        return when (target) {
            "boss" -> (entity is EnderDragon || entity is WitherBoss || entity is ElderGuardian || entity is Warden)
            "hostile" -> entity is Mob && entity.isAggressive
            "animal" -> entity is Animal
            "player" -> entity is Player
            "living" -> entity is LivingEntity
            else -> false
        }
    }
}