package com.jkdr.abyssalascentcore.mixins.compat.oculus.immptl;

import com.mojang.blaze3d.vertex.PoseStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
import qouteall.imm_ptl.core.compat.iris_compatibility.ExperimentalIrisPortalRenderer;

@Mixin(
    value = {ExperimentalIrisPortalRenderer.class},
    remap = false
)
public interface MixinIExpIrisPortalRenderer {
    @Invoker("doPortalRendering")
    void invokeDoPortalRendering(PoseStack var1);
}
