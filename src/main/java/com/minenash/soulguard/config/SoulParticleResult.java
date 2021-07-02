package com.minenash.soulguard.config;

import java.util.ArrayList;
import java.util.List;

public class SoulParticleResult {

    SoulParticle particle = null;
    List<String> debugMessages = new ArrayList<>();

    public static SoulParticleResult quickFail(String error) {
        SoulParticleResult result = new SoulParticleResult();
        result.debugMessages.add(error);
        return result;
    }

    public void addDebugMessage(String msg) {
        debugMessages.add(msg);
    }

}
