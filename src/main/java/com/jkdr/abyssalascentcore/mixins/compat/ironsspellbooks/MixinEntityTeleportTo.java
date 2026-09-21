package com.jkdr.abyssalascentcore.mixins.compat.ironsspellbooks;

import com.jkdr.abyssalascentcore.spell.PortalRay;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Lets a spell that found its landing spot through a portal teleport across dimensions. See {@link PortalRay}. */
@Mixin(Entity.class)
public abstract class MixinEntityTeleportTo {

    @Inject(method = "teleportTo(DDD)V", at = @At("HEAD"), cancellable = true)
    private void aacore$crossPortalTeleport(double x, double y, double z, CallbackInfo ci) {
        if (PortalRay.crossTeleportIfPending((Entity) (Object) this, x, y, z)) ci.cancel();
    }
}