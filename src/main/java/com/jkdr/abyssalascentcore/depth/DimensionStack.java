package com.jkdr.abyssalascentcore.depth;

import io.netty.handler.codec.DecoderException;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import qouteall.imm_ptl.core.portal.Portal;
import qouteall.imm_ptl.core.portal.global_portals.GlobalPortalStorage;
import qouteall.imm_ptl.core.portal.global_portals.VerticalConnectingPortal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The Immersive Portals dimension stack as an ordered list of dimensions, top first,
 * Each with the height range that is actually part of the stack.
 * The server works the order out from the vertical connecting portals and sends it to clients, which only see the portals of the dimensions they have loaded.
 */
public final class DimensionStack {
    /** One dimension of the stack: the portal planes (or the world limits, where there is no portal) bound it. */
    public record Entry(ResourceLocation dimension, int minY, int maxY) {
        public int height() {
            return maxY - minY;
        }

        void encode(FriendlyByteBuf buf) {
            buf.writeResourceLocation(dimension);
            buf.writeVarInt(minY);
            buf.writeVarInt(maxY);
        }

        static Entry decode(FriendlyByteBuf buf) {
            ResourceLocation dimension = readId(buf);
            int minY = buf.readVarInt();
            int maxY = buf.readVarInt();
            // A nonsense range would divide by zero or overflow later on, so anything odd is refused outright.
            if (Math.abs(minY) > MAX_COORDINATE || Math.abs(maxY) > MAX_COORDINATE || maxY <= minY) {
                throw new DecoderException("Invalid dimension stack range " + minY + ".." + maxY);
            }
            return new Entry(dimension, minY, maxY);
        }
    }

    /** More dimensions than any stack has and the largest height any of them could have. Packets outside these are malformed and would be better to disregard anyways. */
    private static final int MAX_ENTRIES = 64;
    private static final int MAX_COORDINATE = 1_000_000;

    /** Reads a dimension id, refusing anything that is not a valid or reasonably short one. */
    public static ResourceLocation readId(FriendlyByteBuf buf) {
        ResourceLocation id = ResourceLocation.tryParse(buf.readUtf(128));
        if (id == null) throw new DecoderException("Invalid dimension id");
        return id;
    }

    private static volatile List<Entry> clientStack = List.of();

    private DimensionStack() {}

    // ---- server ----

    /** The stack, top to bottom - empty when no dimension stack is set up. */
    public static List<Entry> compute(MinecraftServer server) {
        Map<ResourceKey<Level>, VerticalConnectingPortal> ceilings = new HashMap<>();
        Map<ResourceKey<Level>, VerticalConnectingPortal> floors = new HashMap<>();
        for (ServerLevel level : server.getAllLevels()) {
            List<Portal> portals = GlobalPortalStorage.getGlobalPortals(level);
            if (portals == null) continue;
            for (Portal portal : portals) {
                if (!(portal instanceof VerticalConnectingPortal connecting)) continue;
                (connecting.getNormal().y < 0 ? ceilings : floors).put(level.dimension(), connecting);
            }
        }
        if (floors.isEmpty()) return List.of();

        // The top of the stack has a way down but nothing above it.
        ResourceKey<Level> top = null;
        for (ResourceKey<Level> dimension : floors.keySet()) {
            if (!ceilings.containsKey(dimension)) {
                top = dimension;
                break;
            }
        }
        if (top == null) top = floors.keySet().iterator().next();

        List<Entry> stack = new ArrayList<>();
        Set<ResourceKey<Level>> visited = new HashSet<>();
        ResourceKey<Level> current = top;
        while (current != null && visited.add(current)) {
            ServerLevel level = server.getLevel(current);
            if (level == null) break;

            VerticalConnectingPortal floor = floors.get(current);
            VerticalConnectingPortal ceiling = ceilings.get(current);
            int minY = floor != null ? (int) Math.round(floor.getY()) : level.getMinBuildHeight();
            int maxY = ceiling != null ? (int) Math.round(ceiling.getY()) : level.getMinBuildHeight() + level.dimensionType().height();
            if (maxY > minY) stack.add(new Entry(current.location(), minY, maxY));

            current = floor != null ? floor.dimensionTo : null;
        }
        return stack.size() > 1 ? stack : List.of();
    }

