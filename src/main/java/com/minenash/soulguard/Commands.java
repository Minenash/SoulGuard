package com.minenash.soulguard;

import com.minenash.soulguard.config.ConfigManager;
import com.minenash.soulguard.souls.Soul;
import com.minenash.soulguard.souls.SoulManager;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.GameProfileArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static net.minecraft.server.command.CommandManager.literal;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.command.argument.GameProfileArgumentType.gameProfile;
import static com.mojang.brigadier.arguments.StringArgumentType.string;

public class Commands {

    public static void register() {
        CommandRegistrationCallback.EVENT.register(((dispatcher, dedicated) -> dispatcher.register(
                literal("soulguard")
                  .then( literal("reload").executes(Commands::reload))
                  .then( literal("listall").executes(Commands::listAll))
                  .then( literal("list").executes(Commands::list)
                    .then( argument("player", gameProfile()).executes(Commands::listPlayer)))
                  .then( literal("view")
                    .then( argument("soulId", string()).executes(Commands::viewSoul)))
        )));
    }

    private static int reload(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        context.getSource().getPlayer().sendMessage(new LiteralText("[SoulGuard] Config Reloaded"), false);
        ConfigManager.load();
        return 1;
    }

    private static int listAll(CommandContext<ServerCommandSource> context) {
        return SoulManager.listSouls(context.getSource(), null);
    }

    private static int list(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        return SoulManager.listSouls(context.getSource(), Collections.singletonList(context.getSource().getPlayer().getUuid()));
    }

    private static int listPlayer(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        return SoulManager.listSouls(context.getSource(), GameProfileArgumentType.getProfileArgument(context, "player").stream().map(GameProfile::getId).toList());
    }

    private static int viewSoul(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        context.getSource().sendFeedback(new LiteralText("§cView Soul Not Implemented Yet"), false);
        return 1;
    }

}
