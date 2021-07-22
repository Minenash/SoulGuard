package com.minenash.soulguard.config;

import com.google.gson.*;
import com.minenash.soulguard.SoulGuard;
import com.minenash.soulguard.souls.SoulManager;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConfigManager {

    public static final Path CONFIG_FOLDER = FabricLoader.getInstance().getConfigDir().resolve("soul_guard");
    private static final Path CONFIG_FILE = CONFIG_FOLDER.resolve("config.json");

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Pattern TIME_ZONE = Pattern.compile("(-)?(\\d){2}:(\\d){2}");

    public static void load(boolean tryAgain) {

        if (!Files.exists(CONFIG_FILE)) {
            SoulGuard.LOGGER.warn("[Soulguard] Config file not found, creating one");
            saveDefaults();
            if (tryAgain)
                load(false);
            return;
        }

        JsonObject json = null;

        try {
            Reader reader = Files.newBufferedReader(CONFIG_FILE);
            json = GSON.fromJson(reader, JsonObject.class);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (json == null) {
            SoulGuard.LOGGER.error("[Soulguard] Couldn't load config, souls unloaded for safety");
            SoulManager.disable();
            return;
        }

        List<SoulParticle> boundedParticles = new ArrayList<>();
        List<SoulParticle> releasedParticles = new ArrayList<>();
        List<SoulParticle> lockedParticles = new ArrayList<>();
        List<SoulSound> boundedSounds = new ArrayList<>();
        List<SoulSound> releasedSounds = new ArrayList<>();
        List<SoulSound> lockedSounds = new ArrayList<>();

        boolean cancelLoadDueToLists = setParticlesList(json, "captured_particles", boundedParticles, false);
        cancelLoadDueToLists |= setParticlesList(json, "released_particles", releasedParticles, true);
        cancelLoadDueToLists |= setParticlesList(json, "locked_particles", lockedParticles, true);
        cancelLoadDueToLists |= setSoundsList(json, "captured_sounds", boundedSounds, false);
        cancelLoadDueToLists |= setSoundsList(json, "released_sounds", releasedSounds, true);
        cancelLoadDueToLists |= setSoundsList(json, "locked_sounds", lockedSounds, true);

        Integer minutesUntilSoulIsVisibleToAllPlayers = getInteger(json, "minutes_until_soul_is_visible_to_all_players");
        Integer minutesUntilSoulDespawns = getInteger(json, "minutes_until_soul_despawns");
        Integer percentXpLostOnDeath = getInteger(json, "percent_xp_lost_on_death");
        Integer percentXpDroppedOnDeathAfterLoss = getInteger(json, "percent_xp_dropped_on_death_after_loss");
        Boolean dropRewardXpWhenKilledByPlayer = getBoolean(json, "drop_reward_xp_when_killed_by_player");
        Boolean allowPlayersToInspectTheirSouls = getBoolean(json, "allow_players_to_inspect_their_souls");
        Boolean allowPlayersToTeleportToTheirSoul = getBoolean(json, "allow_players_to_teleport_to_their_soul");
        Boolean allowPlayersToHearCapturedSouls = getBoolean(json, "allow_players_to_hear_captured_souls");
        String timezoneOffset = getString(json, "timezone_offset");
        String timezoneAbbreviation = getString(json, "timezone_abbreviation");
        Integer exclusiveSoundRadius = getInteger(json, "exclusive_sound_radius");

        if (minutesUntilSoulIsVisibleToAllPlayers == null || minutesUntilSoulDespawns == null || percentXpLostOnDeath == null
                || percentXpDroppedOnDeathAfterLoss == null || dropRewardXpWhenKilledByPlayer == null || allowPlayersToInspectTheirSouls == null
                || allowPlayersToTeleportToTheirSoul == null || exclusiveSoundRadius == null || allowPlayersToHearCapturedSouls == null
                || cancelLoadDueToLists) {

            SoulGuard.LOGGER.error("[Soulguard] Config load aborted, soul ticking has been disabled for safety");
            for (ServerPlayerEntity player : SoulGuard.server.getPlayerManager().getPlayerList())
                if (player.hasPermissionLevel(2))
                    player.sendMessage(new LiteralText("§c[Soulguard] Config load aborted, soul ticking has been disabled for safety"), false);

            SoulManager.disable();
            return;
        }

        if (timezoneOffset == null || timezoneAbbreviation == null)
            SoulGuard.LOGGER.warn("[Soulguard] Timezone information is missing. System's default will be used");
        else if (!timezoneOffset.equals("system")){

            Matcher matcher = TIME_ZONE.matcher(timezoneOffset);
            if (matcher.matches()) {
                int offset = (matcher.group(1).isEmpty() ? 1 : -1) * (Integer.parseInt(matcher.group(2))*60 + Integer.parseInt(matcher.group(3)));
                Config.timezoneOffset =  60000L * (new Date().getTimezoneOffset() - offset);
                if (!timezoneAbbreviation.equals("system"))
                    Config.timezoneAbbreviation = timezoneAbbreviation;
            }
            else
                SoulGuard.LOGGER.warn("[Soulguard] timezone_offset could not be read. Is it in the correct format? System's default will be used");
        }

        Config.minutesUntilSoulIsVisibleToAllPlayers = minutesUntilSoulIsVisibleToAllPlayers;
        Config.minutesUntilSoulDespawns = minutesUntilSoulDespawns;
        Config.percentXpLostOnDeath = percentXpLostOnDeath;
        Config.percentXpDroppedOnDeathAfterLoss = percentXpDroppedOnDeathAfterLoss;
        Config.dropRewardXpWhenKilledByPlayer = dropRewardXpWhenKilledByPlayer;
        Config.allowPlayersToInspectTheirSouls = allowPlayersToInspectTheirSouls;
        Config.allowPlayersToTeleportToTheirSoul = allowPlayersToTeleportToTheirSoul;
        Config.boundedParticles = boundedParticles;
        Config.releasedParticles = releasedParticles.isEmpty() ? boundedParticles : releasedParticles;
        Config.lockedParticles = lockedParticles.isEmpty() ? boundedParticles : lockedParticles;
        Config.boundedSounds = boundedSounds;
        Config.releasedSounds = releasedSounds.isEmpty() ? boundedSounds : releasedSounds;
        Config.lockedSounds = lockedSounds.isEmpty() ? boundedSounds : lockedSounds;
        Config.exclusiveSoundRadius = exclusiveSoundRadius;
        Config.allowPlayersToHearCapturedSouls = allowPlayersToHearCapturedSouls;

        SoulManager.enable();

        SoulGuard.LOGGER.info("[Soulguard] Config loaded");
    }

    private static Integer getInteger(JsonObject json, String member) {
        if (!json.has(member)) {
            SoulGuard.LOGGER.error("[Soulguard] Missing config entry: " + member);
            return null;
        }

        JsonPrimitive value = json.getAsJsonPrimitive(member);
        if (value.isNumber())
            return value.getAsInt();

        SoulGuard.LOGGER.error("[Soulguard] Config entry, " + member + ", isn't a number");
        return null;
    }

    private static Boolean getBoolean(JsonObject json, String member) {
        if (!json.has(member)) {
            SoulGuard.LOGGER.error("[Soulguard] Missing config entry: " + member);
            return null;
        }

        JsonPrimitive value = json.getAsJsonPrimitive(member);
        if (value.isBoolean())
            return value.getAsBoolean();

        SoulGuard.LOGGER.error("[Soulguard] Config entry, " + member + ", isn't a boolean");
        return null;
    }

    private static String getString(JsonObject json, String member) {
        if (!json.has(member)) {
            SoulGuard.LOGGER.error("[Soulguard] Missing config entry: " + member);
            return null;
        }
        return json.getAsJsonPrimitive(member).getAsString();
    }

    private static JsonArray getArray(JsonObject json, String member) {
        if (!json.has(member)) {
            SoulGuard.LOGGER.error("[Soulguard] Missing config entry: " + member);
            return null;
        }

        if (json.get(member).isJsonArray())
            return json.getAsJsonArray(member);

        SoulGuard.LOGGER.error("[Soulguard] Config entry, " + member + ", isn't a list");
        return null;
    }

    private static boolean setParticlesList(JsonObject json, String member, List<SoulParticle> list, boolean ignoreMissing) {
        JsonArray jParticles = getArray(json, member);

        if (jParticles == null)
           return !ignoreMissing;

        boolean cancelLoadDueToParticles = false;
        for (int i = 0; i < jParticles.size(); i++) {
            JsonObject o = jParticles.get(i).getAsJsonObject();
            if (o == null) {
                SoulGuard.LOGGER.error("[Soulguard] Particle Entry at index " + i + " is not a particle entry");
                cancelLoadDueToParticles = true;
                continue;
            }
            SoulPropertyResult<SoulParticle> result = SoulParticle.create(o, i);
            result.printDebug();

            if (result.value == null)
                cancelLoadDueToParticles = true;
            else
                list.add(result.value);
        }
        return cancelLoadDueToParticles;
    }

    private static boolean setSoundsList(JsonObject json, String member, List<SoulSound> list, boolean ignoreMissing) {
        JsonArray jParticles = getArray(json, member);

        if (jParticles == null)
            return !ignoreMissing;

        boolean cancelLoadDueToSounds = false;
        for (int i = 0; i < jParticles.size(); i++) {
            JsonObject o = jParticles.get(i).getAsJsonObject();
            if (o == null) {
                SoulGuard.LOGGER.error("[Soulguard] Sound Entry at index " + i + " is not a particle entry");
                cancelLoadDueToSounds = true;
                continue;
            }
            SoulPropertyResult<SoulSound> result = SoulSound.create(o, i);
            result.printDebug();

            if (result.value == null)
                cancelLoadDueToSounds = true;
            else
                list.add(result.value);
        }
        return cancelLoadDueToSounds;
    }

    public static void saveDefaults() {
        try {
            if (!Files.exists(ConfigManager.CONFIG_FOLDER))
                Files.createDirectory(ConfigManager.CONFIG_FOLDER);
            Files.write(CONFIG_FILE, ConfigManager.class.getResourceAsStream("/assets/soulguard/default_config.json").readAllBytes());
            SoulGuard.LOGGER.info("[Soulguard] Created default config");
        } catch (IOException e) {
            e.printStackTrace();
            SoulGuard.LOGGER.error("[Soulguard] Couldn't create default config");
        }
    }


}
