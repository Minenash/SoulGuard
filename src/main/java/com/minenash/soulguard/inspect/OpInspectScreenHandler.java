package com.minenash.soulguard.inspect;

import com.minenash.soulguard.souls.Soul;
import com.minenash.soulguard.souls.SoulManager;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

public class OpInspectScreenHandler extends ScreenHandler {

    private final Soul soul;
    private final PlayerEntity inspector;
    private final PlayerEntity owner;
    private final int size;

    public OpInspectScreenHandler(ScreenHandlerType<?> type, int syncId, PlayerInventory playerInventory, int rows, Soul soul, PlayerEntity inspector) {
        super(type, syncId);
        this.soul = soul;
        this.inspector = inspector;
        this.owner = playerInventory.player;
        this.size = rows*9;
        soul.beingInspectedByOp = true;

        int i = (rows - 4) * 18;

        Inventory inv = new SoulInventory(soul, size);
        inv.onOpen(playerInventory.player);

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
    public void close(PlayerEntity player) {
        super.close(player);
        soul.beingInspectedByOp = false;
        SoulManager.save();
        if (soul.isEmpty(true)) {
            SoulManager.souls.remove( soul );
            SoulManager.idToSoul.remove( soul.id );
            inspector.sendMessage(Text.literal("§aYou've emptied their soul, it's now free"), false);
            owner.sendMessage(Text.literal("§aA server admin has emptied your soul, it's now free"), false);
        }

    }

    @Override
    public void setCursorStack(ItemStack stack) {
        if (stack.isOf(Items.STRUCTURE_VOID) && stack.hasCustomName() )
            super.setCursorStack(ItemStack.EMPTY);
        else
            super.setCursorStack(stack);
    }

    @Override
    public void onSlotClick(int slotIndex, int button, SlotActionType actionType, PlayerEntity player) {
        try {
            onSlotClickInternal(slotIndex, button, actionType, player);
        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void onSlotClickInternal(int slotIndex, int button, SlotActionType actionType, PlayerEntity player) {
        if (slotIndex < 0 || slotIndex >= size) {
            super.onSlotClick(slotIndex, button, actionType, player);
            return;
        }
        if (slotIndex >= soul.getOPStackCount())
            return;

        Slot slot = this.slots.get(slotIndex);
        if (!slot.getStack().isOf(Items.STRUCTURE_VOID)) {
            super.onSlotClick(slotIndex, button, actionType, player);
            return;
        }

        if (actionType == SlotActionType.CLONE || actionType == SlotActionType.THROW)
            return;
        if (actionType == SlotActionType.SWAP) {
            ItemStack stack = player.getInventory().getStack(button);
            if (!stack.isEmpty()) {
                slot.setStack(stack);
                player.getInventory().setStack(button, ItemStack.EMPTY);
            }
            return;
        }

        super.onSlotClick(slotIndex, button, actionType, player);
    }

    @Override
    public ItemStack transferSlot(PlayerEntity player, int index) {

        ItemStack itemStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot.hasStack()) {
            ItemStack itemStack2 = slot.getStack();
            itemStack = itemStack2.copy();
            if (index < size)
                if (!this.insertItem(itemStack2, size, this.slots.size(), true))
                    return ItemStack.EMPTY;
            else if (!this.insertItem(itemStack2, 0, size, false))
                return ItemStack.EMPTY;

            if (itemStack2.isEmpty())
                slot.setStack(ItemStack.EMPTY);
            else
                slot.markDirty();
        }

        return itemStack;
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return true;
    }

}
