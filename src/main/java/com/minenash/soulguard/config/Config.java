package com.minenash.soulguard.config;

import net.minecraft.particle.DustParticleEffect;
import net.minecraft.particle.ParticleTypes;

import java.util.Arrays;
import java.util.List;

public class Config {

    public static int ticksUntilSoulIsVisibleToAllPlayers = 0;
    public static int ticksUntilSoulDespawns = -1;

    public static int percentXpLostOnDeath = 0;
    public static int percentXpDroppedOnDeathAfterLoss = 0;
    public static boolean dropRewardXpWhenKilledByPlayer = true;

    public static boolean allowPlayersToInspectTheirSouls = true;
    public static boolean allowPlayersToTeleportToTheirSoul = true;

    public static List<SoulParticle> particles = Arrays.asList(
            new SoulParticle(ParticleTypes.ENCHANT, 18, 0.5, 0.1, 1, 0.1, 0, 1, 0),
            new SoulParticle(new DustParticleEffect(0.6F,0.8F,1,1F), 5, 0.5, 0.25, 1, 0.25, 0, 1, 0)
    );

}
