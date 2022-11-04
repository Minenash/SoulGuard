package com.minenash.soulguard.inspect;

import com.minenash.soulguard.souls.Soul;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import org.apache.commons.lang3.text.WordUtils;

import java.util.List;

public class SoulInventory implements Inventory {

    private final Soul soul;
    private final int size;
    private final int trinketSize;

    public SoulInventory(Soul soul, int size) {
        this.size = size;
        this.soul = soul;
        this.trinketSize = soul.trinkets.size();

        for (int j = soul.main.size(); j < size; j++)
            soul.main.add(ItemStack.EMPTY);

    }

    @Override
    public int size() { return size; }

    @Override
    public boolean isEmpty() { return size > 0; }

    @Override
    public ItemStack getStack(int slot) {
        ItemStack item = getStackInternal(slot);
        if (!item.isOf(Items.AIR))
            return item;

        item = new ItemStack(Items.STRUCTURE_VOID);
        if (slot == 0) return item.setCustomName(Text.literal("Head"));
        if (slot == 1) return item.setCustomName(Text.literal("Chest"));
        if (slot == 2) return item.setCustomName(Text.literal("Legs"));
        if (slot == 3) return item.setCustomName(Text.literal("Feet"));
        if (slot == 4) return item.setCustomName(Text.literal("Offhand"));

        if (slot > 4 && slot < 5 + trinketSize) {
            StringBuilder str2 = new StringBuilder();
            for (String part : soul.trinkets.get(slot - 5).slot().split(" "))
                str2.append(part.substring(0, 1).toUpperCase()).append(part.substring(1));
            return item.setCustomName(Text.literal(str2.toString()));
        }

        if (slot >= 5 + trinketSize && slot < soul.getOPStackCount())
            return item.setCustomName(Text.literal( "Inventory " + (slot - 5 - trinketSize) ));

        return ItemStack.EMPTY;
    }


    public ItemStack getStackInternal(int slot) {
        if (slot >= 0 && slot < 4)
            return soul.armor.get(3-slot);
        if (slot == 4)
            return soul.offhand;
        if (slot > 4 && slot < 5 + trinketSize)
            return soul.trinkets.get( slot-5 ).itemStack();
        if (slot >= 5 + trinketSize && slot < soul.getOPStackCount())
            return soul.main.get(slot - 5 - trinketSize);

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
//        if (slot > 4 && slot < 5 + trinketSize)
//            soul.trinkets.put( slots.get(slot-5), itemStack );
        if (slot >= 5 + trinketSize && slot < size)
            soul.main.set(slot-5-trinketSize, stack);

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
