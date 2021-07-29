package com.minenash.soulguard;

import com.minenash.soulguard.commands.Commands;
import com.minenash.soulguard.config.ConfigManager;
import com.minenash.soulguard.souls.Soul;
import com.minenash.soulguard.souls.SoulManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.LiteralText;
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

	public static final boolean HAS_TRINKETS = FabricLoader.getInstance().isModLoaded("trinkets");

	@Override
	public void onInitialize() {
		Commands.register();
		ServerLifecycleEvents.SERVER_STARTING.register( server -> {
			SoulGuard.server = server;
			ConfigManager.load(true);
			SoulManager.load();
		});

		ServerTickEvents.END_SERVER_TICK.register(SoulManager::processSouls);

		ServerEntityEvents.ENTITY_LOAD.register( (entity, _world) -> {
			if (entity instanceof PlayerEntity && SoulManager.isDisabled() && entity.hasPermissionLevel(2))
				((PlayerEntity)entity).sendMessage(new LiteralText("§c[Soulguard] Last config load was aborted, soul ticking has been disabled for safety"), false);
		});
	}

	public static String getPlayer(UUID uuid) {
		return server.getUserCache().getByUuid(uuid).getName();
	}


}
