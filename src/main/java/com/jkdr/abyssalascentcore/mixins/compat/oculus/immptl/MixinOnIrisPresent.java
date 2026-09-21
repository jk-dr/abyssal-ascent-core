package com.jkdr.abyssalascentcore.mixins.compat.oculus.immptl;

import com.jkdr.abyssalascentcore.compat.OculusCompatLayer;
import java.lang.reflect.Field;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.pipeline.WorldRenderingPipeline;
import net.irisshaders.iris.shadows.ShadowRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import qouteall.imm_ptl.core.compat.iris_compatibility.IrisInterface;
import qouteall.q_misc_util.Helper;

@Mixin(
    value = {IrisInterface.OnIrisPresent.class},
    remap = false
)
public abstract class MixinOnIrisPresent extends IrisInterface.Invoker {
    @Shadow
    private Field worldRendererPipelineField;

    @Inject(
        method = {"getPipeline"},
        at = {@At("HEAD")},
        cancellable = true
    )
    private void replaceGetPipeline(LevelRenderer renderer, CallbackInfoReturnable<Object> cir) {
        if (OculusCompatLayer.hasNewIrisPackage()) {
            cir.setReturnValue(Helper.noError(() -> (WorldRenderingPipeline)this.worldRendererPipelineField.get(renderer)));
        }
    }

    @Inject(
        method = {"setPipeline"},
        at = {@At("HEAD")},
        cancellable = true
    )
    private void replaceSetPipeline(LevelRenderer worldRenderer, Object pipeline, CallbackInfo ci) {
        if (OculusCompatLayer.hasNewIrisPackage()) {
            Helper.noError(() -> {
                this.worldRendererPipelineField.set(worldRenderer, pipeline);
                return null;
            });
            ci.cancel();
        }
    }

    @Inject(
        method = {"reloadPipelines"},
        at = {@At("HEAD")},
        cancellable = true
    )
    private void onReloadPipelines(CallbackInfo ci) {
        if (OculusCompatLayer.hasNewIrisPackage()) {
            Iris.getPipelineManager().destroyPipeline();
            ci.cancel();
        }
    }

    @Inject(
        method = {"isShaders"},
        at = {@At("HEAD")},
        cancellable = true
    )
    private void onIsShaders(CallbackInfoReturnable<Boolean> cir) {
        if (OculusCompatLayer.hasNewIrisPackage()) {
            cir.setReturnValue(Iris.getCurrentPack().isPresent());
        }
    }

    @Inject(
        method = {"isRenderingShadowMap"},
        at = {@At("HEAD")},
        cancellable = true
    )
    private void onIsRenderingShadowMap(CallbackInfoReturnable<Boolean> cir) {
        if (OculusCompatLayer.hasNewIrisPackage()) {
            cir.setReturnValue(ShadowRenderer.ACTIVE);
        }
    }

    @Inject(
        method = {"getShaderpackName"},
        at = {@At("HEAD")},
        cancellable = true
    )
    @Nullable
    private void onGetShaderpackName(CallbackInfoReturnable<String> cir) {
        if (OculusCompatLayer.hasNewIrisPackage()) {
            cir.setReturnValue(Iris.getCurrentPackName());
        }
    }
}
