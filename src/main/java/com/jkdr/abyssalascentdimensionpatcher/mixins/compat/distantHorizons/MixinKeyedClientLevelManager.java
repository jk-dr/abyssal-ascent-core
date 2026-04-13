//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.jkdr.abyssalascentdimensionpatcher.mixins.compat.distantHorizons;

import com.jkdr.abyssalascentdimensionpatcher.interfaces.IKeyedClientLevelManagerAccess;
import com.jkdr.abyssalascentdimensionpatcher.util.DistantHorizonsCaching;
import com.mojang.logging.LogUtils;
import com.seibel.distanthorizons.core.level.IServerKeyedClientLevel;
import com.seibel.distanthorizons.core.wrapperInterfaces.world.IClientLevelWrapper;
import java.util.HashMap;
import loaderCommon.forge.com.seibel.distanthorizons.common.wrappers.level.KeyedClientLevelManager;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(
    value = {KeyedClientLevelManager.class},
    remap = false
)
public abstract class MixinKeyedClientLevelManager implements IKeyedClientLevelManagerAccess {
    private static final Logger LOGGER = LogUtils.getLogger();
    @Shadow
    private @Nullable IServerKeyedClientLevel serverKeyedLevel;

    public void setKeyedLevelForImmersivePortalsSupportPlease(IServerKeyedClientLevel level) {
        this.serverKeyedLevel = level;
    }

    @Inject(
        method = {"setServerKeyedLevel(Lcom/seibel/distanthorizons/core/wrapperInterfaces/world/IClientLevelWrapper;Ljava/lang/String;)Lcom/seibel/distanthorizons/core/level/IServerKeyedClientLevel;"},
        at = {@At("RETURN")},
        remap = false
    )
    private void onSetServerKeyedLevel(IClientLevelWrapper wrapper, String keyA, CallbackInfoReturnable<IServerKeyedClientLevel> cir) {
        ClientLevel level = ((IMixinClientLevelWrapper)wrapper).getLevel();
        ResourceKey<Level> dimensionKey = level.dimension();
        ResourceLocation identifier = dimensionKey.location();
        DistantHorizonsCaching.localDHKeyCache = new HashMap();
        DistantHorizonsCaching.localDHKeyCache.put(identifier.toString(), (IServerKeyedClientLevel)cir.getReturnValue());
    }
}