    public static void write(List<Entry> stack, FriendlyByteBuf buf) {
        buf.writeVarInt(stack.size());
        for (Entry entry : stack) entry.encode(buf);
    }

    public static List<Entry> read(FriendlyByteBuf buf) {
        int size = buf.readVarInt();
        if (size < 0 || size > MAX_ENTRIES) throw new DecoderException("Invalid dimension stack size " + size);
        List<Entry> stack = new ArrayList<>(size);
        for (int i = 0; i < size; i++) stack.add(Entry.decode(buf));
        return stack;
    }

    /** The stack as the server sees it, recomputed now and then rather than on every call. Only used from the server thread. */
    private static List<Entry> serverCache = List.of();
    private static int serverCacheTick = Integer.MIN_VALUE;
    private static final int SERVER_CACHE_TICKS = 100;

    /** The stack this level belongs to on its own side: what the client was sent, or what the server works out. */
    public static List<Entry> stackFor(Level level) {
        if (level.isClientSide) return clientStack;

        MinecraftServer server = level.getServer();
        if (server == null) return List.of();
        int tick = server.getTickCount();
        if (tick < serverCacheTick || tick - serverCacheTick >= SERVER_CACHE_TICKS) {
            serverCache = compute(server);
            serverCacheTick = tick;
        }
        return serverCache;
    }

    /**
     * How far down the whole stack a position is: the height of every dimension above its own, plus the distance
     * from the roof of its own. NaN when the dimension is not part of the stack.
     */
    public static double distanceFromTop(List<Entry> stack, ResourceLocation dimension, double y) {
        double above = 0.0;
        for (Entry entry : stack) {
            if (entry.dimension().equals(dimension)) {
                return above + Math.max(0.0, Math.min(entry.height(), entry.maxY() - y));
            }
            above += entry.height();
        }
        return Double.NaN;
    }

    // ---- client ----

    /** The stack the client was sent, top to bottom. */
    public static List<Entry> clientStack() {
        return clientStack;
    }

    public static void applySynced(List<Entry> stack) {
        clientStack = List.copyOf(stack);
    }

    /** Where a player is within one dimension of the stack: its index from the top, and how far down it (0 roof, 1 floor). */
    public record Position(int index, int count, float fractionDown) {}

    /** The player's position within their own dimension, or null when the dimension is not part of the stack. */
    public static Position position(ResourceKey<Level> dimension, double y) {
        return position(dimension.location(), y);
    }

    public static Position position(ResourceLocation dimension, double y) {
        List<Entry> stack = clientStack;
        for (int i = 0; i < stack.size(); i++) {
            Entry entry = stack.get(i);
            if (!entry.dimension().equals(dimension)) continue;
            double down = (entry.maxY() - y) / entry.height();
            return new Position(i, stack.size(), (float) Math.max(0.0, Math.min(1.0, down)));
        }
        return null;
    }

    /**
     * How far up the whole stack the given position is: 0 at the bottom of the lowest dimension, 1 at the roof of the
     * highest, or -1 when the dimension is not part of the stack.
     * <p>
     * The distance down from the top is the height of every dimension above plus the distance from the roof of this one
     * (for example 100 + 100 + 30). Flipping that against the total height (1000 - 230) and dividing by the total gives 0.77.
     */
    public static float progress(ResourceKey<Level> dimension, double y) {
        return progress(dimension.location(), y);
    }

    public static float progress(ResourceLocation dimension, double y) {
        List<Entry> stack = clientStack;
        if (stack.isEmpty()) return -1.0F;

        double total = 0.0;
        double distanceFromTop = 0.0;
        boolean found = false;
        for (Entry entry : stack) {
            if (!found && entry.dimension().equals(dimension)) {
                found = true;
                distanceFromTop = total + Math.max(0.0, Math.min(entry.height(), entry.maxY() - y));
            }
            total += entry.height();
        }
        if (!found || total <= 0.0) return -1.0F;

        double progress = Math.abs(distanceFromTop - total) / total;
        return (float) Math.max(0.0, Math.min(1.0, progress));
    }
}
