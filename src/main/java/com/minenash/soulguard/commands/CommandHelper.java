package com.minenash.soulguard.commands;

import com.minenash.soulguard.SoulGuard;
import com.minenash.soulguard.config.Config;
import com.minenash.soulguard.souls.Soul;
import com.minenash.soulguard.souls.SoulManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.*;

import static net.minecraft.text.ClickEvent.Action.*;

import java.text.SimpleDateFormat;
import java.util.*;

public class CommandHelper {

    public static int listSouls(ServerCommandSource sender, List<UUID> uuids, boolean showOwner, boolean selfList) {
        if (uuids != null && uuids.isEmpty() ) {
            sender.sendFeedback(new LiteralText("§cNo players to list souls for"), false);
            return 0;
        }
        Collection<Soul> souls = uuids == null ? SoulManager.souls : SoulManager.souls.stream().filter(s -> uuids.contains(s.player) ).toList();
        if (souls.isEmpty()) {
            sender.sendFeedback(new LiteralText("§cNo Souls to list"), false);
            return 0;
        }

        boolean op = sender.hasPermissionLevel(2);
        MutableText text =  new LiteralText(selfList || showOwner ? "\n§6==Souls==" : "\n§6==" + SoulGuard.getPlayer(uuids.get(0)) + "'s Souls==");
        for (Soul soul : souls)
            text.append( formatEntry(soul, showOwner, op) );
        sender.sendFeedback(text, false);
        return 1;
    }

    public static Text formatEntry(Soul soul, boolean showOwner, boolean op) {
        String name = SoulGuard.getPlayer(soul.player);
        MutableText text = new LiteralText("\n");
        addHoverText(text, "§8[§6Info§8]", infoHover(soul));

        if (op)
            addClickText(text, "§8[§cD§8]", "§cClick to Delete", "/soulguard delete " + soul.id, SUGGEST_COMMAND);

        if (Config.allowPlayersToInspectTheirSouls || op )
            addClickText(text, "§8[§aI§8]", "§aClick to Inspect", "/soulguard inspect " + soul.id, RUN_COMMAND);

        if (!op && soul.releaseIn == -1)
            addHoverText(text, "§8[§e_§8]", "§eThe soul can no longer be recapture");
        else if (soul.released)
            addClickText(text, "§8[§eC§8]", "§eClick to Recapture", "/soulguard release " + soul.id, RUN_COMMAND);
        else
            addClickText(text, "§8[§eR§8]", "§eClick to Release", "/soulguard release " + soul.id, RUN_COMMAND);

        if (op)
            if (soul.locked)
                addClickText(text, "§8[§bU§8]", "§bClick to Unlock", "/soulguard lock " + soul.id, RUN_COMMAND);
            else
                addClickText(text, "§8[§bL§8]", "§bClick to Lock", "/soulguard lock " + soul.id, RUN_COMMAND);

        if (showOwner)
            addClickText(text, " §7Owner: §e" + name, "§eClick to show all souls from this player", "/soulguard list " + name, RUN_COMMAND);

        String pos = soul.getPositionString();

        text.append(showOwner ? " §7@ " : " §7Location: §e");
        if (Config.allowPlayersToTeleportToTheirSoul || op)
            addClickText(text, "§e" + pos, "§eClick to Teleport and Collect", "/execute in " + soul.worldId.getValue() + " run tp " + pos, RUN_COMMAND);
        else
            addClickText(text, "§e" + pos, "Click to Copy Coordinates to Clipboard", pos, COPY_TO_CLIPBOARD);

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

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("MMM dd, HH:mm");
    private static final SimpleDateFormat DATE_FORMAT_WITH_TIMEZONE = new SimpleDateFormat("MMM dd, HH:mm zzz");
    public static String infoHover(Soul soul) {
        return "§7ID: §f" + soul.id
                + "\n§7Owner: §f" + SoulGuard.getPlayer(soul.player)
                + "\n§7Coordinates: §f" + soul.getPositionString()
                + "\n§7World: §f" + soul.worldId.getValue()
                + "\n§7Items Stored: §f" + soul.getItemCount()
                + "\n§7XP Stored: §f" + soul.experience
                + "\n§7Status: §f" + (soul.released ? "Released " : "Captured ") + (soul.locked ? "& Locked" : "")
                + "\n"
                + "\n§7Created: §f" + getTimeStamp(soul.createdAt)
                + getTimeLeft("\n§7Releases: §f", soul.releaseIn)
                + getTimeLeft("\n§7Despawns: §f", soul.despawnIn);
    }

    public static String getTimeStamp(long time) {
        if (Config.timezoneAbbreviation == null)
            return DATE_FORMAT_WITH_TIMEZONE.format(time + Config.timezoneOffset);
        return DATE_FORMAT.format(time + Config.timezoneOffset) + " " + Config.timezoneAbbreviation;
    }

    public static String getTimeLeft(String prefix, int timeLeft) {
        return timeLeft == -1? "" :
            prefix + DATE_FORMAT.format(System.currentTimeMillis() + (timeLeft * 50L) + Config.timezoneOffset) + ", " + minutesToString(timeLeft/1200);
    }


    public static Text getDeathMessage(Soul soul, boolean op) {
        MutableText text = new LiteralText("\n§6You have §cdied...§6 But your §bSoul§6 lives on..."
                + "\nListen closely, souls will cry out."
                + "\nTouching a soul collects it, granting its items/knowledge to you");
        if (Config.minutesUntilSoulIsVisibleToAllPlayers > 0 && Config.minutesUntilSoulDespawns > 0) {
            text.append("\n§6Your soul will ");
            addHoverText(text, "§6release in §a" + minutesToString(Config.minutesUntilSoulIsVisibleToAllPlayers), "§aWhen souls are released, anyone can collect their contents");
            text.append("§6 and ");
            addHoverText(text, "§6despawn in §c" + minutesToString(Config.minutesUntilSoulDespawns), "§cWhen souls despawn, their contents are lost forever");
            text.append("§6.");
        }
        else if (Config.minutesUntilSoulIsVisibleToAllPlayers > 0)
            text.append("\n§6Your soul will release in §a" + minutesToString(Config.minutesUntilSoulIsVisibleToAllPlayers) + "§6, allowing anyone to collect it.");

        else if (Config.minutesUntilSoulDespawns > 0)
            text.append("\n§6Your soul will despawn in §c" + minutesToString(Config.minutesUntilSoulDespawns) + "§6, losing its contents forever.");

        return text.append(formatEntry(soul, false, op)).append("\n");
    }

    private static String minutesToString(int minutes) {
        String time = "";
        if (minutes / 1440 > 0) {
            time += minutes / 1440 + "d ";
            minutes %= 1440;
        }
        if (minutes / 60 > 0) {
            time += minutes / 60 + "h ";
            minutes %= 60;
        }
        return time + minutes + "m";

    }

}
