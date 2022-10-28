package com.minenash.soulguard.inspect;

import com.minenash.soulguard.souls.Soul;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;

public class SoulInventory implements Inventory {

    private final Soul soul;
    private final int size;

    public SoulInventory(Soul soul, int size) {
        this.size = size;
        this.soul = soul;
        for (int j = soul.main.size(); j < size; j++)
            soul.main.add(ItemStack.EMPTY);
    }

    @Override
    public int size() { return size; }

    @Override
    public boolean isEmpty() { return size > 0; }

    @Override
    public ItemStack getStack(int slot) {
        if (slot >= 0 && slot < 4)
            return soul.armor.get(3-slot);
        if (slot == 4)
            return soul.offhand;
        if (slot > 4 && slot < size)
            return soul.main.get(slot-5);
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeStack(int slot, int amount) {
        return slot >= 0 && slot < size && !getStack(slot).isEmpty() && amount > 0 ? getStack(slot).split(amount) : ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeStack(int slot) {
        ItemStack itemStack = getStack(slot);
        if (itemStack.isEmpty())
            return ItemStack.EMPTY;
        else {
            setStack(slot, ItemStack.EMPTY);
            return itemStack;
        }
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        if (slot >= 0 && slot < 4)
            soul.armor.set(3-slot, stack);
        if (slot == 4)
            soul.offhand = stack;
        if (slot > 4 && slot < size)
            soul.main.set(slot-5, stack);

        if (!stack.isEmpty() && stack.getCount() > this.getMaxCountPerStack())
            stack.setCount(this.getMaxCountPerStack());

    }

    @Override
    public void markDirty() {}

    @Override
    public boolean canPlayerUse(PlayerEntity player) { return true; }

    @Override
    public void clear() {}
}
