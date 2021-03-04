package com.minenash.soulguard;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public class Soul {

    public final BlockPos pos;
    public final World world;
    public List<ItemStack> main;
    public List<ItemStack> armor;
    public ItemStack offhand;

    public Soul(Vec3d pos, World world, PlayerInventory inventory) {
        this.pos = new BlockPos(pos);
        this.world = world;
        this.armor = inventory.armor;
        this.offhand = inventory.offHand.get(0);

        List<ItemStack> main = new ArrayList<>();
        for (ItemStack item : inventory.main)
            if (!item.isEmpty())
                main.add(item);
        this.main = main;
    }

    @Override
    public String toString() {
        return "Soul{" +
                "pos=" + pos +
                ", world=" + world +
                ", armor=" + armor +
                ", offhand=" + offhand +
                ", main=" + main +
                '}';
    }
}
