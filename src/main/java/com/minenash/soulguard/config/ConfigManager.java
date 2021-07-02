package com.minenash.soulguard.config;

import com.google.gson.*;
import com.minenash.soulguard.SoulSaveManager;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;

public class ConfigManager {

    public static final Path CONFIG_FOLDER = FabricLoader.getInstance().getConfigDir().resolve("soul_guard");
    private static final Path CONFIG_FILE = CONFIG_FOLDER.resolve("config.json");

    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public static void load() {

        if (!Files.exists(CONFIG_FILE)) {
            System.out.println("Config file not found, creating one");
            saveDefaults();
            return;
        }

        JsonObject json = null;

        try {
            Reader reader = Files.newBufferedReader(CONFIG_FILE);
            json = gson.fromJson(reader, JsonObject.class);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (json == null) {
            System.out.println("Couldn't load config, using, but not saving, defaults");
            return;
        }

        Integer ticksUntilSoulIsVisibleToAllPlayers = getInteger(json, "ticksUntilSoulIsVisibleToAllPlayers");
        Integer ticksUntilSoulDespawns = getInteger(json, "ticksUntilSoulDespawns");
        Integer percentXpLostOnDeath = getInteger(json, "percentXpLostOnDeath");
        Integer percentXpDroppedOnDeathAfterLoss = getInteger(json, "percentXpDroppedOnDeathAfterLoss");
        Boolean dropRewardXpWhenKilledByPlayer = getBoolean(json, "dropRewardXpWhenKilledByPlayer");

        JsonArray particles = getArray(json, "particles");

        boolean cancelLoadDueToParticles = particles == null;
        if (particles != null) {
            for (int i = 0; i < particles.size(); i++) {
                JsonObject o = particles.get(i).getAsJsonObject();
                if (o == null) {
                    System.out.println("Particle Entry at index " + i + " is not a particle entry");
                    cancelLoadDueToParticles = true;
                    continue;
                }
                SoulParticleResult result = SoulParticle.create(o, i);
                if (result.particle == null) {
                    System.out.println();
                    for (String line : result.debugMessages)
                        System.out.println(line);
                    cancelLoadDueToParticles = true;
                }
            }
        }

        if (ticksUntilSoulIsVisibleToAllPlayers == null || ticksUntilSoulDespawns == null || percentXpLostOnDeath == null || percentXpDroppedOnDeathAfterLoss == null || dropRewardXpWhenKilledByPlayer == null ||cancelLoadDueToParticles) {
            System.out.println("Config load aborted, souls unloaded for safety");
            SoulSaveManager.unload();
        }

    }

    private static Integer getInteger(JsonObject json, String member) {
        if (!json.has(member)) {
            System.out.println("Missing config entry: " + member);
            return null;
        }

        JsonPrimitive value = json.getAsJsonPrimitive(member);
        if (value.isNumber())
            return json.getAsInt();

        System.out.println("Config entry, " + member + ", isn't a number");
        return null;

    }

    private static Boolean getBoolean(JsonObject json, String member) {
        if (!json.has(member)) {
            System.out.println("Missing config entry: " + member);
            return null;
        }

        JsonPrimitive value = json.getAsJsonPrimitive(member);
        if (value.isBoolean())
            return json.getAsBoolean();

        System.out.println("Config entry, " + member + ", isn't a boolean");
        return null;

    }

    private static JsonArray getArray(JsonObject json, String member) {
        if (!json.has(member)) {
            System.out.println("Missing config entry: " + member);
            return null;
        }

        JsonArray value = json.getAsJsonArray(member);
        if (value == null)
            System.out.println("Config entry, " + member + ", isn't a list");
        return value;
    }

    public static void saveDefaults() {
        JsonObject json = new JsonObject();
        json.addProperty("ticksUntilSoulIsVisibleToAllPlayers", Config.ticksUntilSoulIsVisibleToAllPlayers);
        json.addProperty("ticksUntilSoulDespawns", Config.ticksUntilSoulDespawns);
        json.addProperty("percentXpLostOnDeath", Config.percentXpLostOnDeath);
        json.addProperty("percentXpDroppedOnDeathAfterLoss", Config.percentXpDroppedOnDeathAfterLoss);
        json.addProperty("dropRewardXpWhenKilledByPlayer", Config.dropRewardXpWhenKilledByPlayer);

        JsonObject particle1 = new JsonObject();
        particle1.addProperty("type", "enchant");
        particle1.addProperty("count", 18);
        particle1.addProperty("speed", 0.5);
        particle1.addProperty("deltaX", 0.1);
        particle1.addProperty("deltaY", 1);
        particle1.addProperty("deltaZ", 0.1);
        particle1.addProperty("offsetY", 1);

        JsonObject particle2 = new JsonObject();
        particle1.addProperty("type", "dust");
        particle2.addProperty("color", "9ACDFF");
        particle2.addProperty("count", 5);
        particle2.addProperty("speed", 0.5);
        particle2.addProperty("deltaX", 0.25);
        particle2.addProperty("deltaY", 1);
        particle2.addProperty("deltaZ", 0.25);
        particle2.addProperty("offsetY", 1);

        JsonArray particles = new JsonArray();
        particles.add(particle1);
        particles.add(particle2);
        json.add("particles", particles);

        try {
            Files.write(CONFIG_FILE, gson.toJson(json).getBytes());
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Couldn't create default config");
        }

    }


}
