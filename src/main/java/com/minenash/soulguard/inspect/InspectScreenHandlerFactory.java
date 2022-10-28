package com.minenash.soulguard.inspect;


import com.minenash.soulguard.SoulGuard;
import com.minenash.soulguard.inspect.InspectScreenHandler;
import com.minenash.soulguard.inspect.OpInspectScreenHandler;
import com.minenash.soulguard.souls.Soul;
import com.mojang.datafixers.util.Pair;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;

public class InspectScreenHandlerFactory {

    public static SimpleNamedScreenHandlerFactory get(Soul soul) {
        return new SimpleNamedScreenHandlerFactory((syncId, inv, player) -> getHandler(syncId,inv,player,soul),
                new LiteralText(SoulGuard.getPlayer(soul.player) + "'s Soul [" + soul.experience + " XP]"));
    }

    private static ScreenHandler getHandler(int syncId, PlayerInventory inv, PlayerEntity player, Soul soul) {
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

        return op ? new OpInspectScreenHandler(type, syncId, inv, rows, soul) :
                    new InspectScreenHandler(type, syncId, inv, rows, soul);
    }

}
