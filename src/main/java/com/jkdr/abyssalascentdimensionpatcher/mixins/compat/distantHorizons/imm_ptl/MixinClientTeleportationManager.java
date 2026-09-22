//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.jkdr.abyssalascentdimensionpatcher.mixins.compat.distantHorizons.imm_ptl;

import com.jkdr.abyssalascentdimensionpatcher.AbyssalAscentDimensionPatcher;
import com.jkdr.abyssalascentdimensionpatcher.util.RefreshDH;
import com.mojang.logging.LogUtils;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import qouteall.imm_ptl.core.teleportation.ClientTeleportationManager;

@Mixin(
    value = {ClientTeleportationManager.class},
    remap = false
)
public abstract class MixinClientTeleportationManager {
    private static final Logger LOGGER = LogUtils.getLogger();

    @Inject(
        method = {"changePlayerDimension"},
        at = {@At("TAIL")}
    )
    private static void afterChangeDimensions(LocalPlayer player, ClientLevel fromWorld, ClientLevel toWorld, Vec3 newEyePos, CallbackInfo ci) {
        if (AbyssalAscentDimensionPatcher.getIsDistantHorizonsInstalled()) {
            RefreshDH.refreshDh(fromWorld, toWorld);
        }

    }
}
