package com.jkdr.abyssalascentcore.events;

import com.jkdr.abyssalascentcore.AbyssalAscentCore;
import net.minecraft.network.protocol.game.ClientboundSetPassengersPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * When a player rides a vehicle (an aircraft, a boat) through a portal,
 * The game tells their client who is riding what as the vehicle is moved between dimensions.
 * The client only understands that message once the vehicle is in the world it is looking.
 * <p>
 * Immersive Portals retries it once: if the crossing takes longer,
 * The message is dropped ("Received passengers for unknown entity").
 * The client then disagrees with the server about whether the player is riding, and things like dismounting stop working. The riding state is sent again shortly after the crossing.
 */
@EventBusSubscriber(modid = AbyssalAscentCore.MODID)
public class RidingResyncEvents {
    /** Ticks after a crossing at which the riding state is sent again. */
    private static final int[] RESEND_AFTER_TICKS = {5, 20, 60};

    /** Player -> server ticks at which to resend. Only used from the server thread. */
    private static final Map<UUID, List<Integer>> PENDING = new HashMap<>();

    @SubscribeEvent
    public static void onDimensionChange(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || player.getVehicle() == null) return;

        int now = player.server.getTickCount();
        List<Integer> due = new ArrayList<>();
        for (int delay : RESEND_AFTER_TICKS) due.add(now + delay);
        PENDING.put(player.getUUID(), due);
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || PENDING.isEmpty()) return;

        int now = event.getServer().getTickCount();
        PENDING.entrySet().removeIf(entry -> {
            ServerPlayer player = event.getServer().getPlayerList().getPlayer(entry.getKey());
            if (player == null) return true;

            List<Integer> due = entry.getValue();
            boolean resend = due.removeIf(tick -> tick <= now);
            if (resend) {
                Entity vehicle = player.getVehicle();
                if (vehicle != null) player.connection.send(new ClientboundSetPassengersPacket(vehicle));
            }
            return due.isEmpty();
        });
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        PENDING.remove(event.getEntity().getUUID());
    }
}
