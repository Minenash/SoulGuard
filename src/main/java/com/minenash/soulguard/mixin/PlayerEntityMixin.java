package com.minenash.soulguard.mixin;

import com.minenash.soulguard.commands.CommandHelper;
import com.minenash.soulguard.souls.Soul;
import com.minenash.soulguard.souls.SoulManager;
import com.minenash.soulguard.config.Config;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.LiteralText;
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
		Soul soul = new Soul(e.getPos(),e.getEntityWorld(),inventory.player);
		SoulManager.souls.put(soul.id, soul);
		SoulManager.save();

		String autoRelease = Config.minutesUntilSoulIsVisibleToAllPlayers == -1 ? "" : "\n It will auto-release in " + Config.minutesUntilSoulIsVisibleToAllPlayers + "min.";
		inventory.player.sendMessage(new LiteralText(CommandHelper.getMessage(soul)).styled(style -> style
				.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new LiteralText("§eClick to release soul now")))
				.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/soulguard release " + soul.id))
		), false);
	}

	@Inject(method = "getCurrentExperience", at = @At("HEAD"), cancellable = true)
	private void doNotDropXP(CallbackInfoReturnable<Integer> info) {
		if (!Config.dropRewardXpWhenKilledByPlayer)
			info.setReturnValue(0);
	}

}
