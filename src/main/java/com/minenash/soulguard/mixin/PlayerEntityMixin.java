package com.minenash.soulguard.mixin;

import com.minenash.soulguard.commands.CommandHelper;
import com.minenash.soulguard.souls.Soul;
import com.minenash.soulguard.souls.SoulManager;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = PlayerEntity.class, priority = 900)
public class PlayerEntityMixin {

	boolean wasKilledByPlayer = false;

	@Inject(method = "onDeath", at = @At("HEAD"))
	private void wasKilledByPlayer(DamageSource source, CallbackInfo info) {
		System.out.println("A");
		wasKilledByPlayer = source.getAttacker() instanceof PlayerEntity;
		System.out.println("B");
	}

	@Redirect(method = "dropInventory", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerInventory;dropAll()V"))
	private void dropSoul(PlayerInventory inventory) {
		System.out.println("C");
		Entity e = (Entity)(Object)this;
		BlockPos pos = e.getBlockPos();
		BlockState state;
		while (!(state = e.getWorld().getBlockState(pos)).isAir()) {
			pos = pos.add(0,1,0);
			if (e.getWorld().isOutOfHeightLimit(pos) || state.isOf(Blocks.BEDROCK)) {
				pos = e.getBlockPos();
				break;
			}
		}
		System.out.println("C-1");
		Soul soul = new Soul(pos,e.getEntityWorld(),inventory.player, wasKilledByPlayer);
		System.out.println("C-2");
		SoulManager.souls.add(soul);
		SoulManager.idToSoul.put(soul.id, soul);
		System.out.println("C-3");
		SoulManager.save();
		System.out.println("c-4");

		inventory.player.sendMessage(CommandHelper.getDeathMessage(soul, e.hasPermissionLevel(2)), false);
		System.out.println("D");
	}

	@Redirect(method = "dropInventory", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;dropInventory()V"))
	private void doNotDropTrinkets(LivingEntity instance) {}

}
