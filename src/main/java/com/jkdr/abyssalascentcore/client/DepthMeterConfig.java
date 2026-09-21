package com.jkdr.abyssalascentcore.client;

import net.minecraftforge.common.ForgeConfigSpec;

/** Where the depth meter is drawn on screen. */
public final class DepthMeterConfig {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    public static final ForgeConfigSpec.IntValue OVERLAY_X = BUILDER
            .comment("Overlay X offset")
            .defineInRange("offset_x", 5, 0, Integer.MAX_VALUE);

    public static final ForgeConfigSpec.IntValue OVERLAY_Y = BUILDER
            .comment("Overlay Y offset")
            .defineInRange("offset_y", 5, 0, Integer.MAX_VALUE);

    public static final ForgeConfigSpec SPEC = BUILDER.build();

    private DepthMeterConfig() {}
}
