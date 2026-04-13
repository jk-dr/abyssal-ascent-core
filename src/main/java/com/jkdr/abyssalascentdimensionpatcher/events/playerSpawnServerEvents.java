//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.jkdr.abyssalascentdimensionpatcher.events;

import com.google.common.collect.ImmutableSet;
import com.jkdr.abyssalascentdimensionpatcher.data.SpawnPosData;
import com.jkdr.abyssalascentdimensionpatcher.util.ModInternalConfig;
import com.jkdr.abyssalascentdimensionpatcher.util.ServerDimStackLevelLoaded;
import com.jkdr.abyssalascentdimensionpatcher.util.ServerMessages;
import com.jkdr.abyssalascentdimensionpatcher.util.StructureSpawning;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.registries.ForgeRegistries;
import org.slf4j.Logger;

@EventBusSubscriber(
        modid = "abyssalascentdimensionpatcher",
        bus = Bus.FORGE
)
public class playerSpawnServerEvents {

    private static final Logger LOGGER = LogUtils.getLogger();

    private static Set<Block> VALID_GROUND_BLOCKS = null;

    private static final ResourceKey<Biome> FORCESPAWNINBIOME = ResourceKey.create(
            Registries.BIOME, new ResourceLocation("undergarden", "gronglegrowth")
    );


