package com.jkdr.abyssalascentcore.mixins.compat.ironsspellbooks;

import com.jkdr.abyssalascentcore.spell.PortalRay;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.spells.ender.TeleportSpell;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Blink normally raycasts in the caster's own level,
 * so aiming at a dimension stack ceiling or floor sends the caster into the void beyond the world edge. When the aim ray crosses an Immersive Portals portal,
 * the landing spot is found in the level on the far side and the caster is teleported there.
 */
@Mixin(TeleportSpell.class)
public abstract class MixinTeleportSpell {

    @Inject(method = "onCast", at = @At("HEAD"), remap = false)
    private void aacore$resetTrace(CallbackInfo ci) {
        PortalRay.BLINK.remove();
        PortalRay.clearPendingTeleport();
    }

    // Aim: follow portals. Everything else in findTeleportLocation then runs against the far level.
    @WrapOperation(method = "findTeleportLocation", remap = false, at = @At(value = "INVOKE",
            target = "Lio/redspace/ironsspellbooks/api/util/Utils;getTargetBlock(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/level/ClipContext$Fluid;D)Lnet/minecraft/world/phys/BlockHitResult;", remap = false))
    private static BlockHitResult aacore$aimThroughPortals(Level level, LivingEntity entity, ClipContext.Fluid fluid, double reach, Operation<BlockHitResult> original) {
        PortalRay.BLINK.remove();
        Vec3 start = entity.getEyePosition();
        Vec3 end = start.add(entity.getLookAngle().normalize().scale(reach));
        PortalRay.Trace trace = PortalRay.trace(level, start, end, fluid, entity);
        if (trace == null) return original.call(level, entity, fluid, reach);

        PortalRay.BLINK.set(trace);
        return trace.hit();
    }

    @WrapOperation(method = "findTeleportLocation", remap = false, at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/Level;clip(Lnet/minecraft/world/level/ClipContext;)Lnet/minecraft/world/phys/BlockHitResult;", remap = true))
    private static BlockHitResult aacore$clipInTargetLevel(Level level, ClipContext context, Operation<BlockHitResult> original) {
        PortalRay.Trace trace = PortalRay.BLINK.get();
        return original.call(trace != null ? trace.level() : level, context);
    }

    @WrapOperation(method = "findTeleportLocation", remap = false, at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/Level;getBlockState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;", remap = true))
    private static BlockState aacore$stateInTargetLevel(Level level, BlockPos pos, Operation<BlockState> original) {
        PortalRay.Trace trace = PortalRay.BLINK.get();
        return original.call(trace != null ? trace.level() : level, pos);
    }

    // The landing spot is on the far side: remember it so the spell's own teleportTo(...) crosses over.
    // Teleport, Frost Step, Thunder Step and Blood Step all use this method,
    // each with its own teleport call (see MixinEntityTeleportTo / MixinServerPlayerTeleportTo).
    @Inject(method = "findTeleportLocation", remap = false, at = @At("RETURN"))
    private static void aacore$rememberFarLanding(Level level, LivingEntity entity, float maxDistance, CallbackInfoReturnable<Vec3> cir) {
        PortalRay.Trace trace = PortalRay.BLINK.get();
        PortalRay.BLINK.remove();
        if (trace != null) PortalRay.beginPendingTeleport(trace, entity, cir.getReturnValue());
    }
}