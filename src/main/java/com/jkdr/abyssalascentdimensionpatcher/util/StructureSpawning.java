//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.jkdr.abyssalascentdimensionpatcher.util;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

public class StructureSpawning {

    public static boolean createSpawnStructure(ServerLevel level, BlockPos pos) {

        ResourceLocation structureRL =
                new ResourceLocation("abyssalascentdimensionpatcher", "spawn_prism_prison_v2");

        StructureTemplateManager templateManager = level.getStructureManager();

        Optional<StructureTemplate> optionalTemplate =
                templateManager.get(structureRL);

        if (optionalTemplate.isEmpty()) {
            return false;
        }

        StructureTemplate template = optionalTemplate.get();

        StructurePlaceSettings placementSettings =
                new StructurePlaceSettings()
                        .setRotation(Rotation.NONE)
                        .setMirror(Mirror.NONE)
                        .setIgnoreEntities(false);

        Vec3i templateSize = template.getSize();

        // Clear blocks where the structure will be placed
        for (int x = 0; x < templateSize.getX(); x++) {
            for (int y = 0; y < templateSize.getY(); y++) {
                for (int z = 0; z < templateSize.getZ(); z++) {

                    BlockPos p = pos.offset(x, y, z);

                    BlockState state = level.getBlockState(p);
                    Block block = state.getBlock();

                    ResourceLocation id =
                            BuiltInRegistries.BLOCK.getKey(block);

                    if (state.is(Blocks.WATER) ||
                        (id != null && id.equals(new ResourceLocation("undergarden", "virulent_mix")))) {

                        level.setBlock(p, Blocks.AIR.defaultBlockState(), 3);
                    }
                }
            }
        }

        return template.placeInWorld(
                level,
                pos,
                pos,
                placementSettings,
                level.getRandom(),
                2
        );
    }
}