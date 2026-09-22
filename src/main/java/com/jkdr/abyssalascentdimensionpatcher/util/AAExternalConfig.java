//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.jkdr.abyssalascentdimensionpatcher.util;

import net.minecraftforge.common.ForgeConfigSpec;

public class AAExternalConfig {
    public static ForgeConfigSpec CLIENT_SPEC;
    public static ForgeConfigSpec.BooleanValue WARN_DH_COMPAT;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        WARN_DH_COMPAT = builder.comment("Sets to true whenever the player enters a world the first time to notify them about potential issues").comment("Serves no purpose to playthrough and is client sided").define("warnDistantHorizonsCompatiblity", true);
        CLIENT_SPEC = builder.build();
    }
}
