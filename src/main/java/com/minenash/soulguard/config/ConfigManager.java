package com.minenash.soulguard.config;

import com.google.gson.*;
import com.minenash.soulguard.SoulGuard;
import com.minenash.soulguard.souls.SoulManager;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class ConfigManager {

    public static final Path CONFIG_FOLDER = FabricLoader.getInstance().getConfigDir().resolve("soul_guard");
    private static final Path CONFIG_FILE = CONFIG_FOLDER.resolve("config.json");

    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public static void load() {

        if (!Files.exists(CONFIG_FILE)) {
            SoulGuard.LOGGER.warn("Config file not found, creating one");
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
            SoulGuard.LOGGER.error("Couldn't load config, souls unloaded for safety");
            SoulManager.disable();
            return;
        }

        JsonArray jParticles = getArray(json, "particles");
        List<SoulParticle> particles = new ArrayList<>();

        boolean cancelLoadDueToParticles = jParticles == null;
        if (jParticles != null) {
            for (int i = 0; i < jParticles.size(); i++) {
                JsonObject o = jParticles.get(i).getAsJsonObject();
                if (o == null) {
                    SoulGuard.LOGGER.error("Particle Entry at index " + i + " is not a particle entry");
                    cancelLoadDueToParticles = true;
                    continue;
                }
                SoulParticleResult result = SoulParticle.create(o, i);
                if (result.particle == null) {
                    System.out.println();
                    for (String line : result.debugMessages)
                        SoulGuard.LOGGER.warn(line);
                    cancelLoadDueToParticles = true;
                }
                else
                    particles.add(result.particle);
            }
        }

        Integer ticksUntilSoulIsVisibleToAllPlayers = getInteger(json, "ticksUntilSoulIsVisibleToAllPlayers");
        Integer ticksUntilSoulDespawns = getInteger(json, "ticksUntilSoulDespawns");
        Integer percentXpLostOnDeath = getInteger(json, "percentXpLostOnDeath");
        Integer percentXpDroppedOnDeathAfterLoss = getInteger(json, "percentXpDroppedOnDeathAfterLoss");
        Boolean dropRewardXpWhenKilledByPlayer = getBoolean(json, "dropRewardXpWhenKilledByPlayer");
        Boolean allowPlayersToInspectTheirSouls = getBoolean(json, "allowPlayersToInspectTheirSouls");
        Boolean allowPlayersToTeleportToTheirSoul = getBoolean(json, "allowPlayersToTeleportToTheirSoul");

        if (ticksUntilSoulIsVisibleToAllPlayers == null || ticksUntilSoulDespawns == null || percentXpLostOnDeath == null
                || percentXpDroppedOnDeathAfterLoss == null || dropRewardXpWhenKilledByPlayer == null || allowPlayersToInspectTheirSouls == null
                || allowPlayersToTeleportToTheirSoul == null || cancelLoadDueToParticles) {
            SoulGuard.LOGGER.error("Config load aborted, soul ticking has been disabled for safety");
            SoulManager.disable();
            return;
        }

        Config.ticksUntilSoulIsVisibleToAllPlayers = ticksUntilSoulIsVisibleToAllPlayers;
        Config.ticksUntilSoulDespawns = ticksUntilSoulDespawns;
        Config.percentXpLostOnDeath = percentXpLostOnDeath;
        Config.percentXpDroppedOnDeathAfterLoss = percentXpDroppedOnDeathAfterLoss;
        Config.dropRewardXpWhenKilledByPlayer = dropRewardXpWhenKilledByPlayer;
        Config.allowPlayersToInspectTheirSouls = allowPlayersToInspectTheirSouls;
        Config.allowPlayersToTeleportToTheirSoul = allowPlayersToTeleportToTheirSoul;
        Config.particles = particles;

        SoulManager.enable();

        SoulGuard.LOGGER.info("Config loaded");
    }

    private static Integer getInteger(JsonObject json, String member) {
        if (!json.has(member)) {
            SoulGuard.LOGGER.error("Missing config entry: " + member);
            return null;
        }

        JsonPrimitive value = json.getAsJsonPrimitive(member);
        if (value.isNumber())
            return value.getAsInt();

        SoulGuard.LOGGER.error("Config entry, " + member + ", isn't a number");
        return null;

    }

    private static Boolean getBoolean(JsonObject json, String member) {
        if (!json.has(member)) {
            SoulGuard.LOGGER.error("Missing config entry: " + member);
            return null;
        }

        JsonPrimitive value = json.getAsJsonPrimitive(member);
        if (value.isBoolean())
            return value.getAsBoolean();

        SoulGuard.LOGGER.error("Config entry, " + member + ", isn't a boolean");
        return null;

    }

    private static JsonArray getArray(JsonObject json, String member) {
        if (!json.has(member)) {
            SoulGuard.LOGGER.error("Missing config entry: " + member);
            return null;
        }

        JsonArray value = json.getAsJsonArray(member);
        if (value == null)
            SoulGuard.LOGGER.error("Config entry, " + member + ", isn't a list");
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
        particle2.addProperty("type", "dust");
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
            SoulGuard.LOGGER.info("Created default config");
        } catch (IOException e) {
            e.printStackTrace();
            SoulGuard.LOGGER.error("Couldn't create default config");
        }

    }


}
