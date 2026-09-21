package com.jkdr.abyssalascentcore.mixins.compat.ironsspellbooks;

import com.jkdr.abyssalascentcore.spell.PortalRay;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** ServerPlayer overrides {@code teleportTo(x, y, z)}, so it needs the same hook as Entity. */
@Mixin(ServerPlayer.class)
public abstract class MixinServerPlayerTeleportTo {

    @Inject(method = "teleportTo(DDD)V", at = @At("HEAD"), cancellable = true)
    private void aacore$crossPortalTeleport(double x, double y, double z, CallbackInfo ci) {
        if (PortalRay.crossTeleportIfPending((ServerPlayer) (Object) this, x, y, z)) ci.cancel();
    }
}