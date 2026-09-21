package com.jkdr.abyssalascentcore.mixins.compat.oculus.immptl.replace;

import net.irisshaders.iris.pipeline.IrisRenderingPipeline;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import qouteall.imm_ptl.core.IPCGlobal;
import qouteall.imm_ptl.core.compat.iris_compatibility.ExperimentalIrisPortalRenderer;
import qouteall.imm_ptl.core.compat.iris_compatibility.IEIrisNewWorldRenderingPipeline;
import qouteall.imm_ptl.core.render.PortalRenderer;
import qouteall.imm_ptl.core.render.context_management.PortalRendering;

@Mixin(
   value = {IrisRenderingPipeline.class},
   remap = false
)
public class MixinIrisRenderingPipeline implements IEIrisNewWorldRenderingPipeline {
   @Shadow
   private boolean isRenderingWorld;

   @Inject(
      method = {"finalizeLevelRendering"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onFinalizeLevelRendering(CallbackInfo ci) {
      if (IPCGlobal.renderer instanceof ExperimentalIrisPortalRenderer && PortalRendering.isRendering()) {
         ci.cancel();
      }

   }

   @Inject(
      method = {"beginTranslucents"},
      at = {@At(
   value = "INVOKE",
   target = "Lnet/irisshaders/iris/pipeline/CompositeRenderer;renderAll()V",
   shift = Shift.AFTER
)}
   )
   private void onAfterDeferredCompositeRendering(CallbackInfo ci) {
      PortalRenderer var3 = IPCGlobal.renderer;
      if (var3 instanceof ExperimentalIrisPortalRenderer r) {
         r.onAfterIrisDeferredCompositeRendering();
      }

   }

   public void ip_setIsRenderingWorld(boolean cond) {
      this.isRenderingWorld = cond;
   }
}
