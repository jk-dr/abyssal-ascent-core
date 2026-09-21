package com.jkdr.abyssalascentcore.client;

import net.minecraftforge.common.ForgeConfigSpec;

/** Kept apart from {@link ClientHints} so registering it does not load client-only classes on a dedicated server. */
public final class ClientHintsConfig {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    public static final ForgeConfigSpec.BooleanValue SHOW_GC_HINT = BUILDER
            .comment("Show the tip about changing the garbage collector in chat each time a world is joined.")
            .define("showGarbageCollectorHint", true);

    public static final ForgeConfigSpec SPEC = BUILDER.build();

    private ClientHintsConfig() {}
}
