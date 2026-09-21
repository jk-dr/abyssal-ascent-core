package com.jkdr.abyssalascentcore.depth;

import com.jkdr.abyssalascentcore.config.LocatorConfig;
import com.mojang.logging.LogUtils;
import io.netty.handler.codec.DecoderException;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.scores.Team;
import net.minecraftforge.fml.ModList;

import org.slf4j.Logger;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Which players' dots a player sees on the depth bar. The server decides and clients only draw what they are sent. */
public final class Locator {
    /**
     * What a client is told about another player: where they are and the colour of their dot, nothing that identifies them
     * The name is only filled in (and only sent) when the server has enabled showing names.
     */
    public record Entry(int colour, ResourceLocation dimension, float y, String name) {
        void encode(FriendlyByteBuf buf, boolean withName) {
            buf.writeInt(colour);
            buf.writeResourceLocation(dimension);
            buf.writeFloat(y);
            if (withName) buf.writeUtf(name, 16);
        }

        static Entry decode(FriendlyByteBuf buf, boolean withName) {
            int colour = buf.readInt() & 0xFFFFFF;
            ResourceLocation dimension = DimensionStack.readId(buf);
            float y = buf.readFloat();
            if (!Float.isFinite(y)) throw new DecoderException("Invalid locator height");
            return new Entry(colour, dimension, y, withName ? sanitise(buf.readUtf(16)) : "");
        }
    }

    /** No more players than a server could have online, so a packet cannot make a client allocate a huge list. */
    private static final int MAX_ENTRIES = 1024;

