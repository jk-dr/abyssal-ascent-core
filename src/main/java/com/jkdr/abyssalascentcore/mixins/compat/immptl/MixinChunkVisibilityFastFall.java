package com.jkdr.abyssalascentcore.mixins.compat.immptl;

import com.jkdr.abyssalascentcore.config.StackPreloadConfig;
import com.jkdr.abyssalascentcore.events.StackPreloadEvents;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import qouteall.imm_ptl.core.chunk_loading.ChunkVisibility;

/**
 * For a global portal such as a dimension stack portal, Immersive Portals loads the far side around the player with a radius of (server view distance - distance to the portal in chunks),
 * Never below 2. Someone falling fast covers that distance faster than the far chunks can be generated, sent and built, so the far dimension appears late, and in patches.
 * While the player is falling or rising fast the smallest radius is raised, so the ring around where they will arrive is already there. At any other time the normal radius is used and nothing extra is loaded.
 */
@Mixin(value = ChunkVisibility.class, remap = false)
public abstract class MixinChunkVisibilityFastFall {

    @WrapOperation(method = "getGeneralDirectPortalLoader", remap = false, at = @At(value = "INVOKE", remap = false,
            target = "Ljava/lang/Math;max(II)I"))
    private static int aacore$widerWhileFalling(int minimum, int wanted, Operation<Integer> original,
                                                @Local(argsOnly = true) ServerPlayer player) {
        int radius = original.call(minimum, wanted);
        return StackPreloadEvents.isFastVertical(player) ? Math.max(radius, StackPreloadConfig.fastFallMinRadius()) : radius;
    }
}
