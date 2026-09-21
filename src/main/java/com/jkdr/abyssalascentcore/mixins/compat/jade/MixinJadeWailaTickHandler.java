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
 * Jade reads the block state and block entity of what you look at from the client's current level.
 * Through a portal that has to be the level on the far side, otherwise it looks up the far position in the wrong dimension.
 */
@Pseudo
@Mixin(targets = "snownee.jade.overlay.WailaTickHandler", remap = false)
public abstract class MixinJadeWailaTickHandler {

    @WrapOperation(method = "tickClient", remap = false, at = @At(value = "FIELD", opcode = Opcodes.GETFIELD, remap = true,
            target = "Lnet/minecraft/client/Minecraft;level:Lnet/minecraft/client/multiplayer/ClientLevel;"))
    private ClientLevel aacore$levelOfPointedBlock(Minecraft minecraft, Operation<ClientLevel> original) {
        if (BlockManipulationClient.isPointingToPortal()) {
            ClientLevel remote = BlockManipulationClient.getRemotePointedWorld();
            if (remote != null) return remote;
        }
        return original.call(minecraft);
    }
}