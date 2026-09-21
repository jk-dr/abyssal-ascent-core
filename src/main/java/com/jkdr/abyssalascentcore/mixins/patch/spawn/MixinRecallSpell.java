package com.jkdr.abyssalascentcore.mixins.patch.spawn;

import com.jkdr.abyssalascentcore.spawn.SpawnLocator;
import com.jkdr.abyssalascentcore.util.ModInternalConfig;
import io.redspace.ironsspellbooks.spells.ender.RecallSpell;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = {RecallSpell.class}, priority = 2000)
public abstract class MixinRecallSpell {

    /**
     * Injects into Iron's Spells 'n Spellbooks' "findSpawnPosition" method.
     * If the spell fails to find a valid bed or respawn anchor,
     * this code steps in to provide the Undergarden spawn coordinates instead.
     */
    @Inject(
        method = "findSpawnPosition",
        at = @At("RETURN"),
        cancellable = true,
        remap = false // 'false' because Iron's Spells methods aren't obfuscated the same way as Minecraft
    )
    private static void onFindSpawnPositionReturn(ServerLevel level, ServerPlayer player, CallbackInfoReturnable<Optional<Vec3>> cir) {
        Optional<Vec3> original = cir.getReturnValue();

        // If the original spell found nothing (isEmpty) and the player exists (m_8963_ -> getUUID)
        if (original.isEmpty() && player.getUUID() != null) {
            // Overwrite the return value with our custom Undergarden position
            cir.setReturnValue(Optional.of(getCustomRespawnPosition(player)));
            cir.cancel();
        }
    }

    /**
     * Logic to determine where the Recall spell should send the player.
     */
    private static Vec3 getCustomRespawnPosition(ServerPlayer player) {
        // If the player DOES NOT have a custom respawn point (bed/anchor)
        if (player.getRespawnPosition() == null) { // m_8961_
            MinecraftServer server = player.getServer(); // m_20194_
            
            if (server == null) {
                return Vec3.ZERO; // f_82478_
            } else {
                // Look for the Undergarden dimension defined in ModInternalConfig
                ServerLevel undergardenLevel = server.getLevel(ModInternalConfig.playerSpawnDimension);
                
                if (undergardenLevel == null) {
                    return Vec3.ZERO;
                } else {
                    // Get the specific spawn coordinates for this dimension
                    BlockPos rawPos = SpawnLocator.getSpawnCoordinates(undergardenLevel);
                    
                    // Apply a slight offset: -5 blocks X, +1 block Y, +3 blocks Z
                    BlockPos adjustedPos = rawPos.offset(-5, 1, 3); // m_7918_
                    
                    // Convert BlockPos to Vec3 (exact coordinates)
                    return Vec3.atCenterOf(adjustedPos); // m_82512_
                }
            }
        } else {
            // If they HAVE a bed, just use that bed's location
            return Vec3.atCenterOf(player.getRespawnPosition());
        }
    }
}