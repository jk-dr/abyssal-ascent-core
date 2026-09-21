package com.jkdr.abyssalascentcore.mixins.compat.immptl;

import com.jkdr.abyssalascentcore.portal.CrossPortal;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * While sneaking, vanilla stops a player walking off an edge by checking for ground below in the player's own level.
 * Standing on a block that is in the other dimension of a dimension stack, \
 * That check finds nothing and blocks all sneaking movement. The check now also looks through the portals the player is touching.
 */
@Mixin(Player.class)
public abstract class MixinPlayerSneakEdge {

    @WrapOperation(method = {"maybeBackOffFromEdge", "isAboveGround"}, at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/Level;noCollision(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/phys/AABB;)Z"))
    private boolean aacore$lookThroughPortals(Level level, Entity entity, AABB box, Operation<Boolean> original) {
        return original.call(level, entity, box) && CrossPortal.farSideFree(entity, box);
    }
}