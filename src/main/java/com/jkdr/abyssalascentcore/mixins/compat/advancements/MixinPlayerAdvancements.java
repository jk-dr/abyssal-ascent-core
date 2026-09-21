package com.jkdr.abyssalascentcore.mixins.compat.advancements;

import com.jkdr.abyssalascentcore.util.SilentAdvancements;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.advancements.Advancement;
import net.minecraft.network.chat.Component;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** The advancement is still awarded; only the "X has made the advancement [..]" chat broadcast is skipped. */
@Mixin(PlayerAdvancements.class)
public abstract class MixinPlayerAdvancements {

    @WrapOperation(method = "award", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/server/players/PlayerList;broadcastSystemMessage(Lnet/minecraft/network/chat/Component;Z)V"))
    private void aacore$skipSilentAnnouncement(PlayerList list, Component message, boolean overlay, Operation<Void> original,
                                               @Local(argsOnly = true) Advancement advancement) {
        if (!SilentAdvancements.isSilent(advancement.getId())) original.call(list, message, overlay);
    }
}