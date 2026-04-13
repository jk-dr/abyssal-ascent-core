//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.jkdr.abyssalascentdimensionpatcher.mixins.compat.distantHorizons;

import com.mojang.logging.LogUtils;
import com.seibel.distanthorizons.core.api.internal.ClientApi;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import qouteall.imm_ptl.core.render.context_management.PortalRendering;

@Pseudo
@Mixin(
    value = {ClientApi.class},
    remap = false
)
public class MixinClientApi {
    private static final Logger LOGGER = LogUtils.getLogger();

    @Inject(
        method = {"renderLods"},
        at = {@At("HEAD")},
        cancellable = true
    )
    private void onRenderLods(CallbackInfo ci) {
        if (this.shouldCancelDhRendering()) {
            ci.cancel();
        }

    }

    @Inject(
        method = {"renderDeferredLodsForShaders"},
        at = {@At("HEAD")},
        cancellable = true
    )
    private void onRenderDeferredLods(CallbackInfo ci) {
        if (this.shouldCancelDhRendering()) {
            ci.cancel();
        }

    }

    @Inject(
        method = {"renderFadeOpaque"},
        at = {@At("HEAD")},
        cancellable = true
    )
    private void onRenderFadeOpaque(CallbackInfo ci) {
        if (this.shouldCancelDhRendering()) {
            ci.cancel();
        }

    }

    @Inject(
        method = {"renderFadeTransparent"},
        at = {@At("HEAD")},
        cancellable = true
    )
    private void onRenderFadeTransparent(CallbackInfo ci) {
        if (this.shouldCancelDhRendering()) {
            ci.cancel();
        }

    }

    @Unique
    private boolean shouldCancelDhRendering() {

        if (PortalRendering.isRendering()) {

            ClientLevel currentClientLevel = Minecraft.getInstance().level;

            Object dhWrapper = ClientApi.RENDER_STATE.clientLevelWrapper;

            if (currentClientLevel == null || dhWrapper == null) {
                return false;
            }

            ClientLevel dhLevel = ((IMixinClientLevelWrapper) dhWrapper).getLevel();

            if (currentClientLevel != dhLevel) {
                return true;
            }
        }

        return false;
    }
}
