package com.minenash.soulguard.souls;

import com.minenash.soulguard.config.Config;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
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
import java.util.Random;
import java.util.UUID;

public class Soul {

    public String id;
    public boolean released = false;
    public boolean locked = false;

    public final BlockPos pos;
    public final RegistryKey<World> worldId;
    public ServerWorld world;
    public UUID player;

    public List<ItemStack> main;
    public List<ItemStack> armor;
    public ItemStack offhand;

    public int experience;

    public Soul(Vec3d pos, World world, PlayerEntity player) {
        this.id = generateId();
        this.pos = new BlockPos(pos);
        this.world = (ServerWorld) world;
        this.worldId = RegistryKey.of(Registry.DIMENSION, world.getRegistryKey().getValue());
        this.player = player.getUuid();

        this.armor = player.inventory.armor;
        this.offhand = player.inventory.offHand.get(0);

        List<ItemStack> main = new ArrayList<>();
        for (ItemStack item : player.inventory.main)
            if (!item.isEmpty())
                main.add(item);
        this.main = main;

        this.experience = player.totalExperience;

        System.out.println("Total: " + this.experience);

        if (Config.dropRewardXpWhenKilledByPlayer &&!this.world.getGameRules().getBoolean(GameRules.KEEP_INVENTORY) && !player.isSpectator())
            experience -= Math.min(player.experienceLevel * 7, 100);

        System.out.println("After reward: " + this.experience);

        this.experience -= this.experience * (Config.percentXpLostOnDeath / 100.0);

        System.out.println("After loss: " + this.experience);

        int dropXp = (int) (this.experience * (Config.percentXpDroppedOnDeathAfterLoss / 100.0));
        this.experience -= dropXp;
        while(dropXp > 0) {
            int j = ExperienceOrbEntity.roundToOrbSize(dropXp);
            dropXp -= j;
            this.world.spawnEntity(new ExperienceOrbEntity(this.world, pos.getX(), pos.getY(), pos.getZ(), j));
        }

        System.out.println("After drop: " + this.experience);

        if (this.experience < 0)
            this.experience = 0;

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
        player = tag.getUuid("player");
        id = tag.getString("id");
        released = tag.getBoolean("released");
        locked = tag.getBoolean("locked");

    }

    public String getPositionString() {
        return pos.getX() + " " + pos.getY() + " " + pos.getZ();
    }

    public int getItemCount() {
        int count = offhand.getCount();
        for (ItemStack stack : main)
            count += stack.getCount();
        for (ItemStack stack : armor)
            count += stack.getCount();
        return count;
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

        boolean mode = player.abilities.creativeMode;
        player.abilities.creativeMode  = false;
        for (int i = 0; i < main.size(); i++) {
            ItemStack stack = main.get(i);
            playerInv.insertStack(stack);
            if (stack.isEmpty()) {
                main.remove(i);
                i--;
            }
        }
        player.abilities.creativeMode = mode;


        player.addExperience(experience);
        experience = 0;

    }

    public CompoundTag toTag() {
        CompoundTag tag = new CompoundTag();
        tag.putString("id", id);

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
        tag.putUuid("player", player);
        tag.putBoolean("released", released);
        tag.putBoolean("locked", locked);

        return tag;
    }

    public static Soul fromTag(CompoundTag tag) {
        return new Soul(tag);
    }

    private static final Random RANDOM = new Random();
    private static final char[] LETTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();

    private static String generateId() {
        StringBuilder id;
        do {
            id = new StringBuilder();
            for (int i = 0; i < 6; i++)
                id.append(LETTERS[RANDOM.nextInt(LETTERS.length)]);
        }
        while (SoulManager.souls.containsKey(id.toString()));
        return id.toString();
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
