package com.minenash.soulguard.souls;

import com.minenash.soulguard.SoulGuard;
import com.minenash.soulguard.config.Config;
import com.minenash.soulguard.config.SoulParticle;
import dev.emi.trinkets.api.TrinketsApi;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;

import java.util.*;

public class Soul {

    public String id;
    public long createdAt;
    public boolean released;
    public boolean locked;
    public boolean sealed;
    public int releaseIn;
    public int despawnIn;
    public int sealsIn;

    public final BlockPos pos;
    public final RegistryKey<World> worldId;
    public ServerWorld world;
    public UUID player;

    public List<ItemStack> main;
    public List<ItemStack> armor;
    public ItemStack offhand;

    public int experience;

    public Soul(Vec3d pos, World world, PlayerEntity player, boolean wasKilledByPlayer) {
        this.id = generateId();
        this.createdAt = System.currentTimeMillis();
        this.pos = new BlockPos(pos);
        this.world = (ServerWorld) world;
        this.worldId = RegistryKey.of(Registry.DIMENSION, world.getRegistryKey().getValue());
        this.player = player.getUuid();

        this.releaseIn = Math.max(-1, Config.minutesUntilSoulIsVisibleToAllPlayers * 1200);
        this.despawnIn = Math.max(-1, Config.minutesUntilSoulDespawns * 1200);
        this.sealsIn = 0;
        this.released = this.releaseIn == -1;
        this.locked = false;
        this.sealed = true;

        this.armor = player.inventory.armor;
        this.offhand = player.inventory.offHand.get(0);

        List<ItemStack> playerMain = new ArrayList<>(player.inventory.main);
        if (SoulGuard.HAS_TRINKETS) {
            Inventory inv = TrinketsApi.getTrinketsInventory(player);
            for (int i = 0; i < inv.size(); i++)
                playerMain.add(inv.getStack(i));
        }

        List<ItemStack> main = new ArrayList<>();
        main.add(ItemStack.EMPTY);
        for (ItemStack item : playerMain) {
            for (ItemStack i : new ArrayList<>(main)) {
                if (item.getItem() == i.getItem() && ItemStack.areTagsEqual(item, i)) {
                    int count = i.getCount() + item.getCount();
                    int max = i.getMaxCount();
                    i.setCount(Math.min(count, max));
                    item.setCount(count < max ? 0 : count - max);
                    break;
                }
            }
            if (!item.isEmpty())
                main.add(item.copy());
        }

        main.remove(0);

        while (main.size() > 45) {
            player.dropItem(main.get(main.size()-1), false);
            main.remove(main.size()-1);
        }

        this.main = main;

        this.experience = player.totalExperience;

        SoulGuard.LOGGER.info("--------------------");
        SoulGuard.LOGGER.info(player.getGameProfile().getName() + " XP Report:");
        SoulGuard.LOGGER.info("Incoming XP: " + this.experience);


        if (wasKilledByPlayer && Config.dropRewardXpWhenKilledByPlayer &&!this.world.getGameRules().getBoolean(GameRules.KEEP_INVENTORY) && !player.isSpectator())
            experience -= Math.min(player.experienceLevel * 7, 100);

        SoulGuard.LOGGER.info("XP After Award: " + this.experience);

        SoulGuard.LOGGER.info("XP Lost: " + this.experience * (Config.percentXpLostOnDeath / 100.0));
        this.experience -= this.experience * (Config.percentXpLostOnDeath / 100.0);

        SoulGuard.LOGGER.info("XP After Loss: " + this.experience);

        int dropXp = (int) (this.experience * (Config.percentXpDroppedOnDeathAfterLoss / 100.0));
        SoulGuard.LOGGER.info("XP Dropped: " + dropXp);
        this.experience -= dropXp;

        dropXp(dropXp);

        SoulGuard.LOGGER.info("XP After Drop: " + this.experience);

        if (this.experience < 0)
            this.experience = 0;

        SoulGuard.LOGGER.info("XP In Soul: " + this.experience);
        SoulGuard.LOGGER.info("--------------------");

    }

    private void dropXp(int xp) {
        while (xp > 0) {
            int j = ExperienceOrbEntity.roundToOrbSize(xp);
            xp -= j;
            world.spawnEntity(new ExperienceOrbEntity(world, pos.getX(), pos.getY(), pos.getZ(), j));
        }
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
        releaseIn = tag.getInt("releaseIn");
        createdAt = tag.getLong("createdAt");
        despawnIn = tag.getInt("despawnIn");
        sealsIn = 0;
        sealed = true;

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

    public int getStackCount() {
        int count = main.size();
        if (!offhand.isEmpty())
            count += 9;
        else
            for (ItemStack stack : armor)
                if (!stack.isEmpty()) {
                    count += 9;
                    break;
                }
        return count;
    }

    public boolean process(MinecraftServer server) {
        if (world == null) {
            world = server.getWorld(worldId);
            if (world == null)
                return false;
        }

        if (!locked) {
            if (despawnIn == 0)
                return true;
            else if (despawnIn > -1)
                despawnIn--;

            if (releaseIn < 1)
                released = true;
            else if (releaseIn > -1)
                releaseIn--;
        }

        if (!world.isChunkLoaded(pos))
            return false;

        ServerPlayerEntity host = SoulGuard.server.getPlayerManager().getPlayer(this.player);
        render(host);


        if (released) {
            List<ServerPlayerEntity> players = world.getPlayers(p -> p.isAlive() && pos.isWithinDistance(p.getPos(), 1));
            for (int i = 0; i < players.size() && main.size() + armor.size() + (offhand.isEmpty() ? 0 : 1) > 0; i++) {
                if (locked)
                    players.get(i).sendMessage(new LiteralText("§4Soul §e" + id + "§4 is locked"), true);
                else
                    transferInventory(players.get(i));
            }
        }
        else if (host != null && host.isAlive() && pos.isWithinDistance(host.getPos(),1)) {
            if (locked)
                host.sendMessage(new LiteralText("§cSoul §e" + id + "§c is locked"), true);
            else
                transferInventory(host);
        }

        SoulManager.soulsProcessedThisTick.add(this);

        return main.isEmpty() && experience == 0;

    }

    private void render(ServerPlayerEntity host) {
        (locked ? Config.lockedParticles : released ? Config.releasedParticles : Config.boundedParticles).forEach(p -> p.render(this, pos, host));

        for (Soul soul : SoulManager.soulsProcessedThisTick)
            if (soul.pos.isWithinDistance(pos, Config.exclusiveSoundRadius))
                return;

        (locked ? Config.lockedSounds : released ? Config.releasedSounds : Config.boundedSounds).forEach(p -> p.play(pos, host, released));
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


        SoulGuard.LOGGER.debug("XP In Soul " + id + ": " + this.experience);
        player.addExperience(experience);
        experience = 0;

    }

    public CompoundTag toTag() {
        CompoundTag tag = new CompoundTag();
        tag.putString("id", id);
        tag.putLong("createdAt", createdAt);

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
        tag.putBoolean("sealed", sealed);
        tag.putInt("releaseIn", releaseIn);
        tag.putInt("despawnIn", despawnIn);
        tag.putInt("sealsIn", sealsIn);

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
        while (SoulManager.idToSoul.containsKey(id.toString()));
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
