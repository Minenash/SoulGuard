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
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SoulManager {

    private static final File SAVE_FILE = ConfigManager.CONFIG_FOLDER.resolve("souls.nbt").toFile();

    private static boolean processSouls = false;

    public static List<Soul> souls = new ArrayList<>();

    public static void load() {
        if (!SAVE_FILE.exists()) {
            SoulGuard.LOGGER.info("Soul Save File doesn't exist, creating one");
            save();
            return;
        }

        CompoundTag rootTag = null;

        try { rootTag = NbtIo.read(SAVE_FILE); }
        catch (IOException e) { e.printStackTrace(); }

        if (rootTag == null) {
            SoulGuard.LOGGER.error("Couldn't load Souls");
            return;
        }

        ListTag soulsTag = rootTag.getList("souls", 10);
        List<Soul> soulsFromTag = new ArrayList<>();

        for (Tag soul : soulsTag)
            soulsFromTag.add( Soul.fromTag((CompoundTag) soul) );

        souls = soulsFromTag;
        SoulGuard.LOGGER.info("Load Souls");
    }

    public static void save() {
        ListTag soulsTag = new ListTag();
        for (Soul soul : souls)
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
            SoulGuard.LOGGER.info("Saved Souls");
        } catch (IOException e) {
            SoulGuard.LOGGER.error("Couldn't save Souls");
            e.printStackTrace();
        }
    }

    public static void disable() {
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
