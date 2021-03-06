package com.minenash.soulguard.souls;

import com.minenash.soulguard.SoulGuard;
import com.minenash.soulguard.config.ConfigManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.*;
import net.minecraft.util.math.BlockPos;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

public class SoulManager {

    private static final File SAVE_FILE = ConfigManager.CONFIG_FOLDER.resolve("souls.nbt").toFile();

    private static boolean processSouls = false;

    public static Map<String, Soul> souls = new HashMap<>();

    public static void load() {
        if (!SAVE_FILE.exists()) {
            SoulGuard.LOGGER.info("[Soulguard] Soul Save File doesn't exist, creating one");
            save();
            return;
        }

        CompoundTag rootTag = null;

        try { rootTag = NbtIo.read(SAVE_FILE); }
        catch (IOException e) { e.printStackTrace(); }

        if (rootTag == null) {
            SoulGuard.LOGGER.error("[Soulguard] Couldn't load Souls");
            return;
        }

        ListTag soulsTag = rootTag.getList("souls", 10);
        Map<String, Soul> soulsFromTag = new HashMap<>();

        for (Tag soulTag : soulsTag) {
            Soul soul = Soul.fromTag((CompoundTag) soulTag);
            soulsFromTag.put(soul.id, soul);
        }

        souls = soulsFromTag;
        SoulGuard.LOGGER.info("[Soulguard] Loaded Souls");
    }

    public static void save() {
        ListTag soulsTag = new ListTag();
        for (Soul soul : souls.values())
            soulsTag.add( soul.toTag() );

        CompoundTag rootTag = new CompoundTag();
        rootTag.put("souls", soulsTag);

        try {
            if (!SAVE_FILE.exists()) {
                if (!Files.exists(ConfigManager.CONFIG_FOLDER))
                    Files.createDirectory(ConfigManager.CONFIG_FOLDER);
                SAVE_FILE.createNewFile();
            }
            NbtIo.write(rootTag, SAVE_FILE);
            SoulGuard.LOGGER.info("[Soulguard] Saved Souls");
        } catch (IOException e) {
            SoulGuard.LOGGER.error("[Soulguard] Couldn't save Souls");
            e.printStackTrace();
        }
    }

    public static void disable() {
        if (processSouls)
            save();
        processSouls = false;
    }

    public static void enable() {
        processSouls = true;
    }

    public static boolean processSouls() {
        return processSouls;
    }

}
