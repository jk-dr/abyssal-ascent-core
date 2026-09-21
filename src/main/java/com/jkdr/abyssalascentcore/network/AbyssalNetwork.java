package com.jkdr.abyssalascentcore.network;

import com.jkdr.abyssalascentcore.AbyssalAscentCore;
import com.jkdr.abyssalascentcore.config.LocatorConfig;
import com.jkdr.abyssalascentcore.depth.Discovery;
import com.jkdr.abyssalascentcore.depth.DimensionStack;
import com.jkdr.abyssalascentcore.depth.Locator;
import com.jkdr.abyssalascentcore.mining.MiningRules;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

/** Sends the server's mining rules to clients so they can predict (and not show) breaks the server will deny. */
public final class AbyssalNetwork {
    private static final String PROTOCOL = "3";
    private static final int MAX_JSON_LENGTH = 1 << 18;

    private static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(AbyssalAscentCore.MODID, "main"), () -> PROTOCOL, PROTOCOL::equals, PROTOCOL::equals);

    private AbyssalNetwork() {}

    public static void init() {
        CHANNEL.registerMessage(0, SyncMiningRules.class, SyncMiningRules::encode, SyncMiningRules::decode,
                SyncMiningRules::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        CHANNEL.registerMessage(1, SyncDimensionStack.class, SyncDimensionStack::encode, SyncDimensionStack::decode,
                SyncDimensionStack::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        CHANNEL.registerMessage(2, SyncLocator.class, SyncLocator::encode, SyncLocator::decode,
                SyncLocator::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        CHANNEL.registerMessage(3, SyncDiscovery.class, SyncDiscovery::encode, SyncDiscovery::decode,
                SyncDiscovery::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
    }

    public static void sendDiscoveryTo(ServerPlayer player) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new SyncDiscovery(Discovery.of(player)));
    }

    public static void sendLocatorTo(ServerPlayer player, List<Locator.Entry> players) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new SyncLocator(LocatorConfig.showNames(), players));
    }

    public static void sendDimensionStackTo(ServerPlayer player) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new SyncDimensionStack(DimensionStack.compute(player.server)));
    }

    public static void sendRulesTo(ServerPlayer player) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new SyncMiningRules(MiningRules.syncedJson()));
    }

    public static void sendRulesToAll() {
        CHANNEL.send(PacketDistributor.ALL.noArg(), new SyncMiningRules(MiningRules.syncedJson()));
    }

    /**
     * Every message here only ever goes server to client. The registration already says so,
     * but a packet that arrives the other way is dropped here as well, so a client can never make a server run one of these handlers.
     */
    private static void handleOnClient(Supplier<NetworkEvent.Context> context, Runnable action) {
        NetworkEvent.Context ctx = context.get();
        if (ctx.getDirection() == NetworkDirection.PLAY_TO_CLIENT) ctx.enqueueWork(action);
        ctx.setPacketHandled(true);
    }

    private record SyncDiscovery(Set<ResourceLocation> dimensions) {
        static void encode(SyncDiscovery message, FriendlyByteBuf buf) {
            Discovery.write(message.dimensions, buf);
        }

        static SyncDiscovery decode(FriendlyByteBuf buf) {
            return new SyncDiscovery(Discovery.read(buf));
        }

        static void handle(SyncDiscovery message, Supplier<NetworkEvent.Context> context) {
            handleOnClient(context, () -> Discovery.applySynced(message.dimensions));
        }
    }

    private record SyncLocator(boolean names, List<Locator.Entry> players) {
        static void encode(SyncLocator message, FriendlyByteBuf buf) {
            Locator.write(message.names, message.players, buf);
        }

        static SyncLocator decode(FriendlyByteBuf buf) {
            SyncLocator[] result = new SyncLocator[1];
            Locator.read(buf, (names, players) -> result[0] = new SyncLocator(names, players));
            return result[0];
        }

        static void handle(SyncLocator message, Supplier<NetworkEvent.Context> context) {
            handleOnClient(context, () -> Locator.applySynced(message.names, message.players));
        }
    }

    private record SyncDimensionStack(List<DimensionStack.Entry> stack) {
        static void encode(SyncDimensionStack message, FriendlyByteBuf buf) {
            DimensionStack.write(message.stack, buf);
        }

        static SyncDimensionStack decode(FriendlyByteBuf buf) {
            return new SyncDimensionStack(DimensionStack.read(buf));
        }

        static void handle(SyncDimensionStack message, Supplier<NetworkEvent.Context> context) {
            handleOnClient(context, () -> DimensionStack.applySynced(message.stack));
        }
    }

    private record SyncMiningRules(String json) {
        static void encode(SyncMiningRules message, FriendlyByteBuf buf) {
            buf.writeUtf(message.json, MAX_JSON_LENGTH);
        }

        static SyncMiningRules decode(FriendlyByteBuf buf) {
            return new SyncMiningRules(buf.readUtf(MAX_JSON_LENGTH));
        }

        static void handle(SyncMiningRules message, Supplier<NetworkEvent.Context> context) {
            handleOnClient(context, () -> MiningRules.applySynced(message.json));
        }
    }
}