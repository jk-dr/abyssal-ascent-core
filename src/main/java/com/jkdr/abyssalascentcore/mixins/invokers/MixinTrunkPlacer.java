package com.jkdr.abyssalascentcore.mixins.invokers;

import java.util.function.BiConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin({TrunkPlacer.class})
public interface MixinTrunkPlacer {
    @Invoker("setDirtAt")
    static void invokeSetDirtAt(LevelSimulatedReader level, BiConsumer<BlockPos, BlockState> setter, RandomSource random, BlockPos pos, TreeConfiguration config) {
        throw new AssertionError("Mixin Invoker failed to inject!");
    }
}
