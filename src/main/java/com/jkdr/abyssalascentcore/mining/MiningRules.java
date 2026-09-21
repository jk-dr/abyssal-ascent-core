package com.jkdr.abyssalascentcore.mining;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Config driven "which pickaxe can break which block in which dimension" rules.
 * <p>
 * The config ({@code config/abyssalascentcore/mining_rules.json}) is resolved once into an immutable
 * {@link Snapshot} of registry objects, so the per-break check is a couple of identity hash lookups
 * and never touches the registries or allocates.
 */
public final class MiningRules {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String DEFAULTS = "/defaults/mining_rules.json";

    /** Set while a player break is being processed so {@code Level#removeBlock} doesn't re-check it as a non-player break. */
    public static final ThreadLocal<Boolean> PLAYER_BREAKING = ThreadLocal.withInitial(() -> false);

    private static volatile Snapshot snapshot = Snapshot.EMPTY;
    private static volatile String syncedJson = "{}";

    private MiningRules() {}

    public static Path configPath() {
        return FMLPaths.CONFIGDIR.get().resolve("abyssalascentcore").resolve("mining_rules.json");
    }

    /**
     * @param player the breaker, or {@code null} for non-player breaks (explosions, other mods, ...)
     * @return whether the block at {@code pos} may be broken
     */
    public static boolean canBreak(Level level, BlockPos pos, @Nullable Player player) {
        // Creative players are never restricted (checked first so it can never be shadowed by a rule).
        if (isExempt(player)) return true;
        Snapshot s = snapshot;
        DimensionRule rule = s.rules.get(level.dimension());
        if (rule == null || pos.getY() < rule.minY) return true;
        if (!rule.covers(level.getBlockState(pos))) return true;
        return player != null && s.tierOf(player.getMainHandItem()) >= rule.minTier;
    }

    private static boolean isExempt(@Nullable Player player) {
        return player != null && (player.isCreative() || player.getAbilities().instabuild);
    }

    /** Dimensions where a rule protects this kind of block and {@code tool} is strong enough to break it. Sorted by id. */
    public static List<ResourceLocation> levelsBreakable(BlockState state, ItemStack tool) {
        Snapshot s = snapshot;
        int tier = s.tierOf(tool);
        List<ResourceLocation> levels = new ArrayList<>();
        s.rules.forEach((dimension, rule) -> {
            if (rule.covers(state) && tier >= rule.minTier) levels.add(dimension.location());
        });
        levels.sort(null);
        return levels;
    }

    /** Dimensions whose protected blocks this pickaxe can break. Empty if the item is not a listed pickaxe. */
    public static List<ResourceLocation> levelsFor(ItemStack tool) {
        Snapshot s = snapshot;
        int tier = s.tierOf(tool);
        if (tier <= 0) return List.of();
        List<ResourceLocation> levels = new ArrayList<>();
        s.rules.forEach((dimension, rule) -> {
            if (tier >= rule.minTier) levels.add(dimension.location());
        });
        levels.sort(null);
        return levels;
    }

    /** (Re)loads the config, creating it from the bundled defaults if missing. Keeps the old rules if the file is broken. */
    public static void load() {
        Path path = configPath();
        try {
            if (Files.notExists(path)) writeDefaults(path);
            String json = Files.readString(path, StandardCharsets.UTF_8);
            snapshot = parse(JsonParser.parseString(json).getAsJsonObject());
            syncedJson = json;
            LOGGER.info("Loaded {} mining rule(s) from {}", snapshot.rules.size(), path);
        } catch (Exception e) {
            LOGGER.error("Could not load {}; keeping the previous mining rules", path, e);
        }
    }

    /** The rules exactly as loaded, to send to clients so they can predict denied breaks. */
    public static String syncedJson() {
        return syncedJson;
    }

    /** Client side: use the rules the server sent. Ignored if they cannot be parsed. */
    public static void applySynced(String json) {
        try {
            snapshot = parse(JsonParser.parseString(json).getAsJsonObject());
            LOGGER.info("Received {} mining rule(s) from the server", snapshot.rules.size());
        } catch (Exception e) {
            LOGGER.error("Could not read the mining rules sent by the server; block breaks will not be predicted", e);
        }
    }

    public static int ruleCount() {
        return snapshot.rules.size();
    }

