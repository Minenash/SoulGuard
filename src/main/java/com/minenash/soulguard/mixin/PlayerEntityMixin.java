package com.minenash.soulguard.mixin;

import com.minenash.soulguard.Soul;
import com.minenash.soulguard.SoulGuard;
import com.minenash.soulguard.SoulSaveManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public class PlayerEntityMixin {

	@Redirect(method = "dropInventory", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerInventory;dropAll()V"))
	private void dropSoul(PlayerInventory inventory) {
		Entity e = (Entity)(Object)this;
		SoulSaveManager.souls.add(new Soul(e.getPos(),e.getEntityWorld(),inventory.player));
		SoulSaveManager.save();
	}

//	@Inject(method = "getCurrentExperience", at = @At("HEAD"), cancellable = true)
//	private void doNotDropXP(CallbackInfoReturnable<Integer> info) {
//		info.setReturnValue(0);
//	}

}
