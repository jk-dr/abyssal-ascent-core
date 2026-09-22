package com.jkdr.abyssalascentdimensionpatcher.util;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

public class ModInternalConfig {
    // Commands and Strings
    public static final String BOOK_TOGGLE_CMD = "guide";
    public static final String DISCORD_CMD = "discord";
    public static final String DISCORD_INVITE = "https://discord.com/invite/Z35BDKFEXu";
    public static final String ABYSSAL_ASCENT_OWNER_PREFIX = "<Abyssal Ascent>";
    public static final String CMD_LINK_TEXT = "[Click Here]";
    public static final String URL_LINK_TEXT = "[Open URL]";

    // NBT Tags (Used to track player data)
    public static final String FIRST_JOIN_TAG = "abyssal_ascent_first_join";
    public static final String DISABLE_GUIDE_TAG = "abyssal_ascent_disable_helpbook";

    // Resource Keys
    public static final ResourceKey<Level> playerSpawnDimension;
    public static final ResourceLocation blockBreakFailSoundLocation;

    static {
        // Sets the default spawn dimension to 'The Undergarden'
        // m_135785_ -> create() | f_256858_ -> DIMENSION
        playerSpawnDimension = ResourceKey.create(Registries.DIMENSION, new ResourceLocation("undergarden", "undergarden"));

        // Sets the failure sound to a Banshee hurt sound from 'NetherEx'
        blockBreakFailSoundLocation = new ResourceLocation("netherexp", "entity.banshee.hurt");
    }
}