    private static void writeDefaults(Path path) throws IOException {
        Files.createDirectories(path.getParent());
        try (InputStream in = MiningRules.class.getResourceAsStream(DEFAULTS)) {
            if (in == null) throw new IOException("Missing bundled " + DEFAULTS);
            Files.copy(in, path, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    /** No real rules file comes close to this; it stops a hostile server from making a client build (and log) enormous rule sets. */
    private static final int MAX_ENTRIES = 4096;

    private static void limit(int size, String what) {
        if (size > MAX_ENTRIES) throw new IllegalArgumentException("Too many " + what + " in mining rules (" + size + ")");
    }

    private static Snapshot parse(JsonObject root) {
        Reference2IntOpenHashMap<Item> itemTiers = new Reference2IntOpenHashMap<>();
        List<TagTier> tagTiers = new ArrayList<>();

        JsonObject tiers = root.has("pickaxe_tiers") ? root.getAsJsonObject("pickaxe_tiers") : new JsonObject();
        limit(tiers.size(), "pickaxe tiers");
        for (var entry : tiers.entrySet()) {
            int tier;
            try {
                tier = Integer.parseInt(entry.getKey());
            } catch (NumberFormatException e) {
                tier = 0;
            }
            if (tier < 1) {
                LOGGER.warn("Ignoring pickaxe tier '{}': tier must be a number >= 1", entry.getKey());
                continue;
            }
            limit(entry.getValue().getAsJsonArray().size(), "items in a tier");
            for (JsonElement el : entry.getValue().getAsJsonArray()) {
                String id = el.getAsString();
                if (id.startsWith("#")) {
                    ResourceLocation tag = ResourceLocation.tryParse(id.substring(1));
                    if (tag != null) tagTiers.add(new TagTier(TagKey.create(Registries.ITEM, tag), tier));
                    continue;
                }
                Item item = lookup(ForgeRegistries.ITEMS, id, "pickaxe");
                // Keep the highest tier if an item is listed twice.
                if (item != null && itemTiers.getInt(item) < tier) itemTiers.put(item, tier);
            }
        }

        Reference2ObjectOpenHashMap<ResourceKey<Level>, DimensionRule> rules = new Reference2ObjectOpenHashMap<>();
        JsonObject dims = root.has("dimensions") ? root.getAsJsonObject("dimensions") : new JsonObject();
        limit(dims.size(), "dimension rules");
        for (var entry : dims.entrySet()) {
            ResourceLocation dimId = ResourceLocation.tryParse(entry.getKey());
            if (dimId == null) {
                LOGGER.warn("Ignoring dimension rule '{}': invalid id", entry.getKey());
                continue;
            }
            JsonObject json = entry.getValue().getAsJsonObject();
            int minTier = json.has("min_tier") ? json.get("min_tier").getAsInt() : 1;
            int minY = json.has("min_y") ? json.get("min_y").getAsInt() : Integer.MIN_VALUE;

            Set<Block> blocks = new ReferenceOpenHashSet<>();
            List<TagKey<Block>> blockTags = new ArrayList<>();
            JsonArray blockIds = json.has("blocks") ? json.getAsJsonArray("blocks") : new JsonArray();
            limit(blockIds.size(), "blocks in a dimension rule");
            for (JsonElement el : blockIds) {
                String id = el.getAsString();
                if (id.startsWith("#")) {
                    ResourceLocation tag = ResourceLocation.tryParse(id.substring(1));
                    if (tag != null) blockTags.add(TagKey.create(Registries.BLOCK, tag));
                    continue;
                }
                Block block = lookup(ForgeRegistries.BLOCKS, id, "block");
                if (block != null) blocks.add(block);
            }

            if (blocks.isEmpty() && blockTags.isEmpty()) {
                LOGGER.warn("Dimension rule '{}' has no resolvable blocks, skipping", dimId);
                continue;
            }
            rules.put(ResourceKey.create(Registries.DIMENSION, dimId),
                    new DimensionRule(minTier, minY, blocks, blockTags.toArray(new TagKey[0])));
        }

        itemTiers.defaultReturnValue(0);
        return new Snapshot(rules, itemTiers, tagTiers.toArray(new TagTier[0]));
    }

    @Nullable
    private static <T> T lookup(net.minecraftforge.registries.IForgeRegistry<T> registry, String id, String what) {
        ResourceLocation rl = ResourceLocation.tryParse(id);
        if (rl == null || !registry.containsKey(rl)) {
            LOGGER.warn("Unknown {} '{}' in mining rules (mod not installed?), skipping", what, id);
            return null;
        }
        return registry.getValue(rl);
    }

    private record TagTier(TagKey<Item> tag, int tier) {}

    private record DimensionRule(int minTier, int minY, Set<Block> blocks, TagKey<Block>[] blockTags) {
        boolean covers(BlockState state) {
            if (blocks.contains(state.getBlock())) return true;
            for (TagKey<Block> tag : blockTags) {
                if (state.is(tag)) return true;
            }
            return false;
        }
    }

    private record Snapshot(Reference2ObjectOpenHashMap<ResourceKey<Level>, DimensionRule> rules,
                            Reference2IntOpenHashMap<Item> itemTiers,
                            TagTier[] tagTiers) {
        static final Snapshot EMPTY = new Snapshot(new Reference2ObjectOpenHashMap<>(), new Reference2IntOpenHashMap<>(), new TagTier[0]);

        int tierOf(ItemStack stack) {
            int tier = itemTiers.getInt(stack.getItem());
            for (TagTier t : tagTiers) {
                if (t.tier > tier && stack.is(t.tag)) tier = t.tier;
            }
            return tier;
        }
    }
}
