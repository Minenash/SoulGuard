package com.minenash.soulguard.config;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.minenash.soulguard.SoulGuard;
import com.minenash.soulguard.souls.Soul;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.registry.Registry;
import org.joml.Vector3f;

import java.awt.Color;

public class SoulParticle {

    public ParticleEffect particle;
    public int count = 1, frequency = 1;
    public double speed = 0;
    public double[] delta = new double[]{0, 0, 0};
    public double[] offset = new double[]{0, 0, 0};

    private int i = 0;
    public void render(Soul soul, BlockPos pos, ServerPlayerEntity host) {
        if (++i < frequency)
            return;

        i = 0;
        for (ServerPlayerEntity player : SoulGuard.server.getPlayerManager().getPlayerList())
            if (canViewSoul(soul, host, player) && pos.isWithinDistance(player.getPos(), 512))
                soul.world.spawnParticles(player, particle, true, pos.getX() + offset[0], pos.getY() + offset[1], pos.getZ() + offset[2], count, delta[0], delta[1], delta[2], speed);
    }

    public boolean canViewSoul(Soul soul, ServerPlayerEntity host, ServerPlayerEntity player) {
        return (soul.released || player == host || player.isSpectator()
            || (soul.locked && player.hasPermissionLevel(2))
            || SoulGuard.CAN_SEE_BOUNDED_SOULS.contains(player));
    }

    public SoulParticle() {}
    public SoulParticle(ParticleEffect particle, int count, double speed, int frequency, int delay, double deltaX, double deltaY, double deltaZ, double offsetX, double offsetY, double offsetZ) {
        this.particle = particle;
        this.count = count;
        this.speed = speed;
        this.frequency = frequency;
        this.i = -delay;
        this.delta = new double[]{deltaX, deltaY, deltaZ};
        this.offset = new double[]{offsetX, offsetY, offsetZ};
    }

    public static SoulPropertyResult<SoulParticle> create(JsonObject json, int index) {
        SoulParticle soulParticle = new SoulParticle();

        if (!json.has("type"))
            return SoulPropertyResult.quickFailParticle("No Particle Type was given");

        JsonPrimitive jType = json.getAsJsonPrimitive("type");
        if (!jType.isString())
            return SoulPropertyResult.quickFailParticle("Unknown Particle Type: '" + jType.toString() + "'");

        ParticleType<?> type = Registries.PARTICLE_TYPE.get(new Identifier(jType.getAsString()));
        if (type == null)
            return SoulPropertyResult.quickFailParticle("Unknown Particle Type: '" + jType.getAsString() + "'");

        if (type == ParticleTypes.BLOCK || type == ParticleTypes.ITEM || type == ParticleTypes.FALLING_DUST)
            return SoulPropertyResult.quickFailParticle(jType.getAsString() + " Particles aren't Supported");

        SoulPropertyResult<SoulParticle> result = new SoulPropertyResult<>();
//        result.addDebugMessage("Particle at " + index + " Type: " + type);

        if (type != ParticleTypes.DUST)
            soulParticle.particle = (ParticleEffect) type;
        else {
            Color color = Color.WHITE;
            float scale = 1;

            if (!json.has("color"))
                result.addDebugMessage("Color Property missing from Dust Particle Type, using White");
            else {
                Color c = JsonHelper.getColor(json, result);
                if (c != null)
                    color = c;
            }

            if (json.has("scale")) {
                JsonPrimitive jScale = json.getAsJsonPrimitive("scale");
                if (!jScale.isNumber())
                    result.addDebugMessage("Scale Property not a Number, using 1");
                else
                    scale = jScale.getAsFloat();
            }

            soulParticle.particle = new DustParticleEffect(new Vector3f(color.getRed()/256F, color.getGreen()/256F, color.getBlue()/256F), scale);

        }

        Integer count = JsonHelper.getInt(json, "count", result);
        Integer frequency = JsonHelper.getInt(json, "frequency", result);
        Integer delay = JsonHelper.getInt(json, "delay", result);
        Double speed = JsonHelper.getDouble(json, "speed", result);
        Double[] delta = JsonHelper.getVec3Array(json, "delta", result);
        Double[] offset = JsonHelper.getVec3Array(json, "offset", result);

        if (count == null || count == 0) {
            if (type == ParticleTypes.ENTITY_EFFECT || type == ParticleTypes.AMBIENT_ENTITY_EFFECT) {
                if (json.has("color")) {
                    if (delta[0] != null || delta[1] != null || delta[2] != null)
                        result.addDebugMessage("The color property and delta properties are incompatible for this type, using delta values");
                    else {
                        Color c = JsonHelper.getColor(json, result);
                        if (c != null) {
                            delta[0] = c.getRed()/256.0D;
                            delta[1] = c.getGreen()/256.0D;
                            delta[2] = c.getBlue()/256.0D;
                            if (count == null)
                                count = 0;
                        }
                    }
                }
                speed = JsonHelper.getSpecialDouble(json, "brightness", "speed", speed, result);
            }
            if (type == ParticleTypes.NOTE) {
                delta[0] = JsonHelper.getSpecialDouble(json, "colorModifier", "deltaX", delta[0], result);
                speed = JsonHelper.getSpecialDouble(json, "colorMultiplier", "speed", speed, result);
            }
            else {
                delta[0] = JsonHelper.getSpecialDouble(json, "motionX", "deltaX", delta[0], result);
                delta[1] = JsonHelper.getSpecialDouble(json, "motionY", "deltaY", delta[1], result);
                delta[2] = JsonHelper.getSpecialDouble(json, "motionZ", "deltaZ", delta[2], result);
            }
        }

        if (count != null) soulParticle.count = count;
        if (frequency != null) soulParticle.frequency = frequency;
        if (delay != null) soulParticle.i = -delay;
        if (speed != null) soulParticle.speed = speed;
        if (delta[0] != null) soulParticle.delta[0] = delta[0];
        if (delta[1] != null) soulParticle.delta[1] = delta[1];
        if (delta[2] != null) soulParticle.delta[2] = delta[2];
        if (offset[0] != null) soulParticle.offset[0] = offset[0];
        if (offset[1] != null) soulParticle.offset[1] = offset[1];
        if (offset[2] != null) soulParticle.offset[2] = offset[2];

        result.value = soulParticle;

        return result;
    }

}
