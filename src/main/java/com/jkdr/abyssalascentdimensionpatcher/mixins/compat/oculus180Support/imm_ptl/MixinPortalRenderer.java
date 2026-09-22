//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.jkdr.abyssalascentdimensionpatcher.mixins.compat.oculus180Support.imm_ptl;

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
