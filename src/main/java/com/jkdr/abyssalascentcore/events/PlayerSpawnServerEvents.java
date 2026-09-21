package com.jkdr.abyssalascentcore.events;

import com.jkdr.abyssalascentcore.AbyssalAscentCore;
import com.jkdr.abyssalascentcore.data.SpawnPosData;
import com.jkdr.abyssalascentcore.spawn.SpawnLocator;
import com.jkdr.abyssalascentcore.util.ModInternalConfig;
import com.jkdr.abyssalascentcore.util.ServerMessages;
import com.jkdr.abyssalascentcore.worldgen.structures.SpawnStructure;
import com.mojang.logging.LogUtils;
import net.minecraft.advancements.Advancement;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import org.slf4j.Logger;

/** Sends new players to the spawn dimension, builds the spawn structure and keeps recalled/respawned players there. */
@EventBusSubscriber(modid = AbyssalAscentCore.MODID, bus = Bus.FORGE)
public class PlayerSpawnServerEvents {
    private static final Logger LOGGER = LogUtils.getLogger();

    private static final ResourceLocation ENTER_NETHER = new ResourceLocation("minecraft", "story/enter_the_nether");

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        ServerPlayer player = (ServerPlayer) event.getEntity();
        MinecraftServer server = player.getServer();
        if (server == null) return;

        ServerLevel spawnLevel = server.getLevel(ModInternalConfig.playerSpawnDimension);
        if (spawnLevel == null) {
            LOGGER.error("Spawn dimension {} not found! Cannot set spawn.", ModInternalConfig.playerSpawnDimension.location());
            return;
        }

        buildSpawnStructureIfMissing(spawnLevel);

        CompoundTag persisted = player.getPersistentData().getCompound("PlayerPersisted");
        if (persisted.getBoolean(ModInternalConfig.FIRST_JOIN_TAG)) return;

        persisted.putBoolean(ModInternalConfig.FIRST_JOIN_TAG, true);
        player.getPersistentData().put("PlayerPersisted", persisted);
        ServerMessages.welcome(player);

        Advancement enterNether = server.getAdvancements().getAdvancement(ENTER_NETHER);
        if (enterNether == null) {
            LOGGER.error("Vanilla advancements are missing, most likely a modified instance");
            return;
        }

        boolean freshPlayer = player.level().dimension() == Level.OVERWORLD
                && !player.getAdvancements().getOrStartProgress(enterNether).isDone();
        if (freshPlayer) {
            if (player.getRespawnPosition() == null) sendToSpawn(player, spawnLevel);
        } else {
            // Existing player who predates the spawn structure: tell them where it is.
            LOGGER.info("Returning player detected, sending structure info.");
            ServerMessages.spawnStructureNew(player, SpawnLocator.getSpawnCoordinates(spawnLevel));
        }
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        ServerPlayer player = (ServerPlayer) event.getEntity();
        if (player.getRespawnPosition() != null || player.level().dimension() != Level.OVERWORLD) return;

        MinecraftServer server = player.getServer();
        if (server == null) return;

        ServerLevel spawnLevel = server.getLevel(ModInternalConfig.playerSpawnDimension);
        if (spawnLevel != null && player.level() != spawnLevel) sendToSpawn(player, spawnLevel);
    }

    private static void buildSpawnStructureIfMissing(ServerLevel level) {
        if (SpawnPosData.get(level).getSpawnPos() != null) return;

        LOGGER.info("Building spawn structure, didn't exist before");
        BlockPos spawn = SpawnLocator.getSpawnCoordinates(level);
        SpawnStructure.create(level, spawn.offset(-11, -2, -11));
    }

    private static void sendToSpawn(ServerPlayer player, ServerLevel spawnLevel) {
        BlockPos pos = SpawnLocator.getSpawnCoordinates(spawnLevel).above(4);
        player.teleportTo(spawnLevel, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, player.getYRot(), player.getXRot());
        player.setRespawnPosition(ModInternalConfig.playerSpawnDimension, pos, 0.0F, true, false);
    }
}