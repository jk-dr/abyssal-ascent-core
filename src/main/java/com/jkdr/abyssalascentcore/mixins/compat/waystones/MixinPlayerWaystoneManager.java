package com.jkdr.abyssalascentcore.mixins.compat.waystones;

import com.jkdr.abyssalascentcore.compat.WaystoneStackCost;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.blay09.mods.waystones.api.IWaystone;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Waystones prices a waystone in another dimension at a flat cost.
 * For two dimensions of the same dimension stack this makes it use its normal distance pricing instead,
 * with the stack laid out as one tall world: the waystone counts as being in the player's dimension,
 * and the vertical gap between them is the height of the dimensions in between plus how far each is from its dimension's roof.
 */
@Pseudo
@Mixin(targets = "net.blay09.mods.waystones.core.PlayerWaystoneManager", remap = false)
public abstract class MixinPlayerWaystoneManager {

    // The flat-cost branch is taken when the waystone's dimension differs from the player's; say it does not.
    @WrapOperation(method = "getExperienceLevelCost", remap = false, at = @At(value = "INVOKE",
            target = "Lnet/blay09/mods/waystones/api/IWaystone;getDimension()Lnet/minecraft/resources/ResourceKey;", remap = false))
    private static ResourceKey<Level> aacore$treatStackAsOneDimension(IWaystone waystone, Operation<ResourceKey<Level>> original,
                                                                     @Local(argsOnly = true) Entity entity) {
        if (WaystoneStackCost.pricedByStack(entity, waystone)) return entity.level().dimension();
        return original.call(waystone);
    }

    // The distance is measured at the player's own height, so move the target by the vertical gap through the stack.
    @WrapOperation(method = "getExperienceLevelCost", remap = false, at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/player/Player;distanceToSqr(DDD)D", remap = true))
    private static double aacore$distanceThroughStack(Player player, double x, double y, double z, Operation<Double> original,
                                                     @Local(argsOnly = true) IWaystone waystone) {
        double offset = WaystoneStackCost.verticalOffset(player, waystone);
        return original.call(player, x, Double.isNaN(offset) ? y : y + offset, z);
    }
}
