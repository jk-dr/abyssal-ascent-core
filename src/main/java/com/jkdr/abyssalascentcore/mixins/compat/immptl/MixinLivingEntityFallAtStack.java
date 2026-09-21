package com.jkdr.abyssalascentcore.mixins.compat.immptl;

import com.jkdr.abyssalascentcore.portal.CrossPortal;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * The client half of {@link MixinServerPlayerFallAtStack}. While a fast fall is held at a stack portal waiting for the far side to load,
 * The player's own game believes it has landed: it plays the landing sound and particles.
 * Nothing happens to the player on the server, so here the landing is skipped as well.
 */
@Mixin(LivingEntity.class)
public abstract class MixinLivingEntityFallAtStack {

    @Inject(method = "causeFallDamage", at = @At("HEAD"), cancellable = true)
    private void aacore$noLandingWhileFarSideLoads(float distance, float multiplier, DamageSource source, CallbackInfoReturnable<Boolean> cir) {
        if ((Object) this instanceof Player player && player.level().isClientSide && CrossPortal.waitingForFarSide(player)) {
            cir.setReturnValue(false);
        }
    }
}
