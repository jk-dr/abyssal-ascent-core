package com.jkdr.abyssalascentcore.config;

import net.minecraftforge.common.ForgeConfigSpec;

/** Kill switch for the Immersive Portals aware spell raycasts. */
public final class PortalSpellConfig {
    public static final ForgeConfigSpec SPEC;
    private static final ForgeConfigSpec.BooleanValue ENABLED;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        ENABLED = builder
                .comment("Spells which use the position infront of the player, should use Immersive Portals to prevent players from accidentally entering out of bounds.")
                .define("portalAwareSpellRaycasts", true);
        SPEC = builder.build();
    }

    private PortalSpellConfig() {}

    public static boolean enabled() {
        try {
            return ENABLED.get();
        } catch (IllegalStateException notLoadedYet) {
            return true;
        }
    }
}