package com.jkdr.abyssalascentcore.mixins.compat.immptl;

import com.jkdr.abyssalascentcore.portal.CrossPortal;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * A player falling fast into a dimension stack portal whose far side has not loaded is held at the plane by Immersive Portals,
 * which vanilla mistakes for landing. The server would then deal fall damage for a fall that has not ended.
 * While that is the case the fall damage check is skipped, so the distance fallen so far is kept for the real landing.
 */
@Mixin(ServerPlayer.class)
public abstract class MixinServerPlayerFallAtStack {

    @Inject(method = "doCheckFallDamage", at = @At("HEAD"), cancellable = true)
    private void aacore$noLandingWhileFarSideLoads(double x, double y, double z, boolean onGround, CallbackInfo ci) {
        if (onGround && CrossPortal.waitingForFarSide((ServerPlayer) (Object) this)) ci.cancel();
    }
}
