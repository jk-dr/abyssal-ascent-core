package com.jkdr.abyssalascentcore.mixins.compat.immptl;

import com.jkdr.abyssalascentcore.client.PortalSounds;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import qouteall.imm_ptl.core.ClientWorldLoader;

/**
 * Mining a block in another dimension (through a portal) plays its "hit" sound at the block's far-dimension position,
 * which is silent for the player. The sound is moved to where that block appears in the player's own dimension.
 */
@Mixin(MultiPlayerGameMode.class)
public abstract class MixinMultiPlayerGameModeMiningSound {

    @WrapOperation(method = "continueDestroyBlock", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/sounds/SoundManager;play(Lnet/minecraft/client/resources/sounds/SoundInstance;)V"))
    private void aacore$playHitSoundInOwnDimension(SoundManager soundManager, SoundInstance sound, Operation<Void> original) {
        // Only the position is read and changed: the sound is not resolved yet, so volume and pitch must not be touched.
        if (sound instanceof SimpleSoundInstance simple) {
            Vec3 moved = PortalSounds.toOwnDimension(new Vec3(simple.getX(), simple.getY(), simple.getZ()));
            if (moved != null) {
                AbstractSoundInstanceAccessor accessor = (AbstractSoundInstanceAccessor) simple;
                accessor.aacore$setX(moved.x);
                accessor.aacore$setY(moved.y);
                accessor.aacore$setZ(moved.z);

                // Mining a block in another dimension runs with the client's current world switched to that dimension.
                // The sound now has coordinates in the player's own dimension,
                // So anything that looks at the world while the sound starts (Sound Physics traces it through the world to muffle it and pick its direction)
                // would be reading the wrong dimension's blocks.
                if (ClientWorldLoader.getIsWorldSwitched()) {
                    Minecraft.getInstance().tell(() -> soundManager.play(simple));
                    return;
                }
            }
        }
        original.call(soundManager, sound);
    }
}