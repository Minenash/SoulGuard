package com.minenash.soulguard.config;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.minenash.soulguard.SoulGuard;
import net.minecraft.network.packet.s2c.play.PlaySoundIdS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class SoulSound {

    public Identifier sound;
    public double[] offset = new double[]{0, 0, 0};
    public float volume = 1, pitch = 1;
    public int frequency = 1;

    private int i = 0;
    public void play(BlockPos pos, ServerPlayerEntity host, boolean released) {
        if (i++ < frequency)
            return;

        i = 0;
        for (ServerPlayerEntity player : SoulGuard.server.getPlayerManager().getPlayerList()) {
            if ((Config.allowPlayersToHearCapturedSouls || released || player == host || player.isSpectator() || SoulGuard.CAN_SEE_BOUNDED_SOULS.contains(player)) && pos.isWithinDistance(player.getPos(), Math.min(16, volume*16))) {
                Vec3d vec3d = new Vec3d(pos.getX() + offset[0], pos.getY() + offset[1], pos.getZ() + offset[2]);
                player.networkHandler.sendPacket(new PlaySoundIdS2CPacket(sound, SoundCategory.VOICE, vec3d, volume, pitch, player.getWorld().getRandom().nextLong()));
            }
        }
    }

    public SoulSound() {}
    public SoulSound(Identifier sound, int volume, float pitch, int frequency, int delay, double offsetX, double offsetY, double offsetZ) {
        this.sound = sound;
        this.volume = volume;
        this.pitch = pitch;
        this.frequency = frequency;
        this.i = -delay;
        this.offset = new double[]{offsetX, offsetY, offsetZ};
    }

    public static SoulPropertyResult<SoulSound> create(JsonObject json, int index) {
        SoulSound soulSound = new SoulSound();

        if (!json.has("sound"))
            return SoulPropertyResult.quickFailSound("No sound was given");

        JsonPrimitive jSound = json.getAsJsonPrimitive("sound");
        if (!jSound.isString())
            return SoulPropertyResult.quickFailSound("Sound '" + jSound.toString() + "' is not a string");

        soulSound.sound = new Identifier(jSound.getAsString());

        SoulPropertyResult<SoulSound> result = new SoulPropertyResult<>();

        Integer frequency = JsonHelper.getInt(json, "frequency", result);
        Integer delay = JsonHelper.getInt(json, "delay", result);
        Float volume = JsonHelper.getFloat(json, "volume", result);
        Float pitch = JsonHelper.getFloat(json, "pitch", result);
        Double[] offset = JsonHelper.getVec3Array(json, "offset", result);

        if (pitch != null && (pitch < 0.5 || pitch > 2))
            result.addDebugMessage("Pitch is out of the range of 0.5 to 2, pitch has been " + (pitch < 0.5 ? "upped to 0.5" : "lowered to 2"));

        if (frequency != null) soulSound.frequency = frequency;
        if (delay != null) soulSound.i = -delay;
        if (volume != null) soulSound.volume = volume;
        if (pitch != null) soulSound.pitch = (float) MathHelper.clamp(pitch, 0.5, 2);
        if (offset[0] != null) soulSound.offset[0] = offset[0];
        if (offset[1] != null) soulSound.offset[1] = offset[1];
        if (offset[2] != null) soulSound.offset[2] = offset[2];

        result.value = soulSound;
        return result;
    }
}
