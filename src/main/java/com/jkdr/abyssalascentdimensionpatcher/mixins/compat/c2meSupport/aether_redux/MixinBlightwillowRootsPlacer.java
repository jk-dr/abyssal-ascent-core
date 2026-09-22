package com.jkdr.abyssalascentdimensionpatcher.mixins.patch.c2meSupport.aether_redux;

// --- Java Utility Imports ---
import java.util.Map;
import java.util.HashMap;
import java.util.function.BiConsumer;

// --- Minecraft Imports ---
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider; 
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacer; // Still needed for the direct call

import com.jkdr.abyssalascentdimensionpatcher.mixins.invokers.MixinTrunkPlacer;

// --- Mixin Imports ---
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

// --- Aether Redux Specific Imports ---
import net.zepalesque.redux.world.tree.root.BlightwillowRootsPlacer;
import net.zepalesque.redux.util.ArrayUtil; 
import org.apache.commons.lang3.ArrayUtils;


@Mixin(BlightwillowRootsPlacer.class)
public abstract class MixinBlightwillowRootsPlacer {

    // Keep all necessary original shadows for fields and abstract methods
    @Shadow(remap = false)
    protected Map<BlockPos, Boolean> placements;
    
    @Shadow(remap = false) 
    protected static boolean isDirt(BlockState state) { throw new AssertionError(); }

    @Shadow(remap = false)
    protected abstract boolean validRootPos(LevelSimulatedReader level, BlockPos pos);
    @Shadow(remap = false)
    protected abstract void unshuffle();
    @Shadow(remap = false)
    protected abstract boolean validateAll(LevelSimulatedReader level, Map<BlockPos, Boolean> placements);
    @Shadow(remap = false)
    private static Direction[] HORIZONTAL_PLANE_SHUFFLE;
    @Shadow(remap = false)
    protected int maxRootDepth;
    
    @Shadow(remap = false)
    protected BlockStateProvider wood; 


    // *** The problematic @Shadow for setDirtAt is now safely REMOVED ***

    /**
     * @author YourName
     * @reason Makes BlightwillowRootsPlacer thread-safe by changing 'placements'
     * from an instance field to a local variable to prevent ConcurrentModificationException with C2ME.
     */
    @Overwrite
    public boolean placeRoots(LevelSimulatedReader level, BiConsumer<BlockPos, BlockState> setter, RandomSource random, BlockPos origin, BlockPos trunkOrigin, TreeConfiguration treeConfig) {

        // FIX 1: Local Map for thread safety (ConcurrentModificationException fix)
        Map<BlockPos, Boolean> localPlacements = new HashMap<>(); 
        
        // Correctly reference the static shadow method
        if (level.isStateAtPosition(origin.below(), MixinBlightwillowRootsPlacer::isDirt)) {
            return false;
        }

        ArrayUtil.shuffle(HORIZONTAL_PLANE_SHUFFLE, random); 

        int height = trunkOrigin.getY() - origin.getY();

        for(int i = 0; i < height; i++) localPlacements.put(origin.above(i), false);

        int baseRootHeight = Math.max(height - 5, 2);

        for (Direction d : Direction.Plane.HORIZONTAL) {
            int rootSize = baseRootHeight + ArrayUtils.indexOf(HORIZONTAL_PLANE_SHUFFLE, d);
            BlockPos rootStart = origin.relative(d, 1);
            int min = 0;

            for (int i = -1; i > -2 - maxRootDepth; i--) {
                BlockPos test = rootStart.above(i);
                if (this.validRootPos(level, test))
                    if (i < -maxRootDepth) {
                        unshuffle();
                        return false;
                    } else continue;
                min = i + 1;
                break;
            }

            for (int i = min; i < rootSize; i++) {
                BlockPos pos = rootStart.above(i);
                if (i < rootSize - 1 && validRootPos(level, pos.above())) {
                    localPlacements.put(pos, false);
                } else if (validRootPos(level, pos)) {
                    localPlacements.put(pos, true);
                }
            }
        }

        unshuffle();

        if (validateAll(level, localPlacements)) {
            localPlacements.forEach((pos, useWood) -> setter.accept(pos, !useWood ? treeConfig.trunkProvider.getState(random, pos) : this.wood.getState(random, pos)));
            
            // FIX 3: Direct call is now safe and correct due to the Access Transformer.
            MixinTrunkPlacer.invokeSetDirtAt(level, setter, random, origin.below(), treeConfig); 
            
            return true;
        } else return false;
    }
}