package com.jkdr.abyssalascentcore.summons;

import org.jetbrains.annotations.Nullable;

/** Which players a player's summons are allowed to pick as a target. */
public enum SummonsTarget {
    /** Any player. */
    ALL_PLAYERS("all_players"),
    /** Players who are not in the summoner's team. */
    OPPONENTS("opponents"),
    /** No players at all. */
    IGNORE_PLAYERS("ignore_players");

    public final String id;

    SummonsTarget(String id) {
        this.id = id;
    }

    @Nullable
    public static SummonsTarget byId(String id) {
        // "none" was this mode's name before it was renamed, so a setting stored under it still works.
        if ("none".equals(id)) return IGNORE_PLAYERS;
        for (SummonsTarget mode : values()) {
            if (mode.id.equals(id)) return mode;
        }
        return null;
    }
}
