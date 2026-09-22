package com.jkdr.abyssalascentdimensionpatcher.mixins.feature.blockRules;

import com.jkdr.abyssalascentdimensionpatcher.AbyssalAscentDimensionPatcher;
// Import the new interface
import com.jkdr.abyssalascentdimensionpatcher.data.dimensionRoofData;
import com.jkdr.abyssalascentdimensionpatcher.interfaces.ServerLevelDataAccessor;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.world.level.Level;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import java.util.List;

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

// Mixin for catching ALL block removals (Forge doesn't have an event for this)
@Mixin(Level.class)
public abstract class MixinLevel {

    

    @Inject(method = "removeBlock", at = @At("HEAD"))
    public void onRemoveBlockHead(BlockPos pos, boolean isMoving, CallbackInfoReturnable<Boolean> cir) {
        //Only for methods not inclding the player
        if (AbyssalAscentDimensionPatcher.IS_PLAYER_BREAKING.get()) {return;};

        Level level = (Level)(Object)this;
        
        // Check 1: Ensure we are on the server side
        if (level.isClientSide()) {
            return;
        }

        dimensionRoofData roofData = ((ServerLevelDataAccessor) level).getRoofData();

        boolean allowed = roofData.ValidateAbyAscBlockBreak(pos, null);

        if (!allowed) {
            //No message just audible feedback
            ServerMessages.invalidSourceMine(level, pos);
            

            cir.setReturnValue(false); // cancels the method, returns false
        }
        
        
    }
}