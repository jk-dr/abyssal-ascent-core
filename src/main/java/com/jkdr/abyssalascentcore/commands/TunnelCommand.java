package com.jkdr.abyssalascentcore.commands;

import com.jkdr.abyssalascentcore.AbyssalAscentCore;
import com.jkdr.abyssalascentcore.depth.DimensionStack;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;

/**
 * /aa debug tunnel [radius]: clears a vertical shaft, the full height of every dimension of the dimension stack, at the
 * position the command is run from. Radius 0 is a single column, 1 is 3x3, and so on.
 * The bottom dimension keeps a floor with a little water on it to land in.
 * <p>
 * This command solely exists for development in falling (to provide a more seamless experience to the player)
 */
@Mod.EventBusSubscriber(modid = AbyssalAscentCore.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class TunnelCommand {
    private static final int DEFAULT_RADIUS = 1;
    private static final int MAX_RADIUS = 8;
    private static final int WATER_DEPTH = 2;
    private static final int FLAGS = 2 | 16 | 32;

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(
            Commands.literal(AbyssalAscentCore.CMDALIAS)
                .then(Commands.literal("debug")
                    .requires(source -> source.hasPermission(2))
                    .then(Commands.literal("tunnel")
                        .executes(context -> run(context, DEFAULT_RADIUS))
                        .then(Commands.argument("radius", IntegerArgumentType.integer(0, MAX_RADIUS))
                            .executes(context -> run(context, IntegerArgumentType.getInteger(context, "radius"))))))
        );
    }

    private static int run(CommandContext<CommandSourceStack> context, int radius) {
        CommandSourceStack source = context.getSource();
        MinecraftServer server = source.getServer();
        BlockPos origin = BlockPos.containing(source.getPosition());

        // Every dimension of the stack with its own height range; outside a stack, just the level the command is run in.
        List<Shaft> shafts = new ArrayList<>();
        for (DimensionStack.Entry entry : DimensionStack.compute(server)) {
            ServerLevel level = server.getLevel(ResourceKey.create(Registries.DIMENSION, entry.dimension()));
            if (level != null) shafts.add(new Shaft(level, entry.minY(), entry.maxY()));
        }
        if (shafts.isEmpty()) {
            ServerLevel level = source.getLevel();
            shafts.add(new Shaft(level, level.getMinBuildHeight(), level.getMaxBuildHeight()));
        }

        BlockState air = Blocks.AIR.defaultBlockState();
        BlockState water = Blocks.WATER.defaultBlockState();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        int cleared = 0;
        for (int i = 0; i < shafts.size(); i++) {
            Shaft shaft = shafts.get(i);
            boolean bottom = i == shafts.size() - 1;
            for (int x = origin.getX() - radius; x <= origin.getX() + radius; x++) {
                for (int z = origin.getZ() - radius; z <= origin.getZ() + radius; z++) {
                    int from = shaft.minY;
                    if (bottom) {
                        // The lowest dimension keeps its floor, so whoever comes down the shaft lands on something.
                        from = floorOf(shaft, pos, x, z) + 1;
                    }
                    for (int y = from; y < shaft.maxY; y++) {
                        pos.set(x, y, z);
                        if (shaft.level.getBlockState(pos).isAir()) continue;
                        if (shaft.level.setBlock(pos, air, FLAGS)) cleared++;
                    }
                    if (bottom) {
                        // ...and lands in water, so a long fall does no damage.
                        for (int y = from; y < from + WATER_DEPTH && y < shaft.maxY; y++) {
                            shaft.level.setBlock(pos.set(x, y, z), water, 2);
                        }
                    }
                }
            }
        }
        int total = cleared;
        int dimensions = shafts.size();
        source.sendSuccess(() -> Component.translatable("message.abyssalascentcore.tunnel_done", total, dimensions), true);
        return total;
    }

    /** The height of the lowest solid block in the column; if there is none, stone is placed at the bottom to be the floor. */
    private static int floorOf(Shaft shaft, BlockPos.MutableBlockPos pos, int x, int z) {
        for (int y = shaft.minY; y < shaft.maxY; y++) {
            if (!shaft.level.getBlockState(pos.set(x, y, z)).isAir()) return y;
        }
        shaft.level.setBlock(pos.set(x, shaft.minY, z), Blocks.STONE.defaultBlockState(), FLAGS);
        return shaft.minY;
    }

    private record Shaft(Level level, int minY, int maxY) {}
}
