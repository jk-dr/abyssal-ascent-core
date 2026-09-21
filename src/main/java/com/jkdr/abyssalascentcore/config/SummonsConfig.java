package com.jkdr.abyssalascentcore.config;

import com.jkdr.abyssalascentcore.summons.SummonsTarget;
import net.minecraftforge.common.ForgeConfigSpec;

/** The starting setting for who a player's summons may target. */
public final class SummonsConfig {
    public static final ForgeConfigSpec SPEC;
    private static final ForgeConfigSpec.EnumValue<SummonsTarget> DEFAULT_TARGET;
    private static final ForgeConfigSpec.BooleanValue PROTECT_FROM_IGNORED;
    private static final ForgeConfigSpec.IntValue SWITCH_COOLDOWN;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        DEFAULT_TARGET = builder
                .comment("Which players a player's summons may target, for players who have not used /aa summons_target.",
                        "ALL_PLAYERS: any player. OPPONENTS: players who are not in the summoner's team (FTB Teams party). IGNORE_PLAYERS: no players.")
                .defineEnum("summonsTargetDefault", SummonsTarget.OPPONENTS);
        PROTECT_FROM_IGNORED = builder
                .comment("A player cannot attack a summon that is not allowed to attack them: a summon that ignores a player (a teammate, or everyone, depending on its owner's /aa summons_target) cannot be hurt by that player either.",
                        "Set to false to let players attack any summon, whether or not it may attack them back. The summon's owner is never restricted by this.")
                .define("protectSummonsFromPlayersTheyIgnore", true);
        SWITCH_COOLDOWN = builder
                .comment("Seconds a player has to wait between changes of /aa summons_target. Set to 0 for no cooldown.")
                .defineInRange("summonsTargetCooldownSeconds", 5, 0, 600);
        SPEC = builder.build();
    }

    private SummonsConfig() {}

    public static int switchCooldownSeconds() {
        try {
            return SWITCH_COOLDOWN.get();
        } catch (IllegalStateException notLoadedYet) {
            return 5;
        }
    }

    public static boolean protectFromIgnored() {
        try {
            return PROTECT_FROM_IGNORED.get();
        } catch (IllegalStateException notLoadedYet) {
            return true;
        }
    }

    public static SummonsTarget defaultTarget() {
        try {
            return DEFAULT_TARGET.get();
        } catch (IllegalStateException notLoadedYet) {
            return SummonsTarget.OPPONENTS;
        }
    }
}
