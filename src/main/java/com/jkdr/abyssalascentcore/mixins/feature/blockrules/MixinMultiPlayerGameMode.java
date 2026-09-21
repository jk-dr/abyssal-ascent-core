package com.jkdr.abyssalascentcore.mixins.feature.blockrules;

import com.jkdr.abyssalascentcore.mining.MiningRules;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.core.BlockPos;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * The client removes a block as soon as the break finishes and only puts it back when the server denies it.
 * When the mining rules deny the break, don't remove it locally at all.
 */
@Mixin(MultiPlayerGameMode.class)
public abstract class MixinMultiPlayerGameMode {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static long lastDeniedLog;
    private static long lastNoRulesLog;

    @Inject(method = "destroyBlock", at = @At("HEAD"), cancellable = true)
    private void aacore$keepProtectedBlock(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || minecraft.player == null) return;

        if (MiningRules.ruleCount() == 0) {
            long now = System.currentTimeMillis();
            if (now - lastNoRulesLog > 30_000L) {
                lastNoRulesLog = now;
                LOGGER.warn("No mining rules on the client, so denied breaks are not predicted (waiting for the server to send them)");
            }
            return;
        }
        if (!MiningRules.canBreak(minecraft.level, pos, minecraft.player)) {
            cir.setReturnValue(false);
            long now = System.currentTimeMillis();
            if (now - lastDeniedLog > 1_000L) {
                lastDeniedLog = now;
                LOGGER.info("Client kept protected block {} at {} in {}", minecraft.level.getBlockState(pos).getBlock(), pos, minecraft.level.dimension().location());
            }
        }
    }
}