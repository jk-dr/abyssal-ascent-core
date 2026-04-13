//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.jkdr.abyssalascentdimensionpatcher.mixins.compat.distantHorizons;

import loaderCommon.forge.com.seibel.distanthorizons.common.wrappers.world.ClientLevelWrapper;
import net.minecraft.client.multiplayer.ClientLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(
    value = {ClientLevelWrapper.class},
    remap = false
)
public interface IMixinClientLevelWrapper {
    @Accessor("level")
    ClientLevel getLevel();
}
