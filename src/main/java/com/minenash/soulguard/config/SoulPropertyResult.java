package com.minenash.soulguard.config;

import com.minenash.soulguard.SoulGuard;

import java.util.ArrayList;
import java.util.List;

public class SoulPropertyResult<T> {

    T value = null;
    List<String> debugMessages = new ArrayList<>();

    public static SoulPropertyResult<SoulParticle> quickFailParticle(String error) {
        SoulPropertyResult<SoulParticle> result = new SoulPropertyResult<>();
        result.debugMessages.add(error);
        return result;
    }

    public static SoulPropertyResult<SoulSound> quickFailSound(String error) {
        SoulPropertyResult<SoulSound> result = new SoulPropertyResult<>();
        result.debugMessages.add(error);
        return result;
    }

    public void addDebugMessage(String msg) {
        debugMessages.add(msg);
    }

    public void printDebug() {
        if (!debugMessages.isEmpty()) {
            System.out.println();
            for (String line : debugMessages)
                SoulGuard.LOGGER.warn("[Soulguard] " + line);
        }
    }

}
