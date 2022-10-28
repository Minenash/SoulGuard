package com.minenash.soulguard.commands;

import com.minenash.soulguard.SoulGuard;
import com.minenash.soulguard.config.Config;
import com.minenash.soulguard.config.ConfigManager;
import com.minenash.soulguard.inspect.InspectScreenHandlerFactory;
import com.minenash.soulguard.souls.Soul;
import com.minenash.soulguard.souls.SoulManager;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.minecraft.command.argument.GameProfileArgumentType;
import net.minecraft.entity.player.PlayerEntity;
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
        Predicate<ServerCommandSource> canInspect = s -> s.hasPermissionLevel(2) || Config.allowPlayersToInspectTheirSouls;
        CommandRegistrationCallback.EVENT.register(((dispatcher, dedicated) -> dispatcher.register(
                literal("soulguard")
                  .executes(Commands::list)
                  .then( literal("reload").requires(isOp).executes(Commands::reload))
                  .then( literal("seecapturedsouls").requires(isOp).executes(Commands::seecapturedsouls))
                  .then( literal("list").requires(isOp)
                    .executes(Commands::listAll)
                    .then( argument("player", gameProfile()).executes(Commands::listPlayer)))
                  .then( literal("delete").requires(isOp)
                    .then( argument("soulId", string()).executes(Commands::deleteSoul)))
                  .then( literal("inspect").requires(canInspect)
                    .then( argument("soulId", string()).executes(Commands::inspectSoul)))
                  .then( literal("release")
                    .then( argument("soulId", string()).executes(Commands::releaseSoul)))
                  .then( literal("lock").requires(isOp)
                    .then( argument("soulId", string()).executes(Commands::lockSoul)))
        )));

    }

    private static int reload(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        context.getSource().getPlayer().sendMessage(new LiteralText("[SoulGuard] Config Reloaded"), false);
        ConfigManager.load(true);
        return 1;
    }

    private static int list(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        return CommandHelper.listSouls(context.getSource(), Collections.singletonList(context.getSource().getPlayer().getUuid()), false, true);
    }

    private static int listAll(CommandContext<ServerCommandSource> context) {
        return CommandHelper.listSouls(context.getSource(), null, true, false);
    }

    private static int seecapturedsouls(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        PlayerEntity player = context.getSource().getPlayer();
        if (SoulGuard.CAN_SEE_BOUNDED_SOULS.contains(player)) {
            SoulGuard.CAN_SEE_BOUNDED_SOULS.remove(player);
            return feedback(context, "§aYou can no longer see captured souls", true);
        }
        SoulGuard.CAN_SEE_BOUNDED_SOULS.add(player);
        return feedback(context, "§aYou can now see captured souls", true);
    }

    private static int listPlayer(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        List<UUID> profiles = GameProfileArgumentType.getProfileArgument(context, "player").stream().map(GameProfile::getId).toList();
        return CommandHelper.listSouls(context.getSource(), profiles, profiles.size() > 1, false);
    }

    private static int deleteSoul(CommandContext<ServerCommandSource> context) {
        String soulId = StringArgumentType.getString(context, "soulId");

        if (!SoulManager.idToSoul.containsKey(soulId))
            return feedback(context, "§cThere's no soul with id: §e" + soulId, false);

        feedback(context, CommandHelper.infoText("§aSoul ", SoulManager.idToSoul.get(soulId), "§a has been deleted"), true);
        SoulManager.souls.remove( SoulManager.idToSoul.remove(soulId) );
        SoulManager.save();
        return 1;
    }

    private static int inspectSoul(CommandContext<ServerCommandSource> context) {
        String soulId = StringArgumentType.getString(context, "soulId");
        if (!SoulManager.idToSoul.containsKey(soulId))
            return feedback(context, "§cThere's no soul with id: §e" + soulId, false);

        try {
            context.getSource().getPlayer().openHandledScreen(InspectScreenHandlerFactory.get(SoulManager.idToSoul.get(soulId)));
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return 1;
//        return feedback(context, "§cInspect Soul Not Implemented Yet", false);

    }

    private static int releaseSoul(CommandContext<ServerCommandSource> context) {
        String soulId = StringArgumentType.getString(context, "soulId");
        if (!SoulManager.idToSoul.containsKey(soulId))
            return feedback(context, "§cThere's no soul with id: §e" + soulId, false);

        Soul soul = SoulManager.idToSoul.get(soulId);

        if (soul.releaseIn == -1 && !context.getSource().hasPermissionLevel(2))
            return feedback(context, CommandHelper.infoText("§cYou can no longer recapture soul §e", soul, ""), false);
        soul.released = !soul.released;
        if (soul.released)
            return feedback(context, CommandHelper.infoText("§aSoul ", SoulManager.idToSoul.get(soulId), "§a has been released"), true);
        return feedback(context, CommandHelper.infoText("§aSoul ", SoulManager.idToSoul.get(soulId), "§a has been recaptured"), true);
    }

    private static int lockSoul(CommandContext<ServerCommandSource> context) {
        String soulId = StringArgumentType.getString(context, "soulId");
        if (!SoulManager.idToSoul.containsKey(soulId))
            return feedback(context, "§cThere's no soul with id: §e" + soulId, false);

        Soul soul = SoulManager.idToSoul.get(soulId);
        soul.locked = !soul.locked;
        if (soul.locked)
            return feedback(context, CommandHelper.infoText("§aSoul ", SoulManager.idToSoul.get(soulId), "§a has been locked"), true);
        return feedback(context, CommandHelper.infoText("§aSoul ", SoulManager.idToSoul.get(soulId), "§a has been unlocked"), true);
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
