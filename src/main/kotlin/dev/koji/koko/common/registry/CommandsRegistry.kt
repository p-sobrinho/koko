package dev.koji.koko.common.registry

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

@EventBusSubscriber
object CommandsRegistry {
    private val skillBuilderCommand = Commands.literal("koko")
        .requires { it.hasPermission(2) }
        .then(
            Commands.argument("mode", StringArgumentType.string())
                .suggests { _, builder ->
                    builder.suggest("add")
                    builder.suggest("remove")

                    builder.buildFuture()
                }
                .then(
                    Commands.argument("skill", StringArgumentType.string())
                        .suggests { context, builder ->
                            val skillModels = SkillsHandler.getSkillsModels(context.source.level)

                            for (entry in skillModels) {
                                builder.suggest(entry.key.location().path)
                            }

                            builder.buildFuture()
                        }
                        .then(
                            Commands.argument("amount", DoubleArgumentType.doubleArg())
                                .then(
                                    Commands.argument("player", EntityArgument.player())
                                        .executes(this::skillCommand)
                                )
                        )
                )
        )


    @SubscribeEvent
    fun onRegisterCommands(event: RegisterCommandsEvent) {
        event.dispatcher.register(skillBuilderCommand)
    }

    private fun skillCommand(commandStack: CommandContext<CommandSourceStack>): Int {
        val mode = StringArgumentType.getString(commandStack, "mode")
        val skill = StringArgumentType.getString(commandStack, "skill")
        val amount = DoubleArgumentType.getDouble(commandStack, "amount")
        val player = EntityArgument.getPlayer(commandStack, "player")

        SkillsHandler.updateXp(player, Koko.namespacePath(skill), amount * (if (mode == "add") 1 else - 1))

        commandStack.source.sendSuccess({ Component.literal("Work!") }, false)
        return 1;
    }
}