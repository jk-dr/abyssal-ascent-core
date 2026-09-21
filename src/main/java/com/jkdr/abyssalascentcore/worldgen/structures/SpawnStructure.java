package com.jkdr.abyssalascentcore.worldgen.structures;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.jkdr.abyssalascentcore.AbyssalAscentCore;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraftforge.registries.ForgeRegistries;
public final class SpawnStructure {

    private static final ResourceLocation TEMPLATE = new ResourceLocation(AbyssalAscentCore.MODID, "spawn_prism_prison_v2");
    private static final ResourceLocation VIRULENT_MIX_ID = new ResourceLocation("undergarden", "virulent_mix");

    // 2 | 16: sync to clients, skip neighbour and shape updates while clearing
    private static final int CLEAR_FLAGS = Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE;
    private static final int PLACE_FLAGS = Block.UPDATE_CLIENTS;

    // Resolved on first use: registries aren't ready at class load, and Undergarden may be absent.
    // Blocks.AIR is the "not present" sentinel; air is skipped before this is compared.
    private static final Supplier<Block> VIRULENT_MIX =
            Suppliers.memoize(() -> ForgeRegistries.BLOCKS.getValue(VIRULENT_MIX_ID));

    private SpawnStructure() {}

    public static boolean create(ServerLevel level, BlockPos origin) {
        return level.getStructureManager().get(TEMPLATE)
                .map(template -> {
                    clearFluidAndMix(level, origin, template.getSize());
                    return template.placeInWorld(
                            level, origin, origin,
                            new StructurePlaceSettings().setIgnoreEntities(false),
                            level.getRandom(),
                            PLACE_FLAGS);
                })
                .orElse(false);
    }

    private static void clearFluidAndMix(ServerLevel level, BlockPos origin, Vec3i size) {
        Block mix = VIRULENT_MIX.get();
        BlockPos end = origin.offset(size.getX() - 1, size.getY() - 1, size.getZ() - 1);

        for (BlockPos p : BlockPos.betweenClosed(origin, end)) {
            BlockState state = level.getBlockState(p);
            if (state.isAir()) continue;

            if (state.is(Blocks.WATER) || state.is(mix)) {
                level.setBlock(p, Blocks.AIR.defaultBlockState(), CLEAR_FLAGS);
            }
        }
    }
}