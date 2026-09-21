package com.jkdr.abyssalascentcore.spawn;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import com.jkdr.abyssalascentcore.data.SpawnPosData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/** Finds (and persists) the fixed world spawn used in the spawn dimension. */
public final class SpawnLocator {
    private static final Logger LOGGER = LogUtils.getLogger();

    private static final ResourceKey<Biome> SPAWN_BIOME =
            ResourceKey.create(Registries.BIOME, new ResourceLocation("undergarden", "gronglegrowth"));

    private static final int BIOME_SEARCH_RADIUS_QUART = 800;
    private static final int SAFE_SPOT_RADIUS_CHUNKS = 16;
    /** How far, in blocks sideways and up or down, a spawn with no ground below it may be moved. */
    private static final int MOVE_RADIUS = 48;
    private static final int MOVE_HEIGHT = 24;

    // Resolved on first use: registries aren't ready at class load. Air marks a block that isn't installed.
    private static final Supplier<Set<Block>> VALID_GROUND = Suppliers.memoize(() ->
            Stream.of("deepturf_block", "frozen_deepturf_block", "deepsoil")
                    .map(name -> ForgeRegistries.BLOCKS.getValue(new ResourceLocation("undergarden", name)))
                    .filter(block -> block != null && block != Blocks.AIR)
                    .collect(Collectors.toUnmodifiableSet()));

    private SpawnLocator() {}

    /** The saved spawn, or a freshly searched one (saved for next time). */
    public static BlockPos getSpawnCoordinates(ServerLevel level) {
        SpawnPosData data = SpawnPosData.get(level);
        BlockPos saved = data.getSpawnPos();
        if (saved != null) return saved;

        BlockPos start = level.getSharedSpawnPos();
        Pair<BlockPos, Holder<Biome>> biome = level.getChunkSource().getGenerator().getBiomeSource().findBiomeHorizontal(
                start.getX() / 4, start.getY(), start.getZ() / 4,
                BIOME_SEARCH_RADIUS_QUART,
                holder -> holder.is(SPAWN_BIOME),
                level.getRandom(),
                level.getChunkSource().randomState().sampler());
        if (biome != null) start = biome.getFirst();

        BlockPos spawn = ensureGroundBelow(level, findSafeSpotNearby(level, start, SAFE_SPOT_RADIUS_CHUNKS).above());
        data.setSpawnPos(spawn);
        return spawn;
    }

    /**
     * The search above can give up and return a position in mid air (or the ground it found can be missing under the spot).
     * A spawn with nothing to stand on is moved to the nearest place that has ground under it and room to stand,
     * preferring the mod's own spawn ground.
     * Only used when the spawn is first chosen: once it is saved the spawn structure has been built around it, and moving it would leave the structure behind.
     */
    static BlockPos ensureGroundBelow(ServerLevel level, BlockPos spawn) {
        if (isStandable(level, spawn)) return spawn;

        BlockPos moved = findStandableNear(level, spawn, true);
        if (moved == null) moved = findStandableNear(level, spawn, false);
        if (moved == null) {
            LOGGER.warn("Spawn {} has no ground below it and nothing suitable was found within {} blocks, keeping it", spawn, MOVE_RADIUS);
            return spawn;
        }

        LOGGER.info("Spawn {} had no ground below it, moved to {}", spawn, moved);
        return moved;
    }

    /** Solid ground under the feet position, and room for a player above it. */
    private static boolean isStandable(ServerLevel level, BlockPos feet) {
        BlockPos below = feet.below();
        return level.getBlockState(below).isFaceSturdy(level, below, Direction.UP)
                && level.getFluidState(below).isEmpty()
                && level.getBlockState(feet).getCollisionShape(level, feet).isEmpty()
                && level.getFluidState(feet).isEmpty()
                && level.getBlockState(feet.above()).getCollisionShape(level, feet.above()).isEmpty()
                && level.getFluidState(feet.above()).isEmpty();
    }

    /** The closest standable position to {@code origin}, searched in rings outwards and in each column nearest in height first. */
    @Nullable
    private static BlockPos findStandableNear(ServerLevel level, BlockPos origin, boolean onSpawnGroundOnly) {
        Set<Block> valid = VALID_GROUND.get();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        for (int ring = 1; ring <= MOVE_RADIUS; ring++) {
            for (int dx = -ring; dx <= ring; dx++) {
                for (int dz = -ring; dz <= ring; dz++) {
                    // Only the outline of this ring.
                    if (Math.max(Math.abs(dx), Math.abs(dz)) != ring) continue;

                    int x = origin.getX() + dx;
                    int z = origin.getZ() + dz;
                    level.getChunk(x >> 4, z >> 4, ChunkStatus.FULL, true);

                    for (int dy = 0; dy <= MOVE_HEIGHT; dy++) {
                        for (int sign = dy == 0 ? 1 : -1; sign <= 1; sign += 2) {
                            pos.set(x, origin.getY() + sign * dy, z);
                            if (!isStandable(level, pos)) continue;
                            if (onSpawnGroundOnly && !valid.contains(level.getBlockState(pos.below()).getBlock())) continue;
                            return pos.immutable();
                        }
                    }
                }
            }
        }
        return null;
    }

    /** Scans chunk centres outwards ring by ring so the nearest valid spot wins and the fewest chunks get generated. */
    public static BlockPos findSafeSpotNearby(ServerLevel level, BlockPos initialPos, int radiusChunks) {
        int baseChunkX = initialPos.getX() >> 4;
        int baseChunkZ = initialPos.getZ() >> 4;
        int scanY = level.getMaxBuildHeight() - 15;

        for (int ring = 0; ring <= radiusChunks; ring++) {
            for (int dx = -ring; dx <= ring; dx++) {
                // Only the outline of the ring: full columns at the left/right edges, single cells in between.
                int step = Math.abs(dx) == ring ? 1 : 2 * ring;
                for (int dz = -ring; dz <= ring; dz += Math.max(step, 1)) {
                    int chunkX = baseChunkX + dx;
                    int chunkZ = baseChunkZ + dz;
                    level.getChunk(chunkX, chunkZ, ChunkStatus.FULL, true);

                    BlockPos candidate = findValidSpawn(level, new BlockPos((chunkX << 4) + 8, scanY, (chunkZ << 4) + 8));
                    if (candidate != null) return candidate;
                }
            }
        }

        LOGGER.warn("No safe spawn found nearby, falling back to {}", initialPos);
        return initialPos;
    }

    @Nullable
    public static BlockPos findValidSpawn(ServerLevel level, BlockPos pos) {
        int x = pos.getX();
        int z = pos.getZ();
        int floor = level.getMinBuildHeight() + 30;

        // Coarse pass: drop down in steps of 4 looking for the last open air pocket.
        int airLength = 0;
        BlockPos lastAir = null;
        for (int y = pos.getY(); y > floor; y -= 4) {
            if (!level.getBlockState(new BlockPos(x, y, z)).isAir()) airLength = 0;
            if (++airLength >= 3) {
                if (level.getBlockState(pos).isSolid()) break;
                lastAir = new BlockPos(x, y, z);
            }
        }
        if (lastAir == null) return null;

        // Fine pass: walk down to the first few solid blocks and accept a valid ground block.
        Set<Block> valid = VALID_GROUND.get();
        int solidSeen = 0;
        for (int y = lastAir.getY(); y > floor; y--) {
            BlockPos current = new BlockPos(x, y, z);
            var state = level.getBlockState(current);
            if (!state.isSolid()) continue;
            if (valid.contains(state.getBlock())) return current;
            if (++solidSeen >= 3) break;
        }
        return null;
    }
}