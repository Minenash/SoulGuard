package com.minenash.soulguard.commands;

import com.minenash.soulguard.SoulGuard;
import com.minenash.soulguard.souls.Soul;
import com.minenash.soulguard.souls.SoulManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.*;

import java.util.List;
import java.util.UUID;

public class CommandHelper {

    public static int listSouls(ServerCommandSource sender, List<UUID> uuids, boolean showOwner, boolean canManage) {
        List<Soul> souls = uuids == null ? SoulManager.souls : SoulManager.souls.stream().filter(s -> uuids.contains(s.player) ).toList();
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

    private static Text formatEntry(Soul soul, boolean showOwner, boolean op, boolean canManage) {
        String name = SoulGuard.getPlayer(soul.player);
        MutableText text = new LiteralText("\n");
        text.append(new LiteralText("§8[§6Info§8]").styled( style -> style
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new LiteralText(infoHover(soul, name))))));

        if (op && !canManage)
            text.append( clickableMessage("§8[§cD§8]", "§cClick to Delete", "/soulguard delete " + soul.id, true));

        if (op || canManage) {
            text.append( clickableMessage("§8[§aI§8]", "§aClick to Inspect", "/soulguard inspect " + soul.id, false));
            if (soul.released)
                text.append( clickableMessage("§8[§eR§8]", "§bClick to Release", "/soulguard release " + soul.id, false));
            else
                text.append( clickableMessage("§8[§eC§8]", "§bClick to Recapture", "/soulguard recapture " + soul.id, false));
        }

        if (op && !canManage)
            if (soul.locked)
                text.append( clickableMessage("§8[§bU§8]", "§bClick to Unlock", "/soulguard unlock " + soul.id, false));
            else
                text.append( clickableMessage("§8[§bL§8]", "§bClick to Lock", "/soulguard lock " + soul.id, false));

        if (showOwner)
            text.append(clickableMessage(" §7Owner: §e" + name, "§eClick to show all souls from this player", "/soulguard list " + name, false));

        text.append(clickableMessage((showOwner ? " §7@ §e" : " §7Location: §e") + soul.getPositionString(), "§eClick to Teleport and Collect", "/tp " + soul.getPositionString(), false));
        return text;
    }

    private static Text clickableMessage(String msg, String hover, String command, boolean suggest) {
        return new LiteralText(msg).styled( style -> style
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new LiteralText(hover)))
                .withClickEvent(new ClickEvent(suggest ? ClickEvent.Action.SUGGEST_COMMAND : ClickEvent.Action.RUN_COMMAND, command))
        );
    }

    private static String infoHover(Soul soul, String owner) {
        return "§7ID: §f" + soul.id
                + "\n§7Owner: §f" + owner
                + "\n§7Coordinates: §f" + soul.getPositionString()
                + "\n§7World: §f" + soul.worldId.getValue()
                + "\n§7Items Stored: §f" + soul.getItemCount()
                + "\n§7XP Stored: §f" + soul.experience
                + "\n§7Released: §f" + soul.released
                + "\n§7Locked: §f" + soul.locked;
    }

}
