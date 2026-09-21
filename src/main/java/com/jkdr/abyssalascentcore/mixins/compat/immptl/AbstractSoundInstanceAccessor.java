package com.jkdr.abyssalascentcore.mixins.compat.immptl;

import net.minecraft.client.resources.sounds.AbstractSoundInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** Lets a sound that has not been played yet be moved. */
@Mixin(AbstractSoundInstance.class)
public interface AbstractSoundInstanceAccessor {
    @Accessor("x")
    void aacore$setX(double x);

    @Accessor("y")
    void aacore$setY(double y);

    @Accessor("z")
    void aacore$setZ(double z);
}