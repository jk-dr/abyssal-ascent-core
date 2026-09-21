package com.jkdr.abyssalascentcore.mixins.feature.blockrules;

import com.jkdr.abyssalascentcore.mining.MiningRules;
import com.jkdr.abyssalascentcore.util.ServerMessages;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// Forge has no event for every block removal, so non-player removals are caught here.
@Mixin(Level.class)
public abstract class MixinLevel {

    @Inject(method = "removeBlock", at = @At("HEAD"), cancellable = true)
    private void aacore$onRemoveBlock(BlockPos pos, boolean isMoving, CallbackInfoReturnable<Boolean> cir) {
        // Player breaks are validated in ServerPlayerGameMode#destroyBlock.
        if (MiningRules.PLAYER_BREAKING.get()) return;

        if ((Object) this instanceof ServerLevel level && !MiningRules.canBreak(level, pos, null)) {
            ServerMessages.invalidSourceMine(level, pos);
            cir.setReturnValue(false);
        }
    }
}
