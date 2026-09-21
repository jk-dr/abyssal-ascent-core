package com.jkdr.abyssalascentcore.compat;

import com.jkdr.abyssalascentcore.depth.DimensionStack;
import net.blay09.mods.waystones.api.IWaystone;
import net.blay09.mods.waystones.config.WaystonesConfig;
import net.minecraft.world.entity.Entity;

import java.util.List;

/**
 * Waystones charges a flat price for a waystone in another dimension.
 * Dimensions of a dimension stack are really one tall world,
 * So a waystone in another dimension of the same stack is priced by how far away it is through the stack.
 * <p>
 * Currently in Abyssal Ascent all warps through waystones are free, however this could be better especially in an SMP environment.
 */
public final class WaystoneStackCost {
    private WaystoneStackCost() {}

    /**
     * How much higher (or lower) the waystone is than the entity when the stack is laid out as one world,
     * Or NaN when the normal Waystones pricing applies: same dimension not both in one stack or distance pricing switched off.
     */
    public static double verticalOffset(Entity entity, IWaystone waystone) {
        if (WaystonesConfig.getActive().xpCost.blocksPerXpLevel <= 0) return Double.NaN;
        if (waystone.getDimension() == entity.level().dimension()) return Double.NaN;

        List<DimensionStack.Entry> stack = DimensionStack.stackFor(entity.level());
        if (stack.isEmpty()) return Double.NaN;

        double from = DimensionStack.distanceFromTop(stack, entity.level().dimension().location(), entity.getY());
        double to = DimensionStack.distanceFromTop(stack, waystone.getDimension().location(), waystone.getPos().getY());
        return to - from;
    }

    public static boolean pricedByStack(Entity entity, IWaystone waystone) {
        return !Double.isNaN(verticalOffset(entity, waystone));
    }
}
