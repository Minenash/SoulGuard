package com.minenash.soulguard;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.item.ItemGroup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SoulSaveManager {

    private static final File SAVE_FILE = FabricLoader.getInstance().getConfigDir().resolve("soul_guard/souls.nbt").toFile();
    private static final File CONFIG_FOLDER = FabricLoader.getInstance().getConfigDir().resolve("soul_guard").toFile();

    public static List<Soul> souls = new ArrayList<>();

    public static void load() {
        CompoundTag rootTag = null;

        try { rootTag = NbtIo.read(SAVE_FILE); }
        catch (IOException e) { e.printStackTrace(); }

        if (rootTag == null) {
            System.out.println("Couldn't load Souls");
            return;
        }

        ListTag soulsTag = rootTag.getList("souls", 10);
        List<Soul> soulsFromTag = new ArrayList<>();

        for (Tag soul : soulsTag)
            soulsFromTag.add( Soul.fromTag((CompoundTag) soul) );

        souls = soulsFromTag;
        System.out.println("Load Souls");
    }

    public static void save() {
        ListTag soulsTag = new ListTag();
        for (Soul soul : souls)
            soulsTag.add( soul.toTag() );

        CompoundTag rootTag = new CompoundTag();
        rootTag.put("souls", soulsTag);

        try {
            if (!SAVE_FILE.exists()) {
                CONFIG_FOLDER.mkdir();
                SAVE_FILE.createNewFile();
            }
            NbtIo.write(rootTag, SAVE_FILE);
            System.out.println("Saved Souls");
        } catch (IOException e) {
            System.out.println("Couldn't save Souls");
            e.printStackTrace();
        }
    }

}
