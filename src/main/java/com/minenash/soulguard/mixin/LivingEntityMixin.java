package com.minenash.soulguard.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = LivingEntity.class)
public class LivingEntityMixin {

    @Inject(method = "dropXp", at = @At("HEAD"), cancellable = true)
    public void doNotDropPlayerXP(CallbackInfo ci) {
        if ((Object)this instanceof PlayerEntity)
            ci.cancel();
    }

}
