package com.jkdr.abyssalascentcore.mixins.compat.immptl;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import qouteall.imm_ptl.core.collision.CollisionHelper;
import qouteall.imm_ptl.core.ducks.IEEntity;
import qouteall.imm_ptl.core.portal.Portal;
import qouteall.imm_ptl.core.portal.global_portals.GlobalPortalStorage;

/**
 * Immersive Portals only registers a portal as "colliding" after the tick, from the previous tick's velocity.
 * A jump applies its impulse inside the tick, right before {@code move},
 * So the head can cross a dimension-stack portal plane in that one move while the blocks on the other side are still ignored,
 * Letting the player jump into (and then walk around inside) solid blocks across the portal.
 * <p>
 * This registers the global portals this move is about to reach, so IP's cross-portal collision is active for it.
 */
@Mixin(Entity.class)
public abstract class MixinEntityPredictivePortalCollision {

    @Inject(method = "move", at = @At("HEAD"))
    private void aacore$registerPortalsForThisMove(MoverType type, Vec3 movement, CallbackInfo ci) {
        if (!((Object) this instanceof LocalPlayer player) || movement.lengthSqr() < 1.0E-7) return;

        AABB swept = player.getBoundingBox().expandTowards(movement);
        for (Portal portal : GlobalPortalStorage.getGlobalPortals(player.level())) {
            if (swept.intersects(portal.getBoundingBox()) && CollisionHelper.canCollideWithPortal(player, portal, 0.0F)) {
                ((IEEntity) player).ip_notifyCollidingWithPortal(portal);
            }
        }
    }
}