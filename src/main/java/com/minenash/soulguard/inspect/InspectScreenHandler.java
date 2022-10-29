package com.minenash.soulguard.inspect;

import com.minenash.soulguard.souls.Soul;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;

public class InspectScreenHandler extends ScreenHandler {

    public InspectScreenHandler(ScreenHandlerType<?> type, int syncId, PlayerInventory playerInventory, int rows, Soul soul) {
        super(type, syncId);

        Inventory inv = createInspectInventory(soul, rows);
        inv.onOpen(playerInventory.player);
        int i = (rows - 4) * 18;

        int n, m;
        for(n = 0; n < rows; ++n)
            for(m = 0; m < 9; ++m)
                this.addSlot(new Slot(inv, m + n * 9, 8 + m * 18, 18 + n * 18));

        for(n = 0; n < 3; ++n)
            for(m = 0; m < 9; ++m)
                this.addSlot(new Slot(playerInventory, m + n * 9 + 9, 8 + m * 18, 103 + n * 18 + i));

        for(n = 0; n < 9; ++n)
            this.addSlot(new Slot(playerInventory, n, 8 + n * 18, 161 + i));
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

    public static Inventory createInspectInventory(Soul soul, int rows) {
        ItemStack[] items = new ItemStack[rows*9];
        int i = 0;

        if (!soul.armor.get(3).isEmpty()) items[i++] = soul.armor.get(3);
        if (!soul.armor.get(2).isEmpty()) items[i++] = soul.armor.get(2);
        if (!soul.armor.get(1).isEmpty()) items[i++] = soul.armor.get(1);
        if (!soul.armor.get(0).isEmpty()) items[i++] = soul.armor.get(0);

        if (!soul.offhand.isEmpty())
            items[i++] = soul.offhand;

        for (ItemStack stack : soul.trinkets.values())
            items[i++] = stack;

//        for (; i > 0 && i < 9; i++)
//            items[i] = ItemStack.EMPTY;

        for (ItemStack item : soul.main)
            items[i++] = item;

        for (; i < items.length; i++)
            items[i] = ItemStack.EMPTY;

        return new SimpleInventory(items);
    }

}
