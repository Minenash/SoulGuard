package com.minenash.soulguard;

import com.minenash.soulguard.config.Config;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public class Soul {

    public final BlockPos pos;
    public final RegistryKey<World> worldId;
    public ServerWorld world;

    public List<ItemStack> main;
    public List<ItemStack> armor;
    public ItemStack offhand;

    public int experience;

    public Soul(Vec3d pos, World world, PlayerEntity player) {
        this.pos = new BlockPos(pos);
        this.world = (ServerWorld) world;
        this.worldId = RegistryKey.of(Registry.DIMENSION, world.getRegistryKey().getValue());

        this.armor = player.inventory.armor;
        this.offhand = player.inventory.offHand.get(0);

        List<ItemStack> main = new ArrayList<>();
        for (ItemStack item : player.inventory.main)
            if (!item.isEmpty())
                main.add(item);
        this.main = main;

        this.experience = player.totalExperience;
        if (!this.world.getGameRules().getBoolean(GameRules.KEEP_INVENTORY) && !player.isSpectator())
            experience -= Math.min(player.experienceLevel * 7, 100);
    }

    public Soul(CompoundTag tag) {

        CompoundTag position = tag.getCompound("position");
        pos = new BlockPos(position.getInt("x"), position.getInt("y"), position.getInt("z"));
        worldId = RegistryKey.of(Registry.DIMENSION, new Identifier(position.getString("world")));

        main = new ArrayList<>();
        armor = new ArrayList<>();

        for (Tag itemTag : tag.getList("main_inventory",10))
            main.add(ItemStack.fromTag( (CompoundTag) itemTag));

        for (Tag itemTag : tag.getList("armor_inventory",10))
            armor.add(ItemStack.fromTag( (CompoundTag) itemTag));

        offhand = ItemStack.fromTag( tag.getCompound("offhand_inventory") );

    }

    public boolean process(MinecraftServer server) {

        if (world == null) {
            world = server.getWorld(worldId);
            if (world == null)
                return false;
        }

        if (world.isChunkLoaded(pos)) {
            Config.particles.forEach(p -> p.render(world,pos));

            List<ServerPlayerEntity> players = world.getPlayers(p -> p.isAlive() && pos.isWithinDistance(p.getPos(),1));
            for (int i = 0; i < players.size() && main.size() + armor.size() + (offhand.isEmpty()? 0 : 1) > 0; i++)
                transferInventory(players.get(i));
        }
        return main.isEmpty() && experience == 0;

    }

    private void transferInventory(PlayerEntity player) {
        PlayerInventory playerInv = player.inventory;

        if (!offhand.isEmpty()) {
            if (playerInv.offHand.get(0).isEmpty())
                playerInv.offHand.set(0, offhand);
            else
                main.add(offhand);
            offhand = ItemStack.EMPTY;
        }

        for (int i = 0; i < 4; i++) {
            if (!armor.get(i).isEmpty()) {
                if (playerInv.armor.get(i).isEmpty())
                    playerInv.armor.set(i, armor.get(i));
                else
                    main.add(armor.get(i));
                armor.set(i, ItemStack.EMPTY);
            }
        }

        for (int i = 0; i < main.size(); i++) {
            ItemStack stack = main.get(i);
            playerInv.insertStack(stack);
            if (stack.isEmpty()) {
                main.remove(i);
                i--;
            }
        }

        player.addExperience(experience);
        experience = 0;

    }

    public CompoundTag toTag() {
        CompoundTag tag = new CompoundTag();

        CompoundTag position = new CompoundTag();
        position.putInt("x", pos.getX());
        position.putInt("y", pos.getY());
        position.putInt("z", pos.getZ());
        position.putString("world", world.getRegistryKey().getValue().toString());
        tag.put("position", position);

        ListTag mainItems = new ListTag();
        ListTag armorItems = new ListTag();

        for (ItemStack item : main)
            mainItems.add(item.toTag(new CompoundTag()));

        for (ItemStack item : armor)
            armorItems.add(item.toTag(new CompoundTag()));

        tag.put("main_inventory", mainItems);
        tag.put("armor_inventory", armorItems);
        tag.put("offhand_inventory", offhand.toTag(new CompoundTag()));

        return tag;
    }

    public static Soul fromTag(CompoundTag tag) {
        return new Soul(tag);
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
