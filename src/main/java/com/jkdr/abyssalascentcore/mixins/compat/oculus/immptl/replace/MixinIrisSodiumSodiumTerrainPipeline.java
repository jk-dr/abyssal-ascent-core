package com.jkdr.abyssalascentcore.mixins.compat.oculus.immptl.replace;

import com.mojang.blaze3d.shaders.Program.Type;
import java.util.Optional;
import net.irisshaders.iris.pipeline.SodiumTerrainPipeline;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import qouteall.imm_ptl.core.render.ShaderCodeTransformation;
import qouteall.q_misc_util.Helper;

@Pseudo
@Mixin(
   value = {SodiumTerrainPipeline.class},
   remap = false
)
public class MixinIrisSodiumSodiumTerrainPipeline {
   @Shadow
   Optional<String> terrainSolidVertex;
   @Shadow
   Optional<String> terrainCutoutVertex;
   @Shadow
   Optional<String> translucentVertex;
   @Unique
   private boolean immptlPatched = false;

   @Inject(
      method = {"patchShaders"},
      at = {@At("RETURN")}
   )
   private void onPatchShaderEnds(@Coerce Object chunkVertexType, CallbackInfo ci) {
      if (!this.immptlPatched) {
         this.immptlPatched = true;
         this.terrainSolidVertex = this.terrainSolidVertex.map((code) -> ShaderCodeTransformation.transform(Type.VERTEX, "iris_sodium_terrain_vertex", code));
         this.terrainCutoutVertex = this.terrainCutoutVertex.map((code) -> ShaderCodeTransformation.transform(Type.VERTEX, "iris_sodium_terrain_vertex", code));
         this.translucentVertex = this.translucentVertex.map((code) -> ShaderCodeTransformation.transform(Type.VERTEX, "iris_sodium_terrain_vertex", code));
      } else {
         Helper.err("iris terrain shader ImmPtl patched twice");
      }

   }
}
