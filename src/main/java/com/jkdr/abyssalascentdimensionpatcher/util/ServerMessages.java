package com.jkdr.abyssalascentdimensionpatcher.util;

import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;

public class ServerMessages {
    // The sound played when a block break fails (e.g., wrong tool level)
    public static final SoundEvent BLOCK_BREAK_FAIL_SOUND;
    private static Optional<Holder<SoundEvent>> optionalHolder;

    /**
     * Sends the welcome message to the player when they join.
     */
    public static void welcome(ServerPlayer player) {
        player.sendSystemMessage(FormattingCore.serverTranslate(
            "message.abyssalascentdimensionpatcher.welcome", 
            FormattingCore.createPrefixWithFormatting(), 
            "discord", 
            FormattingCore.createCommandClickableComponent("discord")
        ));
    }

    /**
     * Sends the Discord invite link to the player.
     */
    public static void discord(ServerPlayer player) {
        player.sendSystemMessage(FormattingCore.serverTranslate(
            "message.abyssalascentdimensionpatcher.discordnotice", 
            FormattingCore.createPrefixWithFormatting(), 
            FormattingCore.createExternalLinkClickableComponent("https://discord.com/invite/Z35BDKFEXu", true)
        ));
    }

    /**
     * Informs the player that a structure has spawned at specific coordinates.
     */
    public static void spawnStructureNew(ServerPlayer player, BlockPos pos) {
        player.sendSystemMessage(FormattingCore.serverTranslate(
            "message.abyssalascentdimensionpatcher.structurepos", 
            FormattingCore.createPrefixWithFormatting(), 
            pos.getX(), pos.getY(), pos.getZ()
        ));
    }

    /**
     * Triggered when a player tries to mine a block with a pickaxe that is too weak.
     * Plays a sound and shows a red "Failed" message above the hotbar (action bar).
     */
    public static void pickaxeWeak(ServerPlayer player, BlockPos sourceBlock) {
        if (BLOCK_BREAK_FAIL_SOUND != null) {
            player.level().playSound(
                null, 
                (double)sourceBlock.getX() + 0.5D, 
                (double)sourceBlock.getY() + 0.5D, 
                (double)sourceBlock.getZ() + 0.5D, 
                BLOCK_BREAK_FAIL_SOUND, 
                SoundSource.BLOCKS, 
                1.0F, 1.0F
            );
        }

        // Sends the message to the "Overlay" (the area above the health/hunger bars)
        player.displayClientMessage(
            FormattingCore.serverTranslate("message.abyssalascentdimensionpatcher.failedBlockBreak", ChatFormatting.RED), 
            true
        );
    }

    /**
     * Plays the failure sound at a location without sending a text message.
     */
    public static void invalidSourceMine(Level level, BlockPos sourceBlock) {
        if (BLOCK_BREAK_FAIL_SOUND != null) {
            level.playSound(
                null, 
                (double)sourceBlock.getX() + 0.5D, 
                (double)sourceBlock.getY() + 0.5D, 
                (double)sourceBlock.getZ() + 0.5D, 
                BLOCK_BREAK_FAIL_SOUND, 
                SoundSource.BLOCKS, 
                1.0F, 1.0F
            );
        }
    }

    static {
        // Loads the specific sound defined in the mod's internal configuration
        BLOCK_BREAK_FAIL_SOUND = ForgeRegistries.SOUND_EVENTS.getValue(ModInternalConfig.blockBreakFailSoundLocation);
        optionalHolder = ForgeRegistries.SOUND_EVENTS.getHolder(BLOCK_BREAK_FAIL_SOUND);
    }
}