package com.jkdr.abyssalascentcore.mixins.compat.immptl;

import com.jkdr.abyssalascentcore.portal.CrossPortal;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * A player crawling under the ceiling of a dimension with another dimension above it: vanilla only looks for blocks above them in their own level.
 * Then finds room to stand, and stands up into blocks of the dimension above.
 * Immersive Portals' collision then pushes them back down, and the pose flips back and forth every tick.
 * A pose is now also refused when its taller box would be inside blocks on the other side of a portal.
 */
@Mixin(Entity.class)
public abstract class MixinEntityCanEnterPose {

    @Shadow
    protected abstract AABB getBoundingBoxForPose(Pose pose);

    @Inject(method = "canEnterPose", at = @At("RETURN"), cancellable = true)
    private void aacore$notIntoBlocksAcrossPortal(Pose pose, CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValueZ() || !((Object) this instanceof Player player)) return;

        AABB box = getBoundingBoxForPose(pose).deflate(1.0E-7);
        if (CrossPortal.boxBlockedAcrossPortal(player, box)) cir.setReturnValue(false);
    }
}
