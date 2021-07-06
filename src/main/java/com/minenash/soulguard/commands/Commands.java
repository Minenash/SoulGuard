package com.minenash.soulguard.commands;

import com.minenash.soulguard.config.ConfigManager;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.minecraft.command.argument.GameProfileArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.*;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

import static net.minecraft.server.command.CommandManager.literal;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.command.argument.GameProfileArgumentType.gameProfile;
import static com.mojang.brigadier.arguments.StringArgumentType.string;

public class Commands {

    public static void register() {
        Predicate<ServerCommandSource> isOp = s -> s.hasPermissionLevel(2);
        Predicate<ServerCommandSource> canInspect = s -> s.hasPermissionLevel(2); // || Config.allowPlayerInspectOwnSoul
        CommandRegistrationCallback.EVENT.register(((dispatcher, dedicated) -> dispatcher.register(
                literal("soulguard")
                  .executes(Commands::list)
                  .then( literal("reload").requires(isOp).executes(Commands::reload))
                  .then( literal("listall").requires(isOp).executes(Commands::listAll))
                  .then( literal("list").requires(isOp)
                    .then( argument("player", gameProfile()).executes(Commands::listPlayer)))
                  .then( literal("delete").requires(isOp)
                    .then( argument("soulId", string()).executes(Commands::deleteSoul)))
                  .then( literal("inspect").requires(canInspect)
                    .then( argument("soulId", string()).executes(Commands::inspectSoul)))
                  .then( literal("release")
                    .then( argument("soulId", string()).executes(Commands::recaptureSoul)))
                  .then( literal("recapture")
                    .then( argument("soulId", string()).executes(Commands::recaptureSoul)))
                  .then( literal("lock").requires(isOp)
                    .then( argument("soulId", string()).executes(Commands::lockSoul)))
                  .then( literal("unlock").requires(isOp)
                    .then( argument("soulId", string()).executes(Commands::unlockSoul)))
        )));
    }

    private static int reload(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        context.getSource().getPlayer().sendMessage(new LiteralText("[SoulGuard] Config Reloaded"), false);
        ConfigManager.load();
        return 1;
    }

    private static int listAll(CommandContext<ServerCommandSource> context) {
        return CommandHelper.listSouls(context.getSource(), null, true, false);
    }

    private static int list(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        return CommandHelper.listSouls(context.getSource(), Collections.singletonList(context.getSource().getPlayer().getUuid()), false, true);
    }

    private static int listPlayer(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        List<UUID> profiles = GameProfileArgumentType.getProfileArgument(context, "player").stream().map(GameProfile::getId).toList();
        return CommandHelper.listSouls(context.getSource(), profiles, profiles.size() > 1,false);
    }

    private static int deleteSoul(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        context.getSource().sendFeedback(new LiteralText("§cDelete Soul Not Implemented Yet"), false);
        return 1;
    }

    private static int inspectSoul(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        context.getSource().sendFeedback(new LiteralText("§cInspect Soul Not Implemented Yet"), false);
        return 1;
    }

    private static int releaseSoul(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        context.getSource().sendFeedback(new LiteralText("§cRelease Soul Not Implemented Yet"), false);
        return 1;
    }

    private static int recaptureSoul(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        context.getSource().sendFeedback(new LiteralText("§cRecapture Soul Not Implemented Yet"), false);
        return 1;
    }

    private static int lockSoul(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        context.getSource().sendFeedback(new LiteralText("§cLock Soul Not Implemented Yet"), false);
        return 1;
    }

    private static int unlockSoul(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        context.getSource().sendFeedback(new LiteralText("§cUnlock Soul Not Implemented Yet"), false);
        return 1;
    }

}
