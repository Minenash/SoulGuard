package com.minenash.soulguard.mixin;

import com.minenash.soulguard.commands.CommandHelper;
import com.minenash.soulguard.souls.Soul;
import com.minenash.soulguard.souls.SoulManager;
import com.minenash.soulguard.config.Config;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.LiteralText;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = PlayerEntity.class, priority = 900)
public class PlayerEntityMixin {

	boolean wasKilledByPlayer = false;

	@Inject(method = "onDeath", at = @At("HEAD"))
	private void wasKilledByPlayer(DamageSource source, CallbackInfo info) {
		wasKilledByPlayer = source.getAttacker() instanceof PlayerEntity;
	}

	@Redirect(method = "dropInventory", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerInventory;dropAll()V"))
	private void dropSoul(PlayerInventory inventory) {
		Entity e = (Entity)(Object)this;
		Soul soul = new Soul(e.getPos(),e.getEntityWorld(),inventory.player, wasKilledByPlayer);
		SoulManager.souls.add(soul);
		SoulManager.idToSoul.put(soul.id, soul);
		SoulManager.save();

		inventory.player.sendMessage(CommandHelper.getDeathMessage(soul, e.hasPermissionLevel(2)), false);
	}

	@Inject(method = "dropInventory", at = @At("TAIL"), cancellable = true)
	private void doNotDropTrinkets(CallbackInfo info) { info.cancel();}




	@Inject(method = "getCurrentExperience", at = @At("HEAD"), cancellable = true)
	private void doNotDropXP(CallbackInfoReturnable<Integer> info) {
		System.out.println("Killed By player: " + wasKilledByPlayer);

		if (!(Config.dropRewardXpWhenKilledByPlayer && wasKilledByPlayer))
			info.setReturnValue(0);
	}

}
