package com.jkdr.abyssalascentcore.mixins.compat.jade;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import qouteall.imm_ptl.core.block_manipulation.BlockManipulationClient;

/**
 * Looking at a block through an Immersive Portals portal, the game's own crosshair result is only a "miss" and the real
 * hit (in the far dimension's coordinates) is kept by Immersive Portals. Jade traces its own ray in the player's level
 * towards that far position, so it named whatever block was in the way there. It now uses the real hit.
 */
@Pseudo
@Mixin(targets = "snownee.jade.overlay.RayTracing", remap = false)
public abstract class MixinJadeRayTracing {

    @Inject(method = "rayTrace(Lnet/minecraft/world/entity/Entity;D)Lnet/minecraft/world/phys/HitResult;",
            at = @At("HEAD"), cancellable = true, remap = false)
    private void aacore$useRemoteHit(Entity entity, double reach, CallbackInfoReturnable<HitResult> cir) {
        if (BlockManipulationClient.isPointingToPortal()
                && BlockManipulationClient.remoteHitResult instanceof BlockHitResult hit
                && hit.getType() == HitResult.Type.BLOCK) {
            cir.setReturnValue(hit);
        }
    }
}