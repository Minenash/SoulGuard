package com.minenash.soulguard.config;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;

import java.awt.Color;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SoulParticle {

    public ParticleEffect particle;
    public int count = 1;
    public double speed = 0;
    public double deltaX = 0, deltaY = 0, deltaZ = 0;
    public double offsetX = 0, offsetY = 0, offsetZ = 0;

    public void render(ServerWorld world, BlockPos pos) {
        world.spawnParticles(particle, pos.getX() + offsetX, pos.getY() + offsetY, pos.getZ() + offsetZ, count, deltaX, deltaY, deltaZ, speed);
    }

    public SoulParticle() {}
    public SoulParticle(ParticleEffect particle, int count, double speed, double deltaX, double deltaY, double deltaZ, double offsetX, double offsetY, double offsetZ) {
        this.particle = particle;
        this.count = count;
        this.speed = speed;
        this.deltaX = deltaX;
        this.deltaY = deltaY;
        this.deltaZ = deltaZ;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.offsetZ = offsetZ;
    }

    public static final Pattern COLOR_PATTERN = Pattern.compile("(0x|#)?([A-Fa-f\\d]{6}|[A-Fa-f\\d]{3})");
    public static SoulParticleResult create(JsonObject json, int index) {
        SoulParticle soulParticle = new SoulParticle();

        if (!json.has("type"))
            return SoulParticleResult.quickFail("No Particle Type was given");

        JsonPrimitive jType = json.getAsJsonPrimitive("type");
        if (!jType.isString())
            return SoulParticleResult.quickFail("Unknown Particle Type: '" + jType.toString() + "'");

        ParticleType<?> type = Registry.PARTICLE_TYPE.get(new Identifier(jType.toString()));
        if (type == null)
            return SoulParticleResult.quickFail("Unknown Particle Type: '" + jType.getAsString() + "'");

        if (type == ParticleTypes.BLOCK)
            return SoulParticleResult.quickFail("Block Particles aren't Supported");
        if (type == ParticleTypes.ITEM)
            return SoulParticleResult.quickFail("Item Particles aren't Supported");
        else if (type == ParticleTypes.FALLING_DUST)
            return SoulParticleResult.quickFail("Falling Dust Particles aren't Supported");

        SoulParticleResult result = new SoulParticleResult();
        result.addDebugMessage("Particle at " + index + " Type: " + type);

        if (type != ParticleTypes.DUST)
            soulParticle.particle = (ParticleEffect) type;
        else {
            Color color = Color.WHITE;
            float scale = 1;

            if (!json.has("color"))
                result.addDebugMessage("Color Property missing from Dust Particle Type, using White");
            else {
                Color c = getColor(json, result);
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

            soulParticle.particle = new DustParticleEffect(color.getRed()/256F, color.getGreen()/256F, color.getBlue()/256F, scale);

        }

        Double count = getNumber(json, "count", result);
        Double speed = getNumber(json, "speed", result);
        Double deltaX = getNumber(json, "deltaX", result);
        Double deltaY = getNumber(json, "deltaY", result);
        Double deltaZ = getNumber(json, "deltaZ", result);
        Double offsetX = getNumber(json, "offsetX", result);
        Double offsetY = getNumber(json, "offsetY", result);
        Double offsetZ = getNumber(json, "offsetZ", result);

        if (count == null || count == 0) {
            if (type == ParticleTypes.ENTITY_EFFECT || type == ParticleTypes.AMBIENT_ENTITY_EFFECT) {
                if (json.has("color")) {
                    if (deltaX != null || deltaY != null || deltaZ != null)
                        result.addDebugMessage("The color property and delta properties are incompatible for this type, using delta values");
                    else {
                        Color c = getColor(json, result);
                        if (c != null) {
                            deltaX = c.getRed()/256.0D;
                            deltaY = c.getGreen()/256.0D;
                            deltaZ = c.getBlue()/256.0D;
                            if (count == null)
                                count = 0.0;
                        }
                    }
                }
                speed = getSpecialNumber(json, "brightness", "speed", speed, result);
            }
            if (type == ParticleTypes.NOTE) {
                deltaX = getSpecialNumber(json, "colorModifier", "deltaX", deltaX, result);
                speed = getSpecialNumber(json, "colorMultiplier", "speed", speed, result);
            }
            else {
                deltaX = getSpecialNumber(json, "motionX", "deltaX", deltaX, result);
                deltaY = getSpecialNumber(json, "motionY", "deltaY", deltaY, result);
                deltaZ = getSpecialNumber(json, "motionZ", "deltaZ", deltaZ, result);
            }
        }

        if (count != null) soulParticle.count = count.intValue();
        if (speed != null) soulParticle.speed = speed;
        if (deltaX != null) soulParticle.deltaX = deltaX;
        if (deltaY != null) soulParticle.deltaY = deltaY;
        if (deltaZ != null) soulParticle.deltaZ = deltaZ;
        if (offsetX != null) soulParticle.offsetX = offsetX;
        if (offsetY != null) soulParticle.offsetY = offsetY;
        if (offsetZ != null) soulParticle.offsetZ = offsetZ;

        result.particle = soulParticle;

        return result;
    }

    private static Double getNumber(JsonObject json, String member, SoulParticleResult result) {
        if (!json.has(member))
            return null;
        JsonPrimitive jCount = json.getAsJsonPrimitive(member);
        if (!jCount.isNumber()) {
            result.addDebugMessage(member + " is not a number, using 0");
            return 0.0;
        }
        return jCount.getAsDouble();
    }

    private static Double getSpecialNumber(JsonObject json, String member, String oldMember, Double oldValue, SoulParticleResult result) {
        if (!json.has(member))
            return oldValue;
        if (oldValue == null)
            return getNumber(json, "brightness", result);

        result.addDebugMessage("The " + member + " property and the " + oldMember + " property are incompatible for this type, using " + oldMember + "'s value");
        return oldValue;
    }

    private static Color getColor(JsonObject json, SoulParticleResult result) {
        JsonPrimitive jColor = json.getAsJsonPrimitive("color");
        if (!jColor.isString())
            result.addDebugMessage("Color Property not a Hexadecimal String, using White");
        else {
            Matcher matcher = COLOR_PATTERN.matcher(jColor.getAsString());
            if (!matcher.matches())
                result.addDebugMessage("Color Property not a Hexadecimal String, using White");
            else {
                String hexColor = matcher.group(2);
                if (hexColor.length() == 3)
                    hexColor = "" + hexColor.charAt(0) + hexColor.charAt(0) + hexColor.charAt(1) + hexColor.charAt(1) + hexColor.charAt(2) + hexColor.charAt(2);
                return new Color(Integer.parseInt(hexColor, 16));
            }
        }
        return null;
    }

}
