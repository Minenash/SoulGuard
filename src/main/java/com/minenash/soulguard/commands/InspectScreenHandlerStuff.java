package com.minenash.soulguard.commands;


import com.minenash.soulguard.SoulGuard;
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

public class InspectScreenHandlerStuff {

    public static SimpleNamedScreenHandlerFactory createFactory(Soul soul) {
        return new SimpleNamedScreenHandlerFactory((syncId, inv, player) -> getHandler(syncId,inv,player,soul),
                new LiteralText(SoulGuard.getPlayer(soul.player) + "'s Soul [" + soul.experience + " XP]"));
    }

    private static ScreenHandler getHandler(int syncId, PlayerInventory inv, PlayerEntity player, Soul soul) {
       System.out.println("A");
        int rows = (int) Math.ceil(soul.getStackCount() / 9.0);
        System.out.println(soul.getStackCount());
        Inventory soulInv = createInspectInventory(soul, rows);
        System.out.println("B");
        ScreenHandlerType<?> type = switch (rows) {
            case 1 -> ScreenHandlerType.GENERIC_9X1;
            case 2 -> ScreenHandlerType.GENERIC_9X2;
            case 3 -> ScreenHandlerType.GENERIC_9X3;
            case 4 -> ScreenHandlerType.GENERIC_9X4;
            case 5 -> ScreenHandlerType.GENERIC_9X5;
            default -> ScreenHandlerType.GENERIC_9X6;
        };
        System.out.println("C");
        return player.hasPermissionLevel(2) ?
                new GenericContainerScreenHandler(type, syncId, inv, soulInv, rows) :
                new NoInteractContainerScreenHandler(type, syncId, inv, soulInv, rows);
    }

    public static Inventory createInspectInventory(Soul soul, int rows) {
        ItemStack[] items = new ItemStack[rows*9];
        int i = 0;
        System.out.println("D");
        if (!soul.armor.get(3).isEmpty()) items[i++] = soul.armor.get(3);
        if (!soul.armor.get(2).isEmpty()) items[i++] = soul.armor.get(2);
        if (!soul.armor.get(1).isEmpty()) items[i++] = soul.armor.get(1);
        if (!soul.armor.get(0).isEmpty()) items[i++] = soul.armor.get(0);
        System.out.println("E");
        if (!soul.offhand.isEmpty())
            items[i++] = soul.offhand;
        System.out.println("F");
        for (; i > 1 && i < 9; i++)
            items[i] = ItemStack.EMPTY;
        System.out.println("G");
        for (ItemStack item : soul.main) {
            System.out.println(i);
            items[i++] = item;
        }
        System.out.println("H");
        for (; i < items.length; i++)
            items[i] = ItemStack.EMPTY;
        System.out.println("I");
        return new SimpleInventory(items);
    }

    public static class NoInteractContainerScreenHandler extends ScreenHandler {

        public NoInteractContainerScreenHandler(ScreenHandlerType<?> type, int syncId, PlayerInventory playerInventory, Inventory inventory, int rows) {
            super(type, syncId);
            checkSize(inventory, rows * 9);
            inventory.onOpen(playerInventory.player);
            int i = (rows - 4) * 18;

            int n;
            int m;
            for(n = 0; n < rows; ++n) {
                for(m = 0; m < 9; ++m) {
                    this.addSlot(new Slot(inventory, m + n * 9, 8 + m * 18, 18 + n * 18));
                }
            }

            for(n = 0; n < 3; ++n) {
                for(m = 0; m < 9; ++m) {
                    this.addSlot(new Slot(playerInventory, m + n * 9 + 9, 8 + m * 18, 103 + n * 18 + i));
                }
            }

            for(n = 0; n < 9; ++n) {
                this.addSlot(new Slot(playerInventory, n, 8 + n * 18, 161 + i));
            }
        }

        @Override
        public ItemStack onSlotClick(int i, int j, SlotActionType actionType, PlayerEntity playerEntity) {
            if (actionType == SlotActionType.QUICK_MOVE)
                return getSlot(i).getStack();
            return ItemStack.EMPTY;
        }


        @Override
        public boolean canUse(PlayerEntity player) {
            return true;
        }

    }


}
