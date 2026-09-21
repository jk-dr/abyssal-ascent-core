package com.jkdr.abyssalascentcore.mixins.compat.oculus.immptl;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
import qouteall.imm_ptl.core.render.PortalRenderer;
import qouteall.imm_ptl.core.render.context_management.WorldRenderInfo;

@Mixin(
    value = {PortalRenderer.class},
    remap = false
)
public interface MixinPortalRenderer {
    @Invoker("invokeWorldRendering")
    void callSuperInvokeWorldRendering(WorldRenderInfo var1);
}
