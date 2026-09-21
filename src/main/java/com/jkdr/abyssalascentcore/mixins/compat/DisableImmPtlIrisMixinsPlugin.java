package com.jkdr.abyssalascentcore.mixins.compat;

import java.util.List;
import java.util.Set;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

public class DisableImmPtlIrisMixinsPlugin implements IMixinConfigPlugin {
    private static final Set<String> DISABLED = Set.of("qouteall.imm_ptl.core.compat.mixin.iris.MixinIrisClearPass", "qouteall.imm_ptl.core.compat.mixin.iris.MixinIrisFinalPassRenderer", "qouteall.imm_ptl.core.compat.mixin.iris.MixinIrisIris", "qouteall.imm_ptl.core.compat.mixin.iris.MixinIrisNewWorldRenderingPipeline", "qouteall.imm_ptl.core.compat.mixin.iris.MixinIrisShadowRenderTargets", "qouteall.imm_ptl.core.compat.mixin.iris.MixinIrisSodiumChunkShaderInterface", "qouteall.imm_ptl.core.compat.mixin.iris.MixinIrisSodiumSodiumTerrainPipeline");
    public void onLoad(String mixinPackage) {
    }

    public String getRefMapperConfig() {
        return null;
    }

    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        return !DISABLED.contains(mixinClassName);
    }

    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
    }

    public List<String> getMixins() {
        return null;
    }

    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }

    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }
}
