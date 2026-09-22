//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.jkdr.abyssalascentdimensionpatcher.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import qouteall.imm_ptl.peripheral.dim_stack.DimStackEntry;
import qouteall.imm_ptl.peripheral.dim_stack.DimStackInfo;
import qouteall.imm_ptl.peripheral.dim_stack.DimStackManagement;

public class ServerDimStackLevelLoaded {

    private static List<ServerLevel> dimensionLevels = List.of();
    private static int listLength = 0;

    private static final byte TICK_UNKNOWN = 0;
    private static final byte TICK_TRUE = 1;
    private static final byte TICK_FALSE = 2;

    private static byte[] dimensionTickStatus = new byte[0];


    public static void initDimensionStackKeys(MinecraftServer server) {

        DimStackInfo info = DimStackManagement.getDimStackPreset();

        List<DimStackEntry> entries =
                info != null && info.entries != null
                        ? info.entries.stream().filter(Objects::nonNull).toList()
                        : List.of();

        List<ServerLevel> mutableLevels = new ArrayList<>();

        for (int i = 0; i < entries.size(); i++) {

            DimStackEntry entry = entries.get(i);
            String idStr = entry.dimensionIdStr;

            if (idStr == null) continue;

            try {

                ResourceLocation rl = new ResourceLocation(idStr);

                ResourceKey<Level> key =
                        ResourceKey.create(Registries.DIMENSION, rl);

                ServerLevel level = server.getLevel(key);

                if (level != null) {

                    ((IDimStackCacheAccessor) level)
                            .setIndexForImmersivePortalsDimstackCache(i);

                    mutableLevels.add(level);
                }

            } catch (Exception ignored) {}
        }

        dimensionLevels = List.copyOf(mutableLevels);
        listLength = dimensionLevels.size();

        dimensionTickStatus = new byte[listLength];
        Arrays.fill(dimensionTickStatus, TICK_UNKNOWN);
    }


    public static void resetDimensionTickingFlags() {

        if (dimensionTickStatus == null
                || dimensionTickStatus.length != listLength) {

            dimensionTickStatus = new byte[listLength];
        }

        Arrays.fill(dimensionTickStatus, TICK_UNKNOWN);
    }


    public static boolean shouldDimensionBeTicking(ServerLevel level) {

        int index =
                ((IDimStackCacheAccessor) level)
                        .getIndexForImmersivePortalsDimstackCache();

        // not part of the dim stack
        if (index == -1) {
            return !level.players().isEmpty();
        }

        if (index < 0 || index >= listLength) {
            return !level.players().isEmpty();
        }

        byte cached = dimensionTickStatus[index];

        if (cached != TICK_UNKNOWN) {
            return cached == TICK_TRUE;
        }

        // if players in this dimension
        if (!level.players().isEmpty()) {
            dimensionTickStatus[index] = TICK_TRUE;
            return true;
        }

        // check dimension below
        int below = index - 1;
        if (below >= 0) {

            ServerLevel belowLevel = dimensionLevels.get(below);

            if (!belowLevel.players().isEmpty()) {

                dimensionTickStatus[index] = TICK_TRUE;
                return true;
            }
        }

        // check dimension above
        int above = index + 1;
        if (above < listLength) {

            ServerLevel aboveLevel = dimensionLevels.get(above);

            if (!aboveLevel.players().isEmpty()) {

                dimensionTickStatus[index] = TICK_TRUE;
                return true;
            }
        }

        dimensionTickStatus[index] = TICK_FALSE;
        return false;
    }
}