    private static Set<Block> getValidGroundBlocks() {

        if (VALID_GROUND_BLOCKS == null) {

            VALID_GROUND_BLOCKS = ImmutableSet.of(
                    ForgeRegistries.BLOCKS.getValue(new ResourceLocation("undergarden", "deepturf_block")),
                    ForgeRegistries.BLOCKS.getValue(new ResourceLocation("undergarden", "frozen_deepturf_block")),
                    ForgeRegistries.BLOCKS.getValue(new ResourceLocation("undergarden", "deepsoil"))
            ).stream()
                    .filter(Objects::nonNull)
                    .filter(block -> block != Blocks.AIR)
                    .collect(ImmutableSet.toImmutableSet());

            LOGGER.info("Built the valid ground blocks set. Found {} blocks.", VALID_GROUND_BLOCKS.size());
        }

        return VALID_GROUND_BLOCKS;
    }


    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        ServerDimStackLevelLoaded.resetDimensionTickingFlags();
    }


    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {

        LOGGER.info("Initiated server level optimisations, will not tick any dimensions surrounding the player");

        ServerDimStackLevelLoaded.initDimensionStackKeys(event.getServer());
    }


    @SubscribeEvent
    public static void onPlayerLeave(PlayerEvent.PlayerLoggedOutEvent event) {
        ServerDimStackLevelLoaded.resetDimensionTickingFlags();
    }


    @SubscribeEvent
    public static void onPlayerChangeDimension(PlayerEvent.PlayerChangedDimensionEvent event) {

        ServerPlayer player = (ServerPlayer) event.getEntity();

        MinecraftServer server = player.getServer();

        ResourceKey<Level> toDim = event.getTo();

        ServerLevel toWorld = server.getLevel(toDim);

        LOGGER.info("Server level {} has {} players",
                toWorld,
                toWorld.players().size());

        ServerDimStackLevelLoaded.resetDimensionTickingFlags();
    }


    @SubscribeEvent
    public static void onPlayerFirstJoin(PlayerEvent.PlayerLoggedInEvent event) {

        ServerDimStackLevelLoaded.resetDimensionTickingFlags();

        ServerPlayer player = (ServerPlayer) event.getEntity();

        MinecraftServer server = player.getServer();

        if (server == null) return;

        ServerLevel undergardenLevel =
                server.getLevel(ModInternalConfig.playerSpawnDimension);

        if (undergardenLevel == null) {

            LOGGER.error("Undergarden dimension not found! Cannot set spawn.");
            return;
        }

        spawnStructure(player, undergardenLevel);

        CompoundTag persistent =
                player.getPersistentData().getCompound("PlayerPersisted");


        if (!persistent.getBoolean("abyssal_ascent_first_join")) {

            persistent.putBoolean("abyssal_ascent_first_join", true);
            player.getPersistentData().put("PlayerPersisted", persistent);

            ServerMessages.welcome(player);


            Advancement advancement =
                    server.getAdvancements().getAdvancement(
                            new ResourceLocation("minecraft", "story/enter_the_nether"));

            if (advancement == null) {

                LOGGER.error(
                        "Why don't we have vanilla achievements? Abyssal Ascent Error: Most likely a modified instance");
                return;
            }

            AdvancementProgress progress =
                    player.getAdvancements().getOrStartProgress(advancement);


            if (player.level().dimension() == Level.OVERWORLD && !progress.isDone()) {

                if (player.getRespawnPosition() == null) {

                    BlockPos rawPos = getSpawnCoordinates(undergardenLevel);

                    BlockPos adjustedPos = rawPos.offset(0, 4, 0);

                    player.teleportTo(
                            undergardenLevel,
                            adjustedPos.getX() + 0.5,
                            adjustedPos.getY(),
                            adjustedPos.getZ() + 0.5,
                            player.getYRot(),
                            player.getXRot()
                    );

                    player.setRespawnPosition(
                            ModInternalConfig.playerSpawnDimension,
                            adjustedPos,
                            0.0F,
                            true,
                            false);
                }
            } else {

                LOGGER.info("Returning player detected, sending structure info.");

                ServerMessages.spawnStructureNew(
                        player,
                        getSpawnCoordinates(undergardenLevel));
            }
        }
    }


    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {

        ServerPlayer player = (ServerPlayer) event.getEntity();

        if (player.getRespawnPosition() == null
                && player.level().dimension() == Level.OVERWORLD) {

            MinecraftServer server = player.getServer();

            if (server == null) return;

            ServerLevel undergardenLevel =
                    server.getLevel(ModInternalConfig.playerSpawnDimension);

            if (undergardenLevel == null) return;

            if (player.level() != undergardenLevel) {

                BlockPos rawPos = getSpawnCoordinates(undergardenLevel);

                BlockPos adjustedPos = rawPos.offset(0, 4, 0);

                player.teleportTo(
                        undergardenLevel,
                        adjustedPos.getX() + 0.5,
                        adjustedPos.getY(),
                        adjustedPos.getZ() + 0.5,
                        player.getYRot(),
                        player.getXRot()
                );

                player.setRespawnPosition(
                        ModInternalConfig.playerSpawnDimension,
                        adjustedPos,
                        0.0F,
                        true,
                        false);
            }
        }
    }


    private static void spawnStructure(ServerPlayer player, ServerLevel level) {

        SpawnPosData spawnData = SpawnPosData.get(level);

        if (spawnData.getSpawnPos() == null) {

            LOGGER.info("Building spawn structure, didn't exist before");

            BlockPos spawnBlockData = getSpawnCoordinates(level);

            BlockPos offsetStructure = spawnBlockData.offset(-11, -2, -11);

            LOGGER.info("Structure offset: {}, ORIGINAL {}", offsetStructure, spawnBlockData);

            StructureSpawning.createSpawnStructure(level, offsetStructure);
        }
    }

    public static BlockPos getSpawnCoordinates(ServerLevel level) {

        SpawnPosData spawnData = SpawnPosData.get(level);
        BlockPos customSpawnPos = spawnData.getSpawnPos();

        if (customSpawnPos != null) {
            return customSpawnPos;
        }

        BlockPos bedCompatibleSpawn = level.getSharedSpawnPos();

        Predicate<Holder<Biome>> biomePredicate =
                biomeHolder -> biomeHolder.is(FORCESPAWNINBIOME);

        BiomeSource biomeSource =
                level.getChunkSource().getGenerator().getBiomeSource();

        RandomState randomState =
                level.getChunkSource().randomState();

        Climate.Sampler sampler =
                randomState.sampler();

        RandomSource random = level.getRandom();

        int searchRadiusInQuart = 800;

        Pair<BlockPos, Holder<Biome>> biomeResult =
                biomeSource.findBiomeHorizontal(
                        bedCompatibleSpawn.getX() / 4,
                        bedCompatibleSpawn.getY(),
                        bedCompatibleSpawn.getZ() / 4,
                        searchRadiusInQuart,
                        biomePredicate,
                        random,
                        sampler
                );

        if (biomeResult != null) {
            bedCompatibleSpawn = biomeResult.getFirst();
        }

        BlockPos finalSpawnPos =
                findSafeSpotNearby(level, bedCompatibleSpawn, 16).above();

        spawnData.setSpawnPos(finalSpawnPos);

        return finalSpawnPos;
    }

    public static BlockPos findSafeSpotNearby(ServerLevel level, BlockPos initialPos, int radiusChunks) {

        int baseX = initialPos.getX();
        int baseZ = initialPos.getZ();

        for (int dx = -radiusChunks; dx <= radiusChunks; dx++) {
            for (int dz = -radiusChunks; dz <= radiusChunks; dz++) {

                int chunkX = (baseX >> 4) + dx;
                int chunkZ = (baseZ >> 4) + dz;

                ChunkAccess chunk =
                        level.getChunk(chunkX, chunkZ, ChunkStatus.FULL, true);

                if (chunk != null) {

                    int x = (chunkX << 4) + 8;
                    int z = (chunkZ << 4) + 8;

                    BlockPos candidate =
                            findValidSpawn(level,
                                    new BlockPos(x, level.getMaxBuildHeight() - 15, z));

                    if (candidate != null) {
                        return candidate;
                    }
                }
            }
        }

        LOGGER.warn("No safe spawn found nearby, falling back to initialPos");
        return initialPos;
    }

    public static BlockPos findValidSpawn(ServerLevel level, BlockPos pos) {

        int x = pos.getX();
        int z = pos.getZ();

        int airLength = 0;
        BlockPos lastRecordedAir = null;

        for (int y = pos.getY(); y > level.getMinBuildHeight() + 30; y -= 4) {

            BlockPos current = new BlockPos(x, y, z);

            if (!level.getBlockState(current).isAir()) {
                airLength = 0;
            }

            airLength++;

            if (airLength >= 3) {

                if (level.getBlockState(pos).isSolid()) {
                    break;
                }

                lastRecordedAir = current;
            }
        }

        if (lastRecordedAir != null) {

            int maxSearchDown = 0;

            for (int y = lastRecordedAir.getY(); y > level.getMinBuildHeight() + 30; y--) {

                BlockPos current = new BlockPos(x, y, z);

                if (level.getBlockState(current).isSolid()) {

                    maxSearchDown++;

                    Block block =
                            level.getBlockState(current).getBlock();

                    if (getValidGroundBlocks().contains(block)) {
                        return current;
                    }

                    if (maxSearchDown >= 3) {
                        break;
                    }
                }
            }
        }

        return null;
    }
}