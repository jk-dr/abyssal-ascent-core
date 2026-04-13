//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.jkdr.abyssalascentdimensionpatcher.mixins.compat.distantHorizons;

import com.mojang.logging.LogUtils;
import com.seibel.distanthorizons.core.multiplayer.fullData.FullDataPayload;
import com.seibel.distanthorizons.core.multiplayer.fullData.FullDataPayloadReceiver;
import com.seibel.distanthorizons.core.sql.dto.FullDataSourceV2DTO;
import io.netty.buffer.CompositeByteBuf;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(
    value = {FullDataPayloadReceiver.class},
    remap = false
)
public class MixinFullDataPayloadReceiver {
    private static final Logger LOGGER = LogUtils.getLogger();

    @Inject(
        method = {"decodeDataSource"},
        at = {@At(
    value = "INVOKE",
    target = "Lcom/seibel/distanthorizons/core/util/LodUtil;assertTrue(Z)V"
)},
        cancellable = true,
        locals = LocalCapture.CAPTURE_FAILHARD
    )
    private void onDecodeDataSourceCheck(FullDataPayload payload, CallbackInfoReturnable<FullDataSourceV2DTO> cir, CompositeByteBuf compositeByteBuffer) {
        if (compositeByteBuffer == null) {
            LOGGER.warn("Distant Horizons LOD Assertion Failed: Payload was null (this happens often in Abyssal Ascent and has no side effects)");
            cir.setReturnValue(null);
        }

    }
}
