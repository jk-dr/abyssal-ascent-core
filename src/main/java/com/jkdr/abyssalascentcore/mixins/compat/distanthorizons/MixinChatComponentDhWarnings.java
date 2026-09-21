package com.jkdr.abyssalascentcore.mixins.compat.distanthorizons;

import net.minecraft.client.GuiMessageTag;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MessageSignature;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Distant Horizons prints its own warnings in chat when it starts (Alex's Caves, the G1 garbage collector, its
 * Immersive Portals mixin, memory, render distance and so on). Every one of them is sent as
 * {@code "<colour code>Distant Horizons: <title>.<reset>..."} through {@code Player#displayClientMessage}, so they are
 * dropped where chat lines are added. Its error messages start differently and are kept.
 */
@Mixin(ChatComponent.class)
public abstract class MixinChatComponentDhWarnings {

    @Inject(method = "addMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/MessageSignature;Lnet/minecraft/client/GuiMessageTag;)V",
            at = @At("HEAD"), cancellable = true, remap = true)
    private void aacore$hideDistantHorizonsWarnings(Component message, MessageSignature signature, GuiMessageTag tag, CallbackInfo ci) {
        String text = message.getString();

        // Distant Horizons sends its messages as a plain string with legacy colour codes (a section sign and a character) still in it.
        int start = 0;
        while (start + 1 < text.length() && text.charAt(start) == '§') start += 2;

        if (text.startsWith("Distant Horizons:", start)) ci.cancel();
    }
}
