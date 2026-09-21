package com.jkdr.abyssalascentcore.mixins.compat.advancements;

import com.jkdr.abyssalascentcore.util.SilentAdvancements;
import net.minecraft.client.gui.components.toasts.AdvancementToast;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** The advancement is still shown in the advancement screen; only its pop-up toast is skipped. */
@Mixin(ToastComponent.class)
public abstract class MixinToastComponent {

    @Inject(method = "addToast", at = @At("HEAD"), cancellable = true)
    private void aacore$skipSilentToast(Toast toast, CallbackInfo ci) {
        if (toast instanceof AdvancementToast advancementToast
                && SilentAdvancements.isSilent(((AdvancementToastAccessor) advancementToast).aacore$getAdvancement().getId())) {
            ci.cancel();
        }
    }
}