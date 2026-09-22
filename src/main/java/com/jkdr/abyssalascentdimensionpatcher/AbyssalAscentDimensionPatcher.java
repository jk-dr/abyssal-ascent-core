//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.jkdr.abyssalascentdimensionpatcher;

import com.jkdr.abyssalascentdimensionpatcher.config.IronSpellbookExpandedConfig;
import com.jkdr.abyssalascentdimensionpatcher.util.AAExternalConfig;
import com.mojang.logging.LogUtils;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig.Type;
import org.slf4j.Logger;

@Mod("abyssalascentdimensionpatcher")
public class AbyssalAscentDimensionPatcher {
    public static final String MOD_ID = "abyssalascentdimensionpatcher";
    private static final Logger LOGGER = LogUtils.getLogger();
    public static boolean firstPortalRenderCycleComplete = false;
    private static Boolean isOculusUpdated;
    private static Boolean dhInstalled;

    public AbyssalAscentDimensionPatcher() {
        ModLoadingContext.get().registerConfig(Type.COMMON, IronSpellbookExpandedConfig.SPEC, "irons-spells-config-expansion.toml");
        ModLoadingContext.get().registerConfig(Type.COMMON, AAExternalConfig.CLIENT_SPEC);
        MinecraftForge.EVENT_BUS.register(this);
    }

    private static final ResourceKey<Level> CAVE_DIMENSION =
    ResourceKey.create(Registries.DIMENSION, new ResourceLocation("dimension_of_caves", "cave"));

    public static final ThreadLocal<Boolean> IS_PLAYER_BREAKING = ThreadLocal.withInitial(() -> false);

    public static Boolean getIsDistantHorizonsInstalled() {
        if (dhInstalled == null) {
            dhInstalled = ModList.get().isLoaded("distanthorizons");
        }

        return dhInstalled;
    }

    public static Boolean getIsOculusUpdated() {
        if (isOculusUpdated == null) {
            try {
                Class.forName("net.irisshaders.iris.Iris");
                isOculusUpdated = true;
            } catch (ClassNotFoundException var1) {
                isOculusUpdated = false;
            }
        }

        return isOculusUpdated;
    }

}
