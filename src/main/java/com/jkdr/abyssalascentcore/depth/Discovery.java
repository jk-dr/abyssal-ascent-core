package com.jkdr.abyssalascentcore.depth;

import io.netty.handler.codec.DecoderException;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/** Which dimensions a player has been in, The server keeps the record with the player and the client is sent it to decide what the depth bar shows. */
public final class Discovery {
    private static final String KEY = "abyssalascentcore_discovered";
    private static final String CAUGHT_UP_KEY = "abyssalascentcore_discovered_caught_up";
    /** More dimensions than a pack could have; a packet claiming more is malformed. */
    private static final int MAX_ENTRIES = 256;

    private static volatile Set<ResourceLocation> clientDiscovered = Set.of();

    private Discovery() {}

    // ---- server ----

    public static Set<ResourceLocation> of(ServerPlayer player) {
        CompoundTag persisted = player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);
        Set<ResourceLocation> discovered = new LinkedHashSet<>();
        for (Tag tag : persisted.getList(KEY, Tag.TAG_STRING)) {
            ResourceLocation id = ResourceLocation.tryParse(tag.getAsString());
            if (id != null) discovered.add(id);
        }
        return discovered;
    }

    /** Records that the player has been in the dimension. Returns true if this is the first time. */
    public static boolean discover(ServerPlayer player, ResourceLocation dimension) {
        CompoundTag data = player.getPersistentData();
        CompoundTag persisted = data.getCompound(Player.PERSISTED_NBT_TAG);
        ListTag list = persisted.getList(KEY, Tag.TAG_STRING);

        String id = dimension.toString();
        for (Tag tag : list) {
            if (tag.getAsString().equals(id)) return false;
        }
        list.add(StringTag.valueOf(id));
        persisted.put(KEY, list);
        data.put(Player.PERSISTED_NBT_TAG, persisted);
        return true;
    }

    /**
     * Whether the one-off catch-up has been done for this player
     * Players who were already part way up the stack when discovery was added have been through every dimension below the one they are in.
     */
    public static boolean caughtUp(ServerPlayer player) {
        return player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG).getBoolean(CAUGHT_UP_KEY);
    }

    /**
     * Marks the dimension the player is in, and every dimension below it in the stack (later in the top-to-bottom list),
     * As entered, once. Returns true if that changed anything worth telling the client.
     */
    public static boolean catchUp(ServerPlayer player, List<DimensionStack.Entry> stack) {
        ResourceLocation current = player.level().dimension().location();
        int index = -1;
        for (int i = 0; i < stack.size(); i++) {
            if (stack.get(i).dimension().equals(current)) index = i;
        }
        // Not in the stack (yet): wait until they are, so the dimensions below can be worked out.
        if (index < 0) return false;

        boolean changed = false;
        for (int i = index; i < stack.size(); i++) {
            changed |= discover(player, stack.get(i).dimension());
        }

        CompoundTag data = player.getPersistentData();
        CompoundTag persisted = data.getCompound(Player.PERSISTED_NBT_TAG);
        persisted.putBoolean(CAUGHT_UP_KEY, true);
        data.put(Player.PERSISTED_NBT_TAG, persisted);
        return changed;
    }

    public static void write(Set<ResourceLocation> dimensions, FriendlyByteBuf buf) {
        buf.writeVarInt(dimensions.size());
        for (ResourceLocation id : dimensions) buf.writeResourceLocation(id);
    }

    public static Set<ResourceLocation> read(FriendlyByteBuf buf) {
        int size = buf.readVarInt();
        if (size < 0 || size > MAX_ENTRIES) throw new DecoderException("Invalid discovery size " + size);
        Set<ResourceLocation> dimensions = new HashSet<>(size);
        for (int i = 0; i < size; i++) dimensions.add(DimensionStack.readId(buf));
        return dimensions;
    }

    // ---- client ----

    public static void applySynced(Set<ResourceLocation> dimensions) {
        clientDiscovered = Set.copyOf(dimensions);
    }

    public static boolean isDiscovered(ResourceLocation dimension) {
        return clientDiscovered.contains(dimension);
    }

    /** Only for building lists in tests and tools. */
    static List<ResourceLocation> asList(Set<ResourceLocation> dimensions) {
        return new ArrayList<>(dimensions);
    }
}
