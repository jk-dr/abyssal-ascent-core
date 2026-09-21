package com.jkdr.abyssalascentcore.mixins.compat.infiniteabyss;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Entering Infinite Abyss' sixth layer without Depth Resistance gives Depth Sickness and tells the player "You need depth resistance to survive down here!".
 * Depth Resistance is no longer needed, so neither happens.
 * <p>
 * The KubeJS fix just seemed out of place for the pack
 */
@Pseudo
@Mixin(targets = "net.mcreator.infiniteabyss.procedures.SixthLayerPlayerEntersDimensionProcedure", remap = false)
public abstract class MixinSixthLayerPlayerEntersDimension {

    @Inject(method = "execute(Lnet/minecraft/world/level/LevelAccessor;DDDLnet/minecraft/world/entity/Entity;)V",
            at = @At("HEAD"), cancellable = true, remap = false)
    private static void aacore$noDepthResistanceNeeded(LevelAccessor world, double x, double y, double z, Entity entity, CallbackInfo ci) {
        ci.cancel();
    }
}