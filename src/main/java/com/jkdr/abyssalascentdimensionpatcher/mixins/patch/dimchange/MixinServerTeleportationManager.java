//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.jkdr.abyssalascentdimensionpatcher.mixins.patch.dimchange;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import qouteall.imm_ptl.core.teleportation.ServerTeleportationManager;

@Mixin(
    value = {ServerTeleportationManager.class},
    remap = false
)
public abstract class MixinServerTeleportationManager {
    @Inject(
        method = {"changePlayerDimension"},
        at = {@At("TAIL")}
    )
    private void afterChangeDimensions(ServerPlayer player, ServerLevel fromWorld, ServerLevel toWorld, Vec3 newEyePos, CallbackInfo ci) {
        player.invalidateCaps();
        player.reviveCaps();
        PlayerEvent.PlayerChangedDimensionEvent event = new PlayerEvent.PlayerChangedDimensionEvent(player, fromWorld.dimension(), toWorld.dimension());
        MinecraftForge.EVENT_BUS.post(event);
    }
}
