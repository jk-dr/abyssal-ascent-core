package com.jkdr.abyssalascentcore.mixins.compat.immptl;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import qouteall.imm_ptl.core.IPGlobal;
import qouteall.imm_ptl.core.compat.iris_compatibility.IrisInterface;
import qouteall.imm_ptl.core.render.CrossPortalEntityRenderer;

/**
 * An entity straddling a portal (an aircraft half through the plane of a dimension stack) is drawn twice,
 * once on each side, each clipped to its own side, so it looks whole. Immersive Portals turns that off whenever Iris (Oculus) is installed, even with no shader pack selected, so the part of the entity beyond the plane is simply cut off.
 * <p>
 * It only has to stay off while a shader pack is actually running, because a shader pack's own shaders do not know how to clip.
 * With no shader pack the normal renderer is used and the clipping works, so it is turned back on then.
 */
@Mixin(value = CrossPortalEntityRenderer.class, remap = false)
public abstract class MixinCrossPortalEntityRendering {

    @Inject(method = "isCrossPortalRenderingEnabled", at = @At("RETURN"), cancellable = true, remap = false)
    private static void aacore$allowWithoutShaderPack(CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValueZ() && IPGlobal.correctCrossPortalEntityRendering && !IrisInterface.invoker.isShaders()) {
            cir.setReturnValue(true);
        }
    }
}
