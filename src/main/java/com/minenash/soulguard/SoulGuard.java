package com.minenash.soulguard;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.collection.DefaultedList;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SoulGuard implements ModInitializer {

	public static List<Soul> souls = new ArrayList<>();

	int i = 60;
	@Override
	public void onInitialize() {

		ServerTickEvents.END_SERVER_TICK.register(server -> {

			//System.out.println(souls);

			for (Soul soul : souls)
				renderSoul(soul);
			if (souls.isEmpty())
				return;
			for (PlayerEntity player : server.getPlayerManager().getPlayerList()) {
				if (!player.isAlive()) continue;
				for (Iterator<Soul> it = souls.iterator(); it.hasNext(); ) {
					Soul soul = it.next();
					if (player.world == soul.world && soul.pos.isWithinDistance(player.getPos(), 1))
						if (transferInventory(player, soul))
							it.remove();
				}
			}
		});

	}

	private static void renderSoul(Soul soul) {

		if (soul.world.isChunkLoaded(soul.pos))
			soul.world.addParticle(ParticleTypes.EXPLOSION, soul.pos.getX(), soul.pos.getY(), soul.pos.getZ(), 1, 1, 1);
	}

	private static boolean transferInventory(PlayerEntity player, Soul soul) {
		PlayerInventory playerInv = player.inventory;

		if (!soul.offhand.isEmpty()) {
			if (playerInv.offHand.get(0).isEmpty())
				playerInv.offHand.set(0, soul.offhand);
			else
				soul.main.add(soul.offhand);
			soul.offhand = ItemStack.EMPTY;
		}

		for (int i = 0; i < 4; i++) {
			if (!soul.armor.get(i).isEmpty()) {
				if (playerInv.armor.get(i).isEmpty())
					playerInv.armor.set(i, soul.armor.get(i));
				else
					soul.main.add(soul.armor.get(i));
				soul.armor.set(i, ItemStack.EMPTY);
			}
		}

		for (int i = 0; i < soul.main.size(); i++) {
			ItemStack stack = soul.main.get(i);
			System.out.println(stack.getCount());
			playerInv.insertStack(stack);
			System.out.println(stack.getCount());
			if (stack.isEmpty()) {
				soul.main.remove(i);
				i--;
			}
		}
		System.out.println(soul.main);
		return soul.main.isEmpty();
	}
}
