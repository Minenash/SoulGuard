package com.minenash.soulguard.souls;

import com.minenash.soulguard.SoulGuard;
import com.minenash.soulguard.config.ConfigManager;
import net.minecraft.nbt.*;
import net.minecraft.server.MinecraftServer;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class SoulManager {

    private static final Path SAVE_FILE = ConfigManager.CONFIG_FOLDER.resolve("souls.nbt");

    private static boolean processSouls = false;

    public static Map<String, Soul> idToSoul = new HashMap<>();
    public static List<Soul> souls = new ArrayList<>();

    public static List<Soul> soulsProcessedThisTick = new ArrayList<>();

    private static int saveInterval = 0;
    public static void processSouls(MinecraftServer server) {
        if (!processSouls)
            return;

        boolean save = ++saveInterval % 1200 == 0;

        Iterator<Soul> iterator = SoulManager.souls.iterator();
        while (iterator.hasNext()) {
            Soul soul = iterator.next();
            if (soul.process(server)) {
                SoulManager.idToSoul.remove(soul.id);
                iterator.remove();
                save = true;
            }
        }

        if (save) {
            SoulManager.save();
            saveInterval = 0;
        }
        soulsProcessedThisTick.clear();
    }

    public static void load() {
        if (!Files.exists(SAVE_FILE)) {
            SoulGuard.LOGGER.info("[Soulguard] Soul Save File doesn't exist, creating one");
            save();
            return;
        }

        NbtCompound rootTag = null;

        try {
            rootTag = NbtIo.read(SAVE_FILE);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (rootTag == null) {
            SoulGuard.LOGGER.error("[Soulguard] Couldn't load Souls");
            return;
        }

        NbtList soulsTag = rootTag.getList("souls", 10);
        Map<String, Soul> soulsMap = new HashMap<>();
        List<Soul> soulsList = new ArrayList<>();

        for (NbtElement soulTag : soulsTag) {
            Soul soul = Soul.fromTag((NbtCompound) soulTag);
            soulsMap.put(soul.id, soul);
            soulsList.add(soul);
        }

        souls = soulsList;
        idToSoul = soulsMap;
        SoulGuard.LOGGER.info("[Soulguard] Loaded Souls");
    }

    public static void save() {
        NbtList soulsTag = new NbtList();
        for (Soul soul : souls)
            soulsTag.add( soul.toTag() );

        NbtCompound rootTag = new NbtCompound();
        rootTag.putInt("schema", 1);
        rootTag.put("souls", soulsTag);

        try {
            if (!Files.exists(SAVE_FILE)) {
                if (!Files.exists(ConfigManager.CONFIG_FOLDER))
                    Files.createDirectory(ConfigManager.CONFIG_FOLDER);
                Files.createFile(SAVE_FILE);
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

    public static boolean isDisabled() {
        return !processSouls;
    }

}
