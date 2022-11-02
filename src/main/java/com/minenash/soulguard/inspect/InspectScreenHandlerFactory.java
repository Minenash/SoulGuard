package com.minenash.soulguard.inspect;

import com.minenash.soulguard.SoulGuard;
import com.minenash.soulguard.souls.Soul;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.text.Text;

public class InspectScreenHandlerFactory {

    public static SimpleNamedScreenHandlerFactory get(Soul soul, PlayerEntity inspector) {
        return new SimpleNamedScreenHandlerFactory((syncId, inv, player) -> getHandler(syncId,inv,player,soul,inspector),
                Text.literal(SoulGuard.getPlayer(soul.player) + "'s Soul [" + soul.experience + " XP]"));
    }

    private static ScreenHandler getHandler(int syncId, PlayerInventory inv, PlayerEntity player, Soul soul, PlayerEntity inspector) {
        boolean op = player.hasPermissionLevel(2);
        int rows = (int) Math.ceil(soul.getStackCount(op) / 9.0);

        ScreenHandlerType<?> type = switch (rows) {
            case 2 -> ScreenHandlerType.GENERIC_9X2;
            case 3 -> ScreenHandlerType.GENERIC_9X3;
            case 4 -> ScreenHandlerType.GENERIC_9X4;
            case 5 -> ScreenHandlerType.GENERIC_9X5;
            case 6 -> ScreenHandlerType.GENERIC_9X6;
            default -> ScreenHandlerType.GENERIC_9X1;
        };

        return op ? new OpInspectScreenHandler(type, syncId, inv, rows, soul, inspector) :
                    new InspectScreenHandler(type, syncId, inv, rows, soul);
    }

}
