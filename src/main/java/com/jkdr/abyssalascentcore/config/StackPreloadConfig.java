package com.jkdr.abyssalascentcore.config;

import net.minecraftforge.common.ForgeConfigSpec;

/** Settings for loading the next dimension of a dimension stack ahead of a fast moving player. */
public final class StackPreloadConfig {
    public static final ForgeConfigSpec SPEC;
    private static final ForgeConfigSpec.BooleanValue ENABLED;
    private static final ForgeConfigSpec.DoubleValue MIN_VERTICAL_SPEED;
    private static final ForgeConfigSpec.IntValue LOOKAHEAD_TICKS;
    private static final ForgeConfigSpec.IntValue RADIUS_CHUNKS;
    private static final ForgeConfigSpec.IntValue FAST_FALL_MIN_RADIUS;

    // These predetermined values are best for balance in providing a better experience when falling.
    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        ENABLED = builder
                .comment("Load the chunks for the player where a high velocity player is about to enter the next dimension of a dimension stack.")
                .define("preloadEnabled", true);
        MIN_VERTICAL_SPEED = builder
                .comment("Vertical speed in blocks per tick above which the player's movement is used to look ahead")
                .defineInRange("preloadMinVerticalSpeed", 0.5, 0.05, 8.0);
        LOOKAHEAD_TICKS = builder
                .comment("How many ticks ahead the player's current velocity is projected.")
                .defineInRange("preloadLookaheadTicks", 40, 5, 200);
        RADIUS_CHUNKS = builder
                .comment("Radius in chunks loaded around the point where the player is expected to enter each dimension.")
                .defineInRange("preloadRadiusChunks", 3, 1, 8);
        FAST_FALL_MIN_RADIUS = builder
                .comment("While falling or rising fast (above preloadMinVerticalSpeed), the smallest radius in chunks loaded on the far side of a dimension stack portal.",
                        "Immersive Portals normally loads only about 2 chunks there until you are close, which is too little for the far dimension to be ready by the time a fast fall reaches it. Slower movement is not affected.")
                .defineInRange("preloadFastFallMinRadius", 6, 2, 16);
        SPEC = builder.build();
    }

    private StackPreloadConfig() {}

    public static boolean enabled() {
        try {
            return ENABLED.get();
        } catch (IllegalStateException notLoadedYet) {
            return true;
        }
    }

    public static double minVerticalSpeed() {
        try {
            return MIN_VERTICAL_SPEED.get();
        } catch (IllegalStateException notLoadedYet) {
            return 0.5;
        }
    }

    public static int lookaheadTicks() {
        try {
            return LOOKAHEAD_TICKS.get();
        } catch (IllegalStateException notLoadedYet) {
            return 40;
        }
    }

    public static int fastFallMinRadius() {
        try {
            return FAST_FALL_MIN_RADIUS.get();
        } catch (IllegalStateException notLoadedYet) {
            return 6;
        }
    }

    public static int radiusChunks() {
        try {
            return RADIUS_CHUNKS.get();
        } catch (IllegalStateException notLoadedYet) {
            return 3;
        }
    }
}