    /** Drops control and formatting characters from a name from the network, so it can only ever be plain text. */
    private static String sanitise(String name) {
        StringBuilder clean = new StringBuilder(name.length());
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if (c >= 0x20 && c != 0x7F && c != '§') clean.append(c);
        }
        return clean.toString();
    }

    private static final String KEY_PREFIX = "abyssalascentcore_locator_";

    /** FTB Teams decides who is on a team when it is installed; vanilla scoreboard teams are the fallback. */
    private static final boolean FTB_TEAMS = ModList.get() != null && ModList.get().isLoaded("ftbteams");

    private static final Logger LOGGER = LogUtils.getLogger();
    private static boolean ftbBroken;

    private static volatile List<Entry> clientPlayers = List.of();
    private static volatile boolean clientNames = false;

    private Locator() {}

    // ---- server ----

    /** Who a setting applies to: teammates, or everyone else. */
    public enum Scope {
        TEAM("team"),
        GLOBAL("global");

        public final String id;

        Scope(String id) {
            this.id = id;
        }
    }

    /**
     * The player's setting for a scope. A player who never chose one gets the configured default,
     * Except that a player in a team is hidden from (and cannot see) everyone outside their team until they choose otherwise.
     */
    public static LocatorMode modeOf(ServerPlayer player, Scope scope) {
        return modeOf(player, scope, teamOf(player));
    }

    private static LocatorMode modeOf(ServerPlayer player, Scope scope, Object team) {
        CompoundTag persisted = player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);
        String key = KEY_PREFIX + scope.id;
        if (persisted.contains(key)) {
            LocatorMode stored = LocatorMode.byId(persisted.getString(key));
            if (stored != null) return stored;
        }
        if (scope == Scope.TEAM) return LocatorConfig.teamDefault();
        return team != null ? LocatorMode.DISABLE : LocatorConfig.globalDefault();
    }

    /** Stores the player's setting for a scope. It is kept with the player, so it survives relogging. */
    public static void set(ServerPlayer player, Scope scope, LocatorMode mode) {
        CompoundTag data = player.getPersistentData();
        CompoundTag persisted = data.getCompound(Player.PERSISTED_NBT_TAG);
        persisted.putString(KEY_PREFIX + scope.id, mode.id);
        data.put(Player.PERSISTED_NBT_TAG, persisted);
    }

    /** Whether both players are in the same team (an FTB Teams party, or a vanilla team if FTB Teams is not installed). */
    public static boolean sameTeam(ServerPlayer a, ServerPlayer b) {
        Object teamA = teamOf(a);
        return teamA != null && teamA.equals(teamOf(b));
    }

    /** The player's team as something that can be compared (the FTB party id, or the vanilla team), or null when they are in none. */
    private static Object teamOf(ServerPlayer player) {
        if (FTB_TEAMS && !ftbBroken) {
            try {
                return ftbPartyId(player);
            } catch (ReflectiveOperationException | LinkageError | RuntimeException e) {
                // Never take the server down over a team lookup: log it once and use vanilla teams instead.
                ftbBroken = true;
                LOGGER.warn("FTB Teams lookup failed, using vanilla teams for the locator instead", e);
            }
        }
        return player.getTeam();
    }
    // FTB Teams is reached by reflection so nothing here links against it: a pack without it,
    // Or a different version of it, cannot break the locator (or the server tick that runs it).
    private static Method ftbApi;
    private static Method ftbIsManagerLoaded;
    private static Method ftbGetManager;
    private static Method ftbTeamForPlayer;
    private static Method ftbIsPlayerTeam;
    private static Method ftbGetId;

    /** The id of the party (or server) team the player is in or null when they are not in one. Everyone has a personal FTB team, which does not count. */
    private static UUID ftbPartyId(ServerPlayer player) throws ReflectiveOperationException {
        if (ftbGetId == null) resolveFtb();

        Object api = ftbApi.invoke(null);
        if (api == null || !(Boolean) ftbIsManagerLoaded.invoke(api)) return null;

        Object manager = ftbGetManager.invoke(api);
        Optional<?> team = (Optional<?>) ftbTeamForPlayer.invoke(manager, player);
        if (team.isEmpty() || (Boolean) ftbIsPlayerTeam.invoke(team.get())) return null;
        return (UUID) ftbGetId.invoke(team.get());
    }

    private static void resolveFtb() throws ReflectiveOperationException {
        ClassLoader loader = Locator.class.getClassLoader();
        Class<?> apiHolder = Class.forName("dev.ftb.mods.ftbteams.api.FTBTeamsAPI", false, loader);
        Class<?> api = Class.forName("dev.ftb.mods.ftbteams.api.FTBTeamsAPI$API", false, loader);
        Class<?> manager = Class.forName("dev.ftb.mods.ftbteams.api.TeamManager", false, loader);
        Class<?> team = Class.forName("dev.ftb.mods.ftbteams.api.Team", false, loader);

        ftbApi = apiHolder.getMethod("api");
        ftbIsManagerLoaded = api.getMethod("isManagerLoaded");
        ftbGetManager = api.getMethod("getManager");
        ftbTeamForPlayer = manager.getMethod("getTeamForPlayer", ServerPlayer.class);
        ftbIsPlayerTeam = team.getMethod("isPlayerTeam");
        ftbGetId = team.getMethod("getId"); // last, so it marks everything above as resolved
    }

    /**
     * The players this viewer may see. Two players share what the more private of them allows,
     * In the scope that fits them: the team setting for teammates, the global setting for everyone else.
     */
    public static List<Entry> visibleTo(ServerPlayer viewer, MinecraftServer server) {
        boolean names = LocatorConfig.showNames();
        Object viewerTeam = teamOf(viewer);

        List<Entry> visible = new ArrayList<>();
        for (ServerPlayer other : server.getPlayerList().getPlayers()) {
            // Spectators are often staff watching unseen, so their position is never handed out.
            if (other == viewer || other.isSpectator()) continue;

            Object otherTeam = teamOf(other);
            Scope scope = viewerTeam != null && viewerTeam.equals(otherTeam) ? Scope.TEAM : Scope.GLOBAL;
            LocatorMode shared = modeOf(viewer, scope, viewerTeam).min(modeOf(other, scope, otherTeam));
            if (shared == LocatorMode.DISABLE) continue;

            visible.add(new Entry(colourOf(other.getUUID()), other.level().dimension().location(), (float) other.getY(),
                    names && shared == LocatorMode.ENABLE ? other.getGameProfile().getName() : ""));
        }
        return visible;
    }
    /** The same colour the vanilla locator bar gives a player: their UUID hash at 90% brightness. Computed here so the UUID itself is never sent. */
    public static int colourOf(UUID player) {
        int hash = player.hashCode();
        float r = (hash >> 16 & 0xFF) / 255.0F;
        float g = (hash >> 8 & 0xFF) / 255.0F;
        float b = (hash & 0xFF) / 255.0F;

        // Same hue and saturation, brightness set to 0.9.
        float max = Math.max(r, Math.max(g, b));
        float min = Math.min(r, Math.min(g, b));
        float delta = max - min;
        float saturation = max == 0.0F ? 0.0F : delta / max;
        float hue;
        if (delta == 0.0F) hue = 0.0F;
        else if (max == r) hue = ((g - b) / delta + 6.0F) % 6.0F;
        else if (max == g) hue = (b - r) / delta + 2.0F;
        else hue = (r - g) / delta + 4.0F;

        float value = 0.9F;
        float chroma = value * saturation;
        float x = chroma * (1.0F - Math.abs(hue % 2.0F - 1.0F));
        float m = value - chroma;
        float[] rgb = switch ((int) hue) {
            case 0 -> new float[]{chroma, x, 0};
            case 1 -> new float[]{x, chroma, 0};
            case 2 -> new float[]{0, chroma, x};
            case 3 -> new float[]{0, x, chroma};
            case 4 -> new float[]{x, 0, chroma};
            default -> new float[]{chroma, 0, x};
        };
        return Math.round((rgb[0] + m) * 255.0F) << 16 | Math.round((rgb[1] + m) * 255.0F) << 8 | Math.round((rgb[2] + m) * 255.0F);
    }

    public static void write(boolean names, List<Entry> entries, FriendlyByteBuf buf) {
        buf.writeBoolean(names);
        buf.writeVarInt(entries.size());
        for (Entry entry : entries) entry.encode(buf, names);
    }

    public static void read(FriendlyByteBuf buf, Synced into) {
        boolean names = buf.readBoolean();
        int size = buf.readVarInt();
        if (size < 0 || size > MAX_ENTRIES) throw new DecoderException("Invalid locator size " + size);
        List<Entry> entries = new ArrayList<>(size);
        for (int i = 0; i < size; i++) entries.add(Entry.decode(buf, names));
        into.accept(names, entries);
    }

    /** Receives a decoded locator update. */
    public interface Synced {
        void accept(boolean names, List<Entry> entries);
    }

    // ---- client ----

    public static void applySynced(boolean names, List<Entry> entries) {
        clientNames = names;
        clientPlayers = List.copyOf(entries);
    }

    public static List<Entry> players() {
        return clientPlayers;
    }

    /** Whether the server lets names be shown. */
    public static boolean namesEnabled() {
        return clientNames;
    }
}
