//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.jkdr.abyssalascentdimensionpatcher.mixins.compat.distantHorizons;

import com.seibel.distanthorizons.core.api.internal.ClientApi;
import com.seibel.distanthorizons.core.api.internal.ClientPluginChannelApi;
import com.seibel.distanthorizons.core.wrapperInterfaces.world.IClientLevelWrapper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(
    value = {ClientApi.class},
    remap = false
)
public interface IMixinClientApi {
    @Accessor("pluginChannelApi")
    ClientPluginChannelApi getPluginChannelApi();

    @Invoker("loadWaitingChunksForLevel")
    void invokeLoadWaitingChunksForLevel(IClientLevelWrapper var1);
}
