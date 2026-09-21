package com.jkdr.abyssalascentcore.events;

import com.jkdr.abyssalascentcore.AbyssalAscentCore;
import com.jkdr.abyssalascentcore.depth.DimensionStack;
import com.jkdr.abyssalascentcore.depth.Discovery;
import com.jkdr.abyssalascentcore.network.AbyssalNetwork;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import org.slf4j.Logger;

/**
 * Notes each dimension a player enters, however they got there (a portal, a dimension stack crossing, a command),
 * By checking which dimension they are in a few times a second, and tells them so the depth bar can reveal it.
 */
@EventBusSubscriber(modid = AbyssalAscentCore.MODID)
public class DiscoveryEvents {
    private static final int CHECK_INTERVAL_TICKS = 10;
    private static final Logger LOGGER = LogUtils.getLogger();
    private static boolean failureLogged;

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        Discovery.discover(player, player.level().dimension().location());
        AbyssalNetwork.sendDiscoveryTo(player);
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (event.getServer().getTickCount() % CHECK_INTERVAL_TICKS != 0) return;

        for (ServerPlayer player : event.getServer().getPlayerList().getPlayers()) {
            try {
                ResourceLocation dimension = player.level().dimension().location();
                boolean changed = Discovery.discover(player, dimension);

                // Players who were already part way up the stack get every dimension below them counted as entered, once.
                // The stack may not be set up yet when they log in, so this is tried until it can be worked out.
                if (!Discovery.caughtUp(player)) {
                    changed |= Discovery.catchUp(player, DimensionStack.stackFor(player.level()));
                }

                if (changed) AbyssalNetwork.sendDiscoveryTo(player);
            } catch (RuntimeException | LinkageError e) {
                if (!failureLogged) {
                    failureLogged = true;
                    LOGGER.error("Dimension discovery failed; it will keep retrying quietly", e);
                }
            }
        }
    }
}
