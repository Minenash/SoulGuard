package com.minenash.soulguard;

import com.minenash.soulguard.commands.Commands;
import com.minenash.soulguard.config.ConfigManager;
import com.minenash.soulguard.souls.Soul;
import com.minenash.soulguard.souls.SoulManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Iterator;
import java.util.UUID;

public class SoulGuard implements ModInitializer {

	public static final Logger LOGGER = LogManager.getLogger("SoulGuard");
	public static MinecraftServer server;

	@Override
	public void onInitialize() {
		Commands.register();
		ServerLifecycleEvents.SERVER_STARTING.register( server -> {
			SoulGuard.server = server;
			ConfigManager.load();
			SoulManager.load();
		});

		ServerTickEvents.END_SERVER_TICK.register(server ->	{
			if (!SoulManager.processSouls())
				return;
			Iterator<Soul> iterator = SoulManager.souls.values().iterator();
			while (iterator.hasNext()) {
				if (iterator.next().process(server)) {
					iterator.remove();
					SoulManager.save();
				}
			}
		});
	}

	public static String getPlayer(UUID uuid) {
		return server.getUserCache().getByUuid(uuid).getName();
	}


}
