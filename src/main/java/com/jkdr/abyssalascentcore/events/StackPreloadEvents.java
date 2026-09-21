package com.jkdr.abyssalascentcore.events;

import com.jkdr.abyssalascentcore.AbyssalAscentCore;
import com.jkdr.abyssalascentcore.config.StackPreloadConfig;
import com.jkdr.abyssalascentcore.depth.DimensionStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import qouteall.imm_ptl.core.chunk_loading.ChunkLoader;
import qouteall.imm_ptl.core.chunk_loading.DimensionalChunkPos;
import qouteall.imm_ptl.core.chunk_loading.NewChunkTrackingGraph;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Immersive Portals only loads the far side of a dimension stack portal around where the player is now,
 * And widens it as they get close. A player falling (or flying up) fast can reach the portal before those chunks are generated,
 * sent and ready, and then clips through the ground that is not there yet.
 * This projects the player's velocity ahead and asks Immersive Portals to load, and send to that player, the chunks where they will enter each next dimension.
 */
@EventBusSubscriber(modid = AbyssalAscentCore.MODID)
public class StackPreloadEvents {
    /** More than this in one tick is a teleport, not movement. */
    private static final double TELEPORT_SPEED = 8.0;
    private static final int MAX_HOPS = 2;
    private static final int STACK_REFRESH_TICKS = 100;

    private static final Map<UUID, State> STATES = new HashMap<>();
    private static List<DimensionStack.Entry> stack = List.of();
    private static final Logger LOGGER = LogUtils.getLogger();
    private static boolean failureLogged;

    private static final class State {
        ResourceKey<Level> dimension;
        Vec3 position;
        List<ChunkLoader> loaders = List.of();
        /** Moving vertically fast enough that the far side of a stack portal has to be loaded wider than usual. */
        boolean fast;
    }

    /** Whether the player is currently falling or rising fast. Used by the chunk loading mixin, on the server thread. */
    public static boolean isFastVertical(ServerPlayer player) {
        State state = STATES.get(player.getUUID());
        return state != null && state.fast;
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        if (!StackPreloadConfig.enabled()) {
            if (!STATES.isEmpty()) clearAll(event);
            return;
        }

        int tick = event.getServer().getTickCount();
        if (tick % STACK_REFRESH_TICKS == 0 || (stack.isEmpty() && tick % 20 == 0)) {
            stack = DimensionStack.compute(event.getServer());
        }

        for (ServerPlayer player : event.getServer().getPlayerList().getPlayers()) {
            try {
                update(player);
            } catch (RuntimeException | LinkageError e) {
                // Preloading is an optimisation: it must never crash the server tick.
                if (!failureLogged) {
                    failureLogged = true;
                    LOGGER.error("Stack preloading failed; it will keep retrying quietly", e);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        State state = STATES.remove(player.getUUID());
        if (state != null) removeLoaders(player, state);
    }

    private static void clearAll(TickEvent.ServerTickEvent event) {
        for (ServerPlayer player : event.getServer().getPlayerList().getPlayers()) {
            State state = STATES.get(player.getUUID());
            if (state != null) removeLoaders(player, state);
        }
        STATES.clear();
    }

    private static void update(ServerPlayer player) {
        State state = STATES.computeIfAbsent(player.getUUID(), id -> new State());
        ResourceKey<Level> dimension = player.level().dimension();
        Vec3 position = player.position();

        // Velocity is measured from where the player was last tick: the server does not keep an accurate one for players.
        Vec3 velocity = state.dimension == dimension && state.position != null ? position.subtract(state.position) : Vec3.ZERO;
        state.dimension = dimension;
        state.position = position;

        List<ChunkLoader> wanted = List.of();
        state.fast = Math.abs(velocity.y) >= StackPreloadConfig.minVerticalSpeed() && velocity.lengthSqr() < TELEPORT_SPEED * TELEPORT_SPEED;
        if (state.fast) {
            wanted = project(position, velocity, dimension);
        }

        if (wanted.equals(state.loaders)) return;
        removeLoaders(player, state);
        for (ChunkLoader loader : wanted) NewChunkTrackingGraph.addPerPlayerAdditionalChunkLoader(player, loader);
        state.loaders = wanted;
    }

    /** Walks the player's projected path through the stack and returns a chunk loader for each dimension entry point on the way. */
    private static List<ChunkLoader> project(Vec3 position, Vec3 velocity, ResourceKey<Level> dimension) {
        int index = -1;
        for (int i = 0; i < stack.size(); i++) {
            if (stack.get(i).dimension().equals(dimension.location())) index = i;
        }
        if (index < 0) return List.of();

        boolean down = velocity.y < 0.0;
        double speed = Math.abs(velocity.y);
        double remaining = speed * StackPreloadConfig.lookaheadTicks();
        double x = position.x;
        double y = position.y;
        double z = position.z;

        List<ChunkLoader> loaders = new ArrayList<>();
        for (int hop = 0; hop < MAX_HOPS; hop++) {
            DimensionStack.Entry current = stack.get(index);
            double toEdge = down ? y - current.minY() : current.maxY() - y;
            if (toEdge < 0.0) toEdge = 0.0;
            int next = down ? index + 1 : index - 1;
            if (remaining <= toEdge || next < 0 || next >= stack.size()) break;

            // Advance horizontally by the time it takes to reach the edge.
            double ticks = toEdge / speed;
            x += velocity.x * ticks;
            z += velocity.z * ticks;
            remaining -= toEdge;

            DimensionStack.Entry entry = stack.get(next);
            y = down ? entry.maxY() : entry.minY();
            index = next;

            ResourceKey<Level> key = ResourceKey.create(Registries.DIMENSION, entry.dimension());
            loaders.add(new ChunkLoader(new DimensionalChunkPos(key, new ChunkPos(BlockPos.containing(x, y, z))), StackPreloadConfig.radiusChunks()));
        }
        return loaders;
    }

    private static void removeLoaders(ServerPlayer player, State state) {
        for (ChunkLoader loader : state.loaders) NewChunkTrackingGraph.removePerPlayerAdditionalChunkLoader(player, loader);
        state.loaders = List.of();
    }
}
