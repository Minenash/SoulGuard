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
		wasKilledByPlayer = source.getAttacker() instanceof PlayerEntity;
	}

	@Redirect(method = "dropInventory", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerInventory;dropAll()V"))
	private void dropSoul(PlayerInventory inventory) {
		Entity e = (Entity)(Object)this;
		BlockPos pos = e.getBlockPos();
		BlockState state;
		while (!(state = e.world.getBlockState(pos)).isAir()) {
			pos = pos.add(0,1,0);
			if (e.world.isOutOfHeightLimit(pos) || state.isOf(Blocks.BEDROCK)) {
				pos = e.getBlockPos();
				break;
			}
		}

		Soul soul = new Soul(pos,e.getEntityWorld(),inventory.player, wasKilledByPlayer);
		SoulManager.souls.add(soul);
		SoulManager.idToSoul.put(soul.id, soul);
		SoulManager.save();

		inventory.player.sendMessage(CommandHelper.getDeathMessage(soul, e.hasPermissionLevel(2)), false);
	}

	@Redirect(method = "dropInventory", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;dropInventory()V"))
	private void doNotDropTrinkets(LivingEntity instance) {}

}
