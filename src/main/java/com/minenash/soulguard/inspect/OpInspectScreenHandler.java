package com.minenash.soulguard.inspect;

import com.minenash.soulguard.souls.Soul;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;

public class OpInspectScreenHandler extends ScreenHandler {

    private final Soul soul;

    public OpInspectScreenHandler(ScreenHandlerType<?> type, int syncId, PlayerInventory playerInventory, int rows, Soul soul) {
        super(type, syncId);
        this.soul = soul;
        soul.beingInspectedByOp = true;

        int i = (rows - 4) * 18;

//        ItemStack[] items = soul.main.toArray(new ItemStack[0]);

        Inventory inv = new SoulInventory(soul, rows*9);
//        Inventory inv = new SimpleInventory(items);
//        Inventory inv = createInspectInventory(soul, rows);
        inv.onOpen(playerInventory.player);

        int n, m;

        for(m = 0; m < 9; ++m)
            this.addSlot(new Slot(inv, m, 8 + m * 18, 18));
        for(n = 1; n < rows; ++n)
            for(m = 0; m < 9; ++m)
                this.addSlot(new Slot(inv, m + n * 9, 8 + m * 18, 18 + n * 18));

        for(n = 0; n < 3; ++n)
            for(m = 0; m < 9; ++m)
                this.addSlot(new Slot(playerInventory, m + n * 9 + 9, 8 + m * 18, 103 + n * 18 + i));

        for(n = 0; n < 9; ++n)
            this.addSlot(new Slot(playerInventory, n, 8 + n * 18, 161 + i));
    }

    @Override
    public void close(PlayerEntity player) {
        super.close(player);
        soul.main.removeIf(ItemStack::isEmpty);
        soul.beingInspectedByOp = false;

    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return true;
    }

}
