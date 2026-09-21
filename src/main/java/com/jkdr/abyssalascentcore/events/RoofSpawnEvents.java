package com.jkdr.abyssalascentcore.events;

import com.jkdr.abyssalascentcore.AbyssalAscentCore;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.LevelAccessor;
import net.minecraftforge.event.entity.living.MobSpawnEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

import java.util.EnumSet;
import java.util.Set;

/**
 * Nothing may spawn on the roof of a dimension: the top layers of the world, and anything above its logical height
 * (the bedrock roof of ceilinged dimensions and the space above it). Spawns caused directly by a player or a command
 * (eggs, dispensers, buckets, summoning and so on) are left alone.
 */
@EventBusSubscriber(modid = AbyssalAscentCore.MODID)
public class RoofSpawnEvents {
    /** How many layers below the top of the dimension still count as roof. */
    private static final int ROOF_LAYERS = 5;

    private static final Set<MobSpawnType> PLAYER_DRIVEN = EnumSet.of(
            MobSpawnType.SPAWN_EGG, MobSpawnType.COMMAND, MobSpawnType.DISPENSER, MobSpawnType.BUCKET,
            MobSpawnType.MOB_SUMMONED, MobSpawnType.TRIGGERED, MobSpawnType.CONVERSION, MobSpawnType.BREEDING);

    // Stops natural spawn attempts early, before the mob is finalized.
    @SubscribeEvent
    public static void onPositionCheck(MobSpawnEvent.PositionCheck event) {
        if (!PLAYER_DRIVEN.contains(event.getSpawnType()) && isOnRoof(event.getLevel(), event.getEntity().getY())) {
            event.setResult(Event.Result.DENY);
        }
    }

    // Catches every other kind of spawn (chunk generation, structures, spawners, events...).
    @SubscribeEvent
    public static void onFinalizeSpawn(MobSpawnEvent.FinalizeSpawn event) {
        if (!PLAYER_DRIVEN.contains(event.getSpawnType()) && isOnRoof(event.getLevel(), event.getY())) {
            event.setSpawnCancelled(true);
        }
    }

    private static boolean isOnRoof(LevelAccessor level, double y) {
        int roof = Math.min(level.getMinBuildHeight() + level.dimensionType().logicalHeight(), level.getMaxBuildHeight());
        return y >= roof - ROOF_LAYERS;
    }
}