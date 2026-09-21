package com.jkdr.abyssalascentcore.events;

import com.jkdr.abyssalascentcore.AbyssalAscentCore;
import com.jkdr.abyssalascentcore.network.AbyssalNetwork;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

/** Tells players the order and height of the dimension stack so their depth meter can be worked out on the client. */
@EventBusSubscriber(modid = AbyssalAscentCore.MODID)
public class DimensionStackSyncEvents {

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        send(event.getEntity());
    }

    // The stack is applied to the world after the server starts, so it is also refreshed as players move about.
    @SubscribeEvent
    public static void onDimensionChange(PlayerEvent.PlayerChangedDimensionEvent event) {
        send(event.getEntity());
    }

    @SubscribeEvent
    public static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        send(event.getEntity());
    }

    private static void send(Object entity) {
        if (entity instanceof ServerPlayer player) AbyssalNetwork.sendDimensionStackTo(player);
    }
}
