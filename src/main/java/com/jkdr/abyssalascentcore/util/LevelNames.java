package com.jkdr.abyssalascentcore.util;

import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.Arrays;

/** Display names, colours and the order of the levels from top to bottom, used wherever a level is shown to the player. */
public final class LevelNames {
    private record Display(ResourceLocation id, String name, int color) {}

    /** Top of the stack first. The last entry is the level every pickaxe can reach. */
    private static final List<Display> ORDER = List.of(
            display("minecraft", "overworld", "The Overworld", 0x30BAE3),
            display("infinite_abyss", "first_layer", "Deep Caverns", 0x696969),
            display("deeperdarker", "otherside", "The Otherside", 0x08112E),
            display("oceanworld", "deepsea", "Deepsea", 0x213D9C),
            display("dimension_of_caves", "cave", "Primordial Cave", 0x05850E),
            display("infernalcross", "dimension", "Infernal Crossing", 0x291916),
            display("minecraft", "the_nether", "The Nether", 0x7A3C32),
            display("undergarden", "undergarden", "Undergarden", 0x1C4D10));

    private static final Map<ResourceLocation, Integer> POSITION = new HashMap<>();

    static {
        for (int i = 0; i < ORDER.size(); i++) POSITION.put(ORDER.get(i).id, i);
    }

    private LevelNames() {}

    private static Display display(String namespace, String path, String name, int color) {
        return new Display(new ResourceLocation(namespace, path), name, color);
    }

    /** The level's name in its own colour. Levels without an entry get a tidied-up id in the default colour. */
    public static Component of(ResourceLocation id) {
        Integer position = POSITION.get(id);
        if (position != null) {
            Display display = ORDER.get(position);
            return Component.literal(display.name).withStyle(style -> style.withColor(TextColor.fromRgb(display.color)));
        }

        // "some_mod:the_cave" -> "The Cave", or the dimension's own translation if a mod provides one.
        String words = Arrays.stream(id.getPath().split("[_/]"))
                .map(w -> w.isEmpty() ? w : Character.toUpperCase(w.charAt(0)) + w.substring(1))
                .collect(Collectors.joining(" "));
        return Component.translatableWithFallback(Util.makeDescriptionId("dimension", id), words);
    }

    /** The levels in stack order (top first); levels that are not in the stack go last, by id. */
    public static List<ResourceLocation> ordered(Collection<ResourceLocation> levels) {
        List<ResourceLocation> result = new ArrayList<>(levels);
        result.sort(Comparator.<ResourceLocation>comparingInt(id -> POSITION.getOrDefault(id, Integer.MAX_VALUE)).thenComparing(ResourceLocation::compareTo));
        return result;
    }

    /**
     * Every level from the highest one the pickaxe can break down to the bottom of the stack, in order,
     * including levels in between that have no protected blocks. Levels above its reach are not listed.
     * Levels outside the stack that it can break are added at the end.
     */
    public static List<ResourceLocation> reach(Collection<ResourceLocation> breakable) {
        int highest = Integer.MAX_VALUE;
        List<ResourceLocation> extras = new ArrayList<>();
        for (ResourceLocation id : breakable) {
            Integer position = POSITION.get(id);
            if (position == null) extras.add(id);
            else highest = Math.min(highest, position);
        }

        List<ResourceLocation> result = new ArrayList<>();
        if (highest != Integer.MAX_VALUE) {
            for (int i = highest; i < ORDER.size(); i++) result.add(ORDER.get(i).id);
        }
        extras.sort(null);
        result.addAll(extras);
        return result;
    }
}