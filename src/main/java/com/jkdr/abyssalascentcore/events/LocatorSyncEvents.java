package com.jkdr.abyssalascentcore.events;

import com.jkdr.abyssalascentcore.AbyssalAscentCore;
import com.jkdr.abyssalascentcore.config.LocatorConfig;
import com.jkdr.abyssalascentcore.depth.Locator;
import com.jkdr.abyssalascentcore.network.AbyssalNetwork;
import com.jkdr.abyssalascentcore.summons.Summons;
import com.jkdr.abyssalascentcore.util.ServerMessages;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** Regularly tells each player where the other players they are allowed to see are. */
@EventBusSubscriber(modid = AbyssalAscentCore.MODID)
public class LocatorSyncEvents {
    /** Players whose client currently holds a non-empty list, so an empty list is only sent once. */
    private static final Set<UUID> HOLDS_PLAYERS = ConcurrentHashMap.newKeySet();
    private static final Logger LOGGER = LogUtils.getLogger();
    private static boolean failureLogged;

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (event.getServer().getTickCount() % LocatorConfig.updateIntervalTicks() != 0) return;

        for (ServerPlayer viewer : event.getServer().getPlayerList().getPlayers()) {
            try {
                List<Locator.Entry> visible = Locator.visibleTo(viewer, event.getServer());
                boolean holds = HOLDS_PLAYERS.contains(viewer.getUUID());
                if (visible.isEmpty() && !holds) continue;

                AbyssalNetwork.sendLocatorTo(viewer, visible);
                if (visible.isEmpty()) HOLDS_PLAYERS.remove(viewer.getUUID());
                else HOLDS_PLAYERS.add(viewer.getUUID());
            } catch (RuntimeException | LinkageError e) {
                // A problem with the locator must never crash the server tick.
                if (!failureLogged) {
                    failureLogged = true;
                    LOGGER.error("Locator update failed; it will keep retrying quietly", e);
                }
            }
        }
    }

    // Sent straight away so the client knows whether names are allowed before anything changes.
    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer viewer)) return;
        List<Locator.Entry> visible = Locator.visibleTo(viewer, viewer.server);
        AbyssalNetwork.sendLocatorTo(viewer, visible);
        if (!visible.isEmpty()) HOLDS_PLAYERS.add(viewer.getUUID());
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        HOLDS_PLAYERS.remove(event.getEntity().getUUID());
        ServerMessages.forget(event.getEntity().getUUID());
        Summons.forget(event.getEntity().getUUID());
    }
}
