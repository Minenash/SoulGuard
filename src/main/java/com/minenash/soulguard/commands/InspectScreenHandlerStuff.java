package com.minenash.soulguard.commands;


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
import net.minecraft.text.LiteralText;

public class InspectScreenHandlerStuff {

    public static SimpleNamedScreenHandlerFactory createFactory(Soul soul) {
        Inventory soulInv = createInspectInventory(soul);

        return new SimpleNamedScreenHandlerFactory((syncId, inv, player) ->
            player.hasPermissionLevel(2)?
                new GenericContainerScreenHandler(ScreenHandlerType.GENERIC_9X5, syncId, inv, soulInv, 5) :
                new NoInteractContainerScreenHandler(ScreenHandlerType.GENERIC_9X5, syncId, inv, soulInv, 5)
        , new LiteralText("Player"));
    }

    public static Inventory createInspectInventory(Soul soul) {
        ItemStack[] items = new ItemStack[45];
        int i = 0;

        if (!soul.armor.get(3).isEmpty()) items[i++] = soul.armor.get(3);
        if (!soul.armor.get(2).isEmpty()) items[i++] = soul.armor.get(2);
        if (!soul.armor.get(1).isEmpty()) items[i++] = soul.armor.get(1);
        if (!soul.armor.get(0).isEmpty()) items[i++] = soul.armor.get(0);

        if (!soul.offhand.isEmpty())
            items[i++] = soul.offhand;

        for (; i > 1 && i < 9; i++)
            items[i] = ItemStack.EMPTY;

        System.out.println(soul.main.size());
        for (ItemStack item : soul.main)
            items[i++] = item;

        for (; i < 45; i++)
            items[i] = ItemStack.EMPTY;

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
