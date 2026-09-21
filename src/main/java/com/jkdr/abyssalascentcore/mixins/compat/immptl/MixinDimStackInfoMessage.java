package com.jkdr.abyssalascentcore.mixins.compat.immptl;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import qouteall.imm_ptl.peripheral.dim_stack.DimStackInfo;

/** Hides the "[Immersive Portals] Dimension Stack World Initialized" chat message. Error messages are still sent. */
@Mixin(value = DimStackInfo.class, remap = false)
public abstract class MixinDimStackInfoMessage {

    @WrapOperation(method = "apply", remap = false, at = @At(value = "INVOKE", remap = false,
            target = "Lqouteall/imm_ptl/core/McHelper;sendMessageToFirstLoggedPlayer(Lnet/minecraft/network/chat/Component;)V"))
    private void aacore$hideInitializedMessage(Component text, Operation<Void> original) {
        if (text.getContents() instanceof TranslatableContents contents && contents.getKey().equals("imm_ptl.dim_stack_initialized")) return;
        original.call(text);
    }
}