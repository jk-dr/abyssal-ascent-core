//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.jkdr.abyssalascentdimensionpatcher.mixins.compat.distantHorizons;

import com.seibel.distanthorizons.core.api.internal.ClientPluginChannelApi;
import com.seibel.distanthorizons.core.level.IServerKeyedClientLevel;
import com.seibel.distanthorizons.core.wrapperInterfaces.world.IClientLevelWrapper;
import java.util.function.Consumer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(
    value = {ClientPluginChannelApi.class},
    remap = false
)
public interface IMixinClientPluginChannelApi {
    @Accessor("levelLoadHandler")
    Consumer<IServerKeyedClientLevel> getUnloadConsumer();

    @Accessor("levelUnloadHandler")
    Consumer<IClientLevelWrapper> getLoadConsumer();
}
