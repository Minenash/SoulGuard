package com.minenash.soulguard;

import com.minenash.soulguard.commands.Commands;
import com.minenash.soulguard.config.ConfigManager;
import com.minenash.soulguard.souls.Soul;
import com.minenash.soulguard.souls.SoulManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

public class SoulGuard implements ModInitializer {

	public static final Logger LOGGER = LogManager.getLogger("SoulGuard");
	public static MinecraftServer server;

	public static List<PlayerEntity> CAN_SEE_BOUNDED_SOULS = new ArrayList<>();

	private static int saveInterval = 0;

	@Override
	public void onInitialize() {
		Commands.register();
		ServerLifecycleEvents.SERVER_STARTING.register( server -> {
			SoulGuard.server = server;
			ConfigManager.load(true);
			SoulManager.load();
		});

		ServerTickEvents.END_SERVER_TICK.register(server ->	{
			if (!SoulManager.processSouls())
				return;

			boolean save = ++saveInterval % 1200 == 0;
			Iterator<Soul> iterator = SoulManager.souls.values().iterator();
			while (iterator.hasNext()) {
				if (iterator.next().process(server)) {
					iterator.remove();
					save = true;
				}
			}

			if (save) {
				SoulManager.save();
				saveInterval = 0;
			}

		});
	}

	public static String getPlayer(UUID uuid) {
		return server.getUserCache().getByUuid(uuid).getName();
	}


}
