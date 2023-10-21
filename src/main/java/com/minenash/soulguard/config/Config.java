package com.minenash.soulguard.config;

import net.minecraft.particle.DustParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.Identifier;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Config {

    public static int minutesUntilSoulIsVisibleToAllPlayers = -1;
    public static int minutesUntilSoulDespawns = -1;

    public static int percentXpLostOnDeath = 0;
    public static int percentXpDroppedOnDeathAfterLoss = 0;
    public static boolean dropRewardXpWhenKilledByPlayer = true;

    public static boolean allowPlayersToInspectTheirSouls = true;
    public static boolean allowPlayersToTeleportToTheirSoul = true;
    public static boolean allowPlayersToHearCapturedSouls = false;

    public static long timezoneOffset = 0;
    public static String timezoneAbbreviation = null;

    public static int exclusiveSoundRadius = 0;

    public static List<SoulParticle> boundedParticles = Arrays.asList(
            new SoulParticle(ParticleTypes.ENCHANT, 18, 0.5, 1, 1, 0.1, 1, 0.1, 0, 1, 0),
            new SoulParticle(new DustParticleEffect(new Vector3f(0.6F,0.8F,1F),1F), 5, 0.5, 1, 1, 0.25, 1, 0.25, 0, 1, 0)
    );
    public static List<SoulParticle> releasedParticles = new ArrayList<>();
    public static List<SoulParticle> lockedParticles = new ArrayList<>();

    public static List<SoulSound> boundedSounds = List.of(
            new SoulSound(new Identifier("entity.ghast.scream"), 1, 1, 10, 1, 0, 0, 0)
    );
    public static List<SoulSound> releasedSounds = new ArrayList<>();
    public static List<SoulSound> lockedSounds = new ArrayList<>();

}
