package com.jkdr.abyssalascentcore.depth;

import org.jetbrains.annotations.Nullable;

/** How much of themselves a player shares on the depth bar, and how much of others they get to see. Ordered from least to most. */
public enum LocatorMode {
    /** No dots at all. */
    DISABLE("disable"),
    /** Dots, without names. */
    DOT_ONLY("dot-only"),
    /** Dots with names (while Tab is held). */
    ENABLE("enable");

    public final String id;

    LocatorMode(String id) {
        this.id = id;
    }

    @Nullable
    public static LocatorMode byId(String id) {
        for (LocatorMode mode : values()) {
            if (mode.id.equals(id)) return mode;
        }
        return null;
    }

    /** The more restrictive of two modes: what two players actually share is limited by whichever of them shares less. */
    public LocatorMode min(LocatorMode other) {
        return ordinal() <= other.ordinal() ? this : other;
    }
}
