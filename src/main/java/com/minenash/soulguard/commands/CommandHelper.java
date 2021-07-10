package com.minenash.soulguard.commands;

import com.minenash.soulguard.SoulGuard;
import com.minenash.soulguard.config.Config;
import com.minenash.soulguard.souls.Soul;
import com.minenash.soulguard.souls.SoulManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.*;

import static net.minecraft.text.ClickEvent.Action.*;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class CommandHelper {

    public static int listSouls(ServerCommandSource sender, List<UUID> uuids, boolean showOwner, boolean canManage) {
        Collection<Soul> souls = uuids == null ? SoulManager.souls.values() : SoulManager.souls.values().stream().filter(s -> uuids.contains(s.player) ).toList();
        if (souls.isEmpty()) {
            sender.sendFeedback(new LiteralText("§cNo Souls to list"), false);
            return 0;
        }
        boolean op = sender.hasPermissionLevel(2);
        MutableText text = new LiteralText("\n§6==Souls==");
        for (Soul soul : souls)
            text.append( formatEntry(soul, showOwner, op, canManage) );
        sender.sendFeedback(text, false);
        return 1;
    }

    private static Text formatEntry(Soul soul, boolean showOwner, boolean op, boolean user) {
        String name = SoulGuard.getPlayer(soul.player);
        MutableText text = new LiteralText("\n");
        addHoverText(text, "§8[§6Info§8]", infoHover(soul));

        if (op && !user)
            addClickText(text, "§8[§cD§8]", "§cClick to Delete", "/soulguard delete " + soul.id, SUGGEST_COMMAND);

        if (Config.allowPlayersToInspectTheirSouls || (op && !user))
            addClickText(text, "§8[§aI§8]", "§aClick to Inspect", "/soulguard inspect " + soul.id, RUN_COMMAND);

        if (soul.released)
            addClickText(text, "§8[§eC§8]", "§eClick to Recapture", "/soulguard recapture " + soul.id, RUN_COMMAND);
        else
            addClickText(text, "§8[§eR§8]", "§eClick to Release", "/soulguard release " + soul.id, RUN_COMMAND);

        if (op && !user)
            if (soul.locked)
                addClickText(text, "§8[§bU§8]", "§bClick to Unlock", "/soulguard unlock " + soul.id, RUN_COMMAND);
            else
                addClickText(text, "§8[§bL§8]", "§bClick to Lock", "/soulguard lock " + soul.id, RUN_COMMAND);

        if (showOwner)
            addClickText(text, " §7Owner: §e" + name, "§eClick to show all souls from this player", "/soulguard list " + name, RUN_COMMAND);

        String pos = soul.getPositionString();
        if (Config.allowPlayersToTeleportToTheirSoul || (op && !user))
            addClickText(text, (showOwner ? " §7@ §e" : " §7Location: §e") + pos, "§eClick to Teleport and Collect", "/tp " + pos, RUN_COMMAND);
        else
            addClickText(text, " §7Location: §e" + pos, "Click to Copy Coordinates to Clipboard", pos, COPY_TO_CLIPBOARD);

        return text;
    }

    private static void addHoverText(MutableText text, String msg, String hover) {
        text.append(new LiteralText(msg).styled( style -> style
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new LiteralText(hover)))
        ));
    }

    private static void addClickText(MutableText text, String msg, String hover, String command, ClickEvent.Action action) {
        text.append(new LiteralText(msg).styled( style -> style
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new LiteralText(hover)))
                .withClickEvent(new ClickEvent(action, command))
        ));
    }

    public static Text infoText(String prefix, Soul soul, String suffix) {
        MutableText text = new LiteralText(prefix);
        addHoverText(text, "§e" + soul.id, infoHover(soul));
        return text.append(suffix);
    }

    public static String infoHover(Soul soul) {
        return "§7ID: §f" + soul.id
                + "\n§7Owner: §f" + SoulGuard.getPlayer(soul.player)
                + "\n§7Coordinates: §f" + soul.getPositionString()
                + "\n§7World: §f" + soul.worldId.getValue()
                + "\n§7Items Stored: §f" + soul.getItemCount()
                + "\n§7XP Stored: §f" + soul.experience
                + "\n§7Released: §f" + soul.released
                + "\n§7Locked: §f" + soul.locked;
    }

}
