package com.jkdr.abyssalascentcore.mixins.optimisations.entity.alexsmobs;

import com.github.alexthe666.alexsmobs.entity.EntityWarpedToad;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityWarpedToad.class)
public abstract class MixinEntityWarpedToad {

    @Invoker("travel")
    protected abstract void callSuperTravel(Vec3 travelVector);

    @Inject(
        method = "travel",
        at = @At("HEAD"),
        cancellable = true
    )
    private void replaceTravel(Vec3 travelVector, CallbackInfo ci) {

        EntityWarpedToad self = (EntityWarpedToad) (Object) this;

        PathNavigation navigation = self.getNavigation();

        // Sitting / immobile state
        if (self.isOrderedToSit()) {

            if (navigation.getPath() != null) {
                navigation.stop();
            }

            self.setDeltaMovement(Vec3.ZERO);

            ci.cancel();
            return;
        }

        boolean inFluid = self.isInWater() || self.isInLava();

        // Swimming movement logic
        if (self.isEffectiveAi() && inFluid) {

            self.moveRelative(self.getSpeed(), travelVector);

            Vec3 preMoveDelta = self.getDeltaMovement();

            self.move(MoverType.SELF, preMoveDelta);

            Vec3 newDelta = preMoveDelta.scale(0.9D);

            if (self.getTarget() == null) {
                newDelta = newDelta.add(0.0D, -0.005D, 0.0D);
            }

            self.setDeltaMovement(newDelta);

            ci.cancel();
        }
    }
}