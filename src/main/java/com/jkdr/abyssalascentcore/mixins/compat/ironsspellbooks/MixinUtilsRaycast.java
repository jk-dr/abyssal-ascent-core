package com.jkdr.abyssalascentcore.mixins.compat.ironsspellbooks;

import com.jkdr.abyssalascentcore.spell.PortalRay;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.redspace.ironsspellbooks.api.util.Utils;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Iron's block/entity raycast helpers are used by nearly every spell, including add-ons such as Travel Optics.
 * A ray that leaves through a dimension stack portal used to end in the void beyond the world edge,
 * so spells targeted empty space there. It now stops at the portal plane, like a ceiling.
 * <p>
 * Spells that spawn effects cannot safely act in the far dimension (owner lookups and damage attribution are per
 * level), so only Blink and Portal, which relocate things, cross over (see the other mixins in this package).
 */
@Mixin(Utils.class)
public abstract class MixinUtilsRaycast {

    @WrapOperation(method = {"getTargetBlock", "raycastForBlock", "internalRaycastForEntity"}, remap = false, at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/Level;clip(Lnet/minecraft/world/level/ClipContext;)Lnet/minecraft/world/phys/BlockHitResult;", remap = true))
    private static BlockHitResult aacore$stopAtPortalPlane(Level level, ClipContext context, Operation<BlockHitResult> original) {
        return PortalRay.clampToPortalPlane(level, context, original.call(level, context));
    }
}