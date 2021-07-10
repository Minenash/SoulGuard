package com.minenash.soulguard.commands;

import com.minenash.soulguard.config.ConfigManager;
import com.minenash.soulguard.souls.Soul;
import com.minenash.soulguard.souls.SoulManager;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.arguments.StringArgumentType;
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
                    .then( argument("soulId", string()).executes(Commands::releaseSoul)))
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

    private static int deleteSoul(CommandContext<ServerCommandSource> context) {
        String soulId = StringArgumentType.getString(context, "soulId");

        if (!SoulManager.souls.containsKey(soulId))
            return feedback(context, "§cThere's no soul with id: §e" + soulId, false);

        feedback(context, CommandHelper.infoText("§aSoul ", SoulManager.souls.get(soulId), "§a has been deleted"), true);
        SoulManager.souls.remove(soulId);
        SoulManager.save();
        return 1;
    }

    private static int inspectSoul(CommandContext<ServerCommandSource> context) {
        String soulId = StringArgumentType.getString(context, "soulId");
        if (!SoulManager.souls.containsKey(soulId))
            return feedback(context, "§cThere's no soul with id: §e" + soulId, false);
        return feedback(context, "§cInspect Soul Not Implemented Yet", false);

    }

    private static int releaseSoul(CommandContext<ServerCommandSource> context) {
        String soulId = StringArgumentType.getString(context, "soulId");
        if (!SoulManager.souls.containsKey(soulId))
            return feedback(context, "§cThere's no soul with id: §e" + soulId, false);

        Soul soul = SoulManager.souls.get(soulId);
        if (soul.released)
            return feedback(context, CommandHelper.infoText("§aSoul ", SoulManager.souls.get(soulId), "§a was already released"), true);

        soul.released = true;
        return feedback(context, CommandHelper.infoText("§aSoul ", SoulManager.souls.get(soulId), "§a has been released"), true);
    }

    private static int recaptureSoul(CommandContext<ServerCommandSource> context) {
        String soulId = StringArgumentType.getString(context, "soulId");
        if (!SoulManager.souls.containsKey(soulId))
            return feedback(context, "§cThere's no soul with id: §e" + soulId, false);

        Soul soul = SoulManager.souls.get(soulId);
        if (!soul.released)
            return feedback(context, CommandHelper.infoText("§aSoul ", SoulManager.souls.get(soulId), "§a wasn't released"), true);

        soul.released = false;
        return feedback(context, CommandHelper.infoText("§aSoul ", SoulManager.souls.get(soulId), "§a has been recaptured"), true);
    }

    private static int lockSoul(CommandContext<ServerCommandSource> context) {
        String soulId = StringArgumentType.getString(context, "soulId");
        if (!SoulManager.souls.containsKey(soulId))
            return feedback(context, "§cThere's no soul with id: §e" + soulId, false);

        Soul soul = SoulManager.souls.get(soulId);
        if (soul.locked)
            return feedback(context, CommandHelper.infoText("§aSoul ", SoulManager.souls.get(soulId), "§a was already locked"), true);

        soul.locked = true;
        return feedback(context, CommandHelper.infoText("§aSoul ", SoulManager.souls.get(soulId), "§a has been locked"), true);
    }

    private static int unlockSoul(CommandContext<ServerCommandSource> context) {
        String soulId = StringArgumentType.getString(context, "soulId");
        if (!SoulManager.souls.containsKey(soulId))
            return feedback(context, "§cThere's no soul with id: §e" + soulId, false);

        Soul soul = SoulManager.souls.get(soulId);
        if (!soul.locked)
            return feedback(context, CommandHelper.infoText("§aSoul ", SoulManager.souls.get(soulId), "§a was already unlocked"), true);

        soul.locked = false;
        return feedback(context, CommandHelper.infoText("§aSoul ", SoulManager.souls.get(soulId), "§a has been unlocked"), true);
    }

    private static int feedback(CommandContext<ServerCommandSource> context, String msg, boolean pass) {
        context.getSource().sendFeedback(new LiteralText(msg), false);
        return pass ? 1 : 0;
    }
    private static int feedback(CommandContext<ServerCommandSource> context, Text text, boolean pass) {
        context.getSource().sendFeedback(text, false);
        return pass ? 1 : 0;
    }

}
