package com.jkdr.abyssalascentcore.summons;

import com.jkdr.abyssalascentcore.config.SummonsConfig;
import com.jkdr.abyssalascentcore.depth.Locator;
import net.minecraft.nbt.CompoundTag;
import io.redspace.ironsspellbooks.entity.mobs.MagicSummon;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;

/** Each player's choice of which players their summons may target, and the rule that follows from it. */
public final class Summons {
    private static final String KEY = "abyssalascentcore_summons_target";
    private static final String WARNED_KEY = "abyssalascentcore_summons_warned";

    private Summons() {}

    /** The player's setting; the configured default if they never chose one. */
    public static SummonsTarget modeOf(ServerPlayer player) {
        CompoundTag persisted = player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);
        if (persisted.contains(KEY)) {
            SummonsTarget stored = SummonsTarget.byId(persisted.getString(KEY));
            if (stored != null) return stored;
        }
        return SummonsConfig.defaultTarget();
    }

    /** Stores the player's setting. It is kept with the player, so it survives relogging. */
    public static void set(ServerPlayer player, SummonsTarget mode) {
        CompoundTag data = player.getPersistentData();
        CompoundTag persisted = data.getCompound(Player.PERSISTED_NBT_TAG);
        persisted.putString(KEY, mode.id);
        data.put(Player.PERSISTED_NBT_TAG, persisted);
    }

    /**
     * Makes the owner's summons stop attacking any player they are no longer allowed to target.
     * Changing the setting only stops summons picking new targets, so ones already fighting a player have to be called off here.
     * Returns how many were called off.
     */
    public static int callOffForbiddenTargets(ServerPlayer owner) {
        int calledOff = 0;
        for (ServerLevel level : owner.server.getAllLevels()) {
            for (Entity entity : level.getAllEntities()) {
                if (!(entity instanceof Mob mob) || !(entity instanceof MagicSummon summon)) continue;
                LivingEntity summoner = summon.getSummoner();
                if (summoner == null || !summoner.getUUID().equals(owner.getUUID())) continue;

                if (mob.getTarget() instanceof ServerPlayer target && !mayTarget(owner, target)) {
                    mob.setTarget(null);
                    mob.getNavigation().stop();
                    calledOff++;
                }
            }
        }
        return calledOff;
    }

    /** Server tick each player last changed their setting, to hold them to the cooldown. Not saved: it only has to outlast a few seconds. */
    private static final Map<UUID, Integer> LAST_SWITCH = new HashMap<>();

    /**
     * How many seconds (rounded up) the player still has to wait before they may change the setting again, or 0 if they
     * may now. Operators never have to wait.
     */
    public static int cooldownLeftSeconds(ServerPlayer player) {
        int cooldownTicks = SummonsConfig.switchCooldownSeconds() * 20;
        if (cooldownTicks <= 0 || player.hasPermissions(2)) return 0;

        Integer last = LAST_SWITCH.get(player.getUUID());
        if (last == null) return 0;
        int left = cooldownTicks - (player.server.getTickCount() - last);
        return left <= 0 ? 0 : (left + 19) / 20;
    }

    /** Starts the cooldown, once the player has changed the setting. */
    public static void startCooldown(ServerPlayer player) {
        LAST_SWITCH.put(player.getUUID(), player.server.getTickCount());
    }

    public static void forget(UUID player) {
        LAST_SWITCH.remove(player);
    }

    /** Whether the one-off notice that summons can attack players has been shown to this player. */
    public static boolean wasWarned(ServerPlayer player) {
        return player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG).getBoolean(WARNED_KEY);
    }

    public static void markWarned(ServerPlayer player) {
        CompoundTag data = player.getPersistentData();
        CompoundTag persisted = data.getCompound(Player.PERSISTED_NBT_TAG);
        persisted.putBoolean(WARNED_KEY, true);
        data.put(Player.PERSISTED_NBT_TAG, persisted);
    }

    /**
     * Whether summons belonging to {@code owner} may target {@code target}.
     * Opponents are everyone outside the owner's team, including players who are not in a team at all. A summon is never stopped from anything to do with its own owner.
     */
    public static boolean mayTarget(ServerPlayer owner, ServerPlayer target) {
        if (owner == target) return true;
        return switch (modeOf(owner)) {
            case ALL_PLAYERS -> true;
            case OPPONENTS -> !Locator.sameTeam(owner, target);
            case IGNORE_PLAYERS -> false;
        };
    }
}
