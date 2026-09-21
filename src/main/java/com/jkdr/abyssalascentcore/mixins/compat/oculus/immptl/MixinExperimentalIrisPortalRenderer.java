package com.jkdr.abyssalascentcore.mixins.compat.oculus.immptl;

import com.jkdr.abyssalascentcore.compat.OculusCompatLayer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.pipeline.ShaderRenderingPipeline;
import net.irisshaders.iris.pipeline.WorldRenderingPhase;
import net.irisshaders.iris.pipeline.WorldRenderingPipeline;
import net.irisshaders.iris.uniforms.SystemTimeUniforms;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import qouteall.imm_ptl.core.compat.iris_compatibility.ExperimentalIrisPortalRenderer;
import qouteall.imm_ptl.core.compat.iris_compatibility.IEIrisNewWorldRenderingPipeline;
import qouteall.imm_ptl.core.render.PortalRenderer;
import qouteall.imm_ptl.core.render.context_management.WorldRenderInfo;

@Mixin(value = {ExperimentalIrisPortalRenderer.class}, remap = false)
public abstract class MixinExperimentalIrisPortalRenderer {
    private static final Logger LOGGER = LoggerFactory.getLogger("AbyssalAscentPatcher");

    /**
     * Intercepts the start of the translucent rendering phase (glass, water, portals).
     */
    @Inject(
        method = "onBeginIrisTranslucentRendering",
        at = @At("HEAD"),
        cancellable = true
    )
    private void replaceOnBeginIrisTranslucentRendering(PoseStack matrixStack, CallbackInfo ci) {
        if (OculusCompatLayer.hasNewIrisPackage()) {
            // Force Minecraft to finish drawing any pending geometry (flushing the buffer)
            // m_91269_() -> renderBuffers() | m_110104_() -> bufferSource() | m_109911_() -> endBatch()
            PortalRenderer.client.renderBuffers().bufferSource().endBatch();
            
            // Invoke the actual portal rendering logic
            MixinIExpIrisPortalRenderer invoker = (MixinIExpIrisPortalRenderer) (Object) this;
            invoker.invokeDoPortalRendering(matrixStack);
            
            // Notify the Iris shader pipeline that it is currently drawing a world
            ((IEIrisNewWorldRenderingPipeline) Iris.getPipelineManager().getPipeline().get()).ip_setIsRenderingWorld(true);
        }
    }

    /**
     * Wraps the actual world rendering call to manage shader states and timers.
     */
    @Inject(
        method = "invokeWorldRendering",
        at = @At("HEAD"),
        cancellable = true
    )
    private void onInvokeWorldRendering(WorldRenderInfo worldRenderInfo, CallbackInfo ci) {
        if (OculusCompatLayer.hasNewIrisPackage()) {
            WorldRenderingPipeline pipeline = (WorldRenderingPipeline) Iris.getPipelineManager().getPipeline().get();
            MixinPortalRenderer invoker = (MixinPortalRenderer) (Object) this;
            
            // Reset the shader animation timer so animations in the portal sync properly
            SystemTimeUniforms.COUNTER.beginFrame();
            
            // Draw the actual portal world
            invoker.callSuperInvokeWorldRendering(worldRenderInfo);
            
            // Reset the timer again for the main world
            SystemTimeUniforms.COUNTER.beginFrame();
            
            // Reset the shader pipeline phase back to solid terrain to prevent visual corruption
            // after returning from the portal render
            if (pipeline instanceof ShaderRenderingPipeline newWorldRenderingPipeline) {
                newWorldRenderingPipeline.setPhase(WorldRenderingPhase.TERRAIN_SOLID);
            }

            // Tell Iris we are done rendering the portal world
            ((IEIrisNewWorldRenderingPipeline) pipeline).ip_setIsRenderingWorld(false);
            
            // Cancel the original method so our wrapped version takes full control
            ci.cancel();
        }
    }
}
