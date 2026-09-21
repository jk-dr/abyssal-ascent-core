package com.jkdr.abyssalascentcore.config;

import com.jkdr.abyssalascentcore.depth.LocatorMode;
import net.minecraftforge.common.ForgeConfigSpec;

/** Settings for the depth bar that shows other players' positions. */
public final class LocatorConfig {
    public static final ForgeConfigSpec SPEC;
    private static final ForgeConfigSpec.EnumValue<LocatorMode> TEAM_DEFAULT;
    private static final ForgeConfigSpec.EnumValue<LocatorMode> GLOBAL_DEFAULT;
    private static final ForgeConfigSpec.IntValue UPDATE_INTERVAL;
    private static final ForgeConfigSpec.BooleanValue SHOW_NAMES;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        TEAM_DEFAULT = builder
                .comment("The starting setting, for players who have not used /aa locator team <disable|dot-only|enable>, for sharing dots with their teammates.",
                        "DISABLE: no dots. DOT_ONLY: dots without names. ENABLE: dots with names (holding TAB)",
                        "Teams are FTB Teams parties.")
                .defineEnum("locatorTeamDefault", LocatorMode.ENABLE);
        GLOBAL_DEFAULT = builder
                .comment("The starting setting, for players who have not used /aa locator global <disable|dot-only|enable> this is for sharing dots with everyone who is not a teammate.",
                        "This only applies to players who are not in a team; a player in a team starts hidden from (and unable to see) everyone outside it until they choose a global setting.",
                        "Two players share the more private of their two settings, so a dot only shows if both allow it, and a name only if both are on ENABLE.")
                .defineEnum("locatorGlobalDefault", LocatorMode.ENABLE);
        UPDATE_INTERVAL = builder
                .comment("How often in ticks, players are sent the positions of the other players.")
                .defineInRange("locatorUpdateIntervalTicks", 10, 1, 200);
        SHOW_NAMES = builder
                .comment("Show usernames beside the dots on the depth bar while TAB is held.",
                        "When false the server never sends usernames to clients, whatever players choose: they only receive each visible player's dimension, height and dot colour.")
                .define("locatorShowNames", true);
        SPEC = builder.build();
    }

    private LocatorConfig() {}

    public static LocatorMode teamDefault() {
        try {
            return TEAM_DEFAULT.get();
        } catch (IllegalStateException notLoadedYet) {
            return LocatorMode.ENABLE;
        }
    }

    public static LocatorMode globalDefault() {
        try {
            return GLOBAL_DEFAULT.get();
        } catch (IllegalStateException notLoadedYet) {
            return LocatorMode.ENABLE;
        }
    }

    public static boolean showNames() {
        try {
            return SHOW_NAMES.get();
        } catch (IllegalStateException notLoadedYet) {
            return true;
        }
    }

    public static int updateIntervalTicks() {
        try {
            return UPDATE_INTERVAL.get();
        } catch (IllegalStateException notLoadedYet) {
            return 10;
        }
    }
}
