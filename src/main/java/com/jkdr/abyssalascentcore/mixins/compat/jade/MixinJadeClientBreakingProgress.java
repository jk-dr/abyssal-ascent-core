package com.jkdr.abyssalascentcore.mixins.compat.jade;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import qouteall.imm_ptl.core.block_manipulation.BlockManipulationClient;

/**
 * Jade's mining progress bar looks up the block being mined in the player's own level using the position from the game mode,
 * which is a position in the far dimension while mining through a portal. It read a different block each frame,
 * so the bar jittered. It now reads the block from the dimension actually being mined.
 */
@Pseudo
@Mixin(targets = "snownee.jade.JadeClient", remap = false)
public abstract class MixinJadeClientBreakingProgress {

    @WrapOperation(method = "drawBreakingProgress", remap = false, at = @At(value = "FIELD", opcode = Opcodes.GETFIELD, remap = true,
            target = "Lnet/minecraft/client/Minecraft;level:Lnet/minecraft/client/multiplayer/ClientLevel;"))
    private static ClientLevel aacore$levelBeingMined(Minecraft minecraft, Operation<ClientLevel> original) {
        if (BlockManipulationClient.isPointingToPortal()) {
            ClientLevel remote = BlockManipulationClient.getRemotePointedWorld();
            if (remote != null) return remote;
        }
        return original.call(minecraft);
    }
}