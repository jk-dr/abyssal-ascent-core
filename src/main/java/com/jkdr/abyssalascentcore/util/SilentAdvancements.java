package com.jkdr.abyssalascentcore.util;

import net.minecraft.resources.ResourceLocation;

import java.util.Set;

/** Advancements that are still granted but never announced in chat or shown as a toast. */
public final class SilentAdvancements {
    private static final Set<ResourceLocation> SILENT = Set.of(
            new ResourceLocation("undergarden", "undergarden/enter_undergarden"),
            new ResourceLocation("traveloptics", "root"));

    private SilentAdvancements() {}

    public static boolean isSilent(ResourceLocation id) {
        return SILENT.contains(id);
    }
}