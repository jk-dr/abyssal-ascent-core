package com.jkdr.abyssalascentdimensionpatcher.mixins.patch.blockbreaking;

import com.jkdr.abyssalascentdimensionpatcher.data.dimensionRoofData;
import com.jkdr.abyssalascentdimensionpatcher.interfaces.ServerLevelDataAccessor;
import com.jkdr.abyssalascentdimensionpatcher.util.ServerMessages;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import qouteall.imm_ptl.core.block_manipulation.BlockManipulationServer;
import com.jkdr.abyssalascentdimensionpatcher.AbyssalAscentDimensionPatcher;

@Mixin(value = ServerPlayerGameMode.class, priority = 2001)
public abstract class MixinServerPlayerGameModePatchForClientAndServers {

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

    @Inject(method = "destroyBlock", at = @At("HEAD"), cancellable = true)
    private void preSetPlayerDestroy(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        ServerLevel level = ip_getActualWorld();
        
        dimensionRoofData roofData = ((ServerLevelDataAccessor) level).getRoofData();

        boolean allowed = roofData.ValidateAbyAscBlockBreak(pos, player);

        if (!allowed) {
            ServerMessages.pickaxeWeak(player, pos);

            cir.setReturnValue(false); // cancels the method, returns false
        } else {
            AbyssalAscentDimensionPatcher.IS_PLAYER_BREAKING.set(true);
        }

        
    }

    @Inject(method = "destroyBlock", at = @At("RETURN"))
    private void postSetPlayerDestroy(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        AbyssalAscentDimensionPatcher.IS_PLAYER_BREAKING.set(false);

    }

    @Redirect(
        method = {
            "removeBlock(Lnet/minecraft/core/BlockPos;Z)Z"
        },
        at = @At(
            value = "FIELD",
            opcode = Opcodes.GETFIELD,
            target = "Lnet/minecraft/server/level/ServerPlayerGameMode;level:Lnet/minecraft/server/level/ServerLevel;"
        )
    )
    private ServerLevel redirectGetLevel(
        ServerPlayerGameMode serverPlayerGameMode
    ) {
        return ip_getActualWorld();
    }
}