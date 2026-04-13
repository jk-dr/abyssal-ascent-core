package com.jkdr.abyssalascentdimensionpatcher.mixins.patch.blockbreaking;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import qouteall.imm_ptl.core.block_manipulation.BlockManipulationServer;

@Mixin(value = ServerPlayerGameMode.class, priority = 2000)
public abstract class MixinServerPlayerGameModePatchForDedicatedServers {

    @Shadow
    @Final
    protected ServerPlayer player;

    @Shadow
    private ServerLevel level;

    private ServerLevel ip_getActualWorld() {
        ServerLevel redirect = BlockManipulationServer.SERVER_PLAYER_INTERACTION_REDIRECT.get();
        if (redirect != null) {
            return redirect;
        }
        return level;
    }

    //THIS FUNCTION DOES WORK AS INTENDED BUT THERE IS ANOTHER CHECK PREVENTING THE BLOCK FROM BREAKING.
    //Wrap operation does not change the method but waits until "canReachRaw" is called and changes the value depending on if it is through a portal.
    //THIS IS A DEDICATED SERVER ONLY OPERATION which is why the blocks didnt auto replace on Essentails and LAN
    @WrapOperation(
        method = "handleBlockBreakAction",
        at = @At(
            value = "INVOKE", //Immersive portals tries to access the wrong method and is not designed to access "canReachRaw" so we have to call it manually. This is not immersive portal's fault instead its just a mod incompatibility.
            target = "Lnet/minecraft/server/level/ServerPlayer;canReachRaw(Lnet/minecraft/core/BlockPos;D)Z"
        )
    )
    private boolean wrapDistanceInHandleBlockBreakAction(
        ServerPlayer instance, BlockPos blockPos, double v, Operation<Boolean> original
    ) {
        ServerLevel redirect = BlockManipulationServer.SERVER_PLAYER_INTERACTION_REDIRECT.get(); //Gets the value of player interaction through portal if value is null it means the interaction is normal and if its not null it is through a portal.
        if (redirect != null) {
            return true; //Is through a portal, do not check in the original method.
        }
        return original.call(instance, blockPos, v); //Is normal, continue with operation.
    }
}