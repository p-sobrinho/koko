package dev.koji.koko.server

import com.mojang.brigadier.arguments.DoubleArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import dev.koji.koko.Koko
import dev.koji.koko.common.SkillsHandler
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.commands.arguments.EntityArgument
import net.minecraft.network.chat.Component
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.common.EventBusSubscriber
import net.neoforged.neoforge.event.RegisterCommandsEvent

@EventBusSubscriber(modid = Koko.MOD_ID)
object ServerRegistry {
    // Inverse order due variable initialization.
    private val playerArgument = Commands.argument("player", EntityArgument.player()).executes(this::skillCommand)

    private val amountArgument = Commands.argument("amount", DoubleArgumentType.doubleArg())
        .then(playerArgument)

    private val skillArgument = Commands.argument("skill", StringArgumentType.string())
        .suggests { context, builder ->
            val skillModels = SkillsHandler.getSkillsModels(context.source.level) ?: emptySet()

            for (entry in skillModels) {
                builder.suggest(entry.key.location().path)
            }

            builder.buildFuture()
        }
        .then(amountArgument)

    private val modeArgument = Commands.argument("mode", StringArgumentType.string())
        .suggests { _, builder ->
            builder.suggest("add")
            builder.suggest("remove")

            builder.buildFuture()
        }
        .then(skillArgument)

    private val kokoCommand = Commands.literal("koko")
        .requires { it.hasPermission(2) }
        .then(modeArgument)

    @SubscribeEvent
    fun onRegisterCommands(event: RegisterCommandsEvent) {
        event.dispatcher.register(kokoCommand)
    }

    private fun skillCommand(commandStack: CommandContext<CommandSourceStack>): Int {
        val mode = StringArgumentType.getString(commandStack, "mode")
        val skill = StringArgumentType.getString(commandStack, "skill")
        val amount = DoubleArgumentType.getDouble(commandStack, "amount")
        val player = EntityArgument.getPlayer(commandStack, "player")

        val multiplier = if (mode == "add") 1 else - 1

        val namespacePath = Koko.namespacePath(skill)
        val playerSkill = SkillsHandler.getSkill(player, namespacePath)
            ?: return 0

        val beforeXp = playerSkill.xp

        SkillsHandler.updateXp(player, namespacePath, amount * multiplier)

        commandStack.source.sendSuccess({
            Component.literal("Edited ${player.name.string} experience of $skill from $beforeXp to ${beforeXp + amount * multiplier}!")
        }, false)

        return 1
    }
}