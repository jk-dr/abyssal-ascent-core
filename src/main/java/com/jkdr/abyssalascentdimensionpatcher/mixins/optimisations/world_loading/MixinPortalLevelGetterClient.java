package com.jkdr.abyssalascentdimensionpatcher.mixins.optimisations.world_loading;

import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import qouteall.imm_ptl.core.portal.Portal;

@Mixin(value = {Portal.class}, remap = false)
public abstract class MixinPortalLevelGetterClient {

    /**
     * Overrides the destination world resolution.
     * Ensures that the returned Level is the authoritative one from the server's level map.
     */
    @Inject(
        method = "getDestinationWorld(Z)Lnet/minecraft/world/level/Level;",
        at = @At("HEAD"),
        cancellable = true
    )
    private void onGetDestinationWorld(boolean isClient, CallbackInfoReturnable<Level> cir) {
        Portal self = (Portal) (Object) this;

        try {
            ResourceKey<Level> destKey = self.dimensionTo;
            Level originLevel = self.level(); // m_9236_
            MinecraftServer server = null;

            if (originLevel instanceof ServerLevel serverLevel) {
                server = serverLevel.getServer(); // m_7654_
            }

            if (server != null && destKey != null) {
                Level serverResolved = server.getLevel(destKey); // m_129880_
                
                // Safety check: Ensure the resolved level matches the one registered on the server
                if (serverResolved != null && serverResolved == server.getLevel(serverResolved.dimension())) {
                    cir.setReturnValue(serverResolved);
                }
            }
        } catch (Throwable ignored) {
            // Silently fail to let the original method handle the logic if an error occurs
        }
    }

    /**
     * Overrides the origin world resolution.
     * Prevents mismatched Level instances by verifying the origin against the Server's active levels.
     */
    @Inject(
        method = "getOriginWorld()Lnet/minecraft/world/level/Level;",
        at = @At("HEAD"),
        cancellable = true
    )
    private void onGetOriginWorld(CallbackInfoReturnable<Level> cir) {
        Portal self = (Portal) (Object) this;

        try {
            Level origin = self.level(); // m_9236_
            if (origin instanceof ServerLevel sl) {
                MinecraftServer server = sl.getServer(); // m_7654_
                if (server != null) {
                    Level serverOrigin = server.getLevel(origin.dimension()); // m_129880_ / m_46472_
                    
                    // If the current instance is the same as the server's registered instance, return it.
                    if (origin == serverOrigin) {
                        cir.setReturnValue(origin);
                    }
                }
            }
        } catch (Throwable ignored) {
        }
    }
}