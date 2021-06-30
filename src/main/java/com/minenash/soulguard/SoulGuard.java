package com.minenash.soulguard;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

import java.util.Iterator;

public class SoulGuard implements ModInitializer {

	@Override
	public void onInitialize() {
		ServerLifecycleEvents.SERVER_STARTING.register( _server -> SoulSaveManager.load());

		ServerTickEvents.END_SERVER_TICK.register(server ->	{
			Iterator<Soul> iterator = SoulSaveManager.souls.iterator();
			while (iterator.hasNext()) {
				if (iterator.next().process(server)) {
					iterator.remove();
					SoulSaveManager.save();
				}
			}
		});
	}


}
