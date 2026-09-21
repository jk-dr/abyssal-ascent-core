package com.jkdr.abyssalascentcore.events;

import com.github.alexmodguy.alexscaves.AlexsCaves;
import com.jkdr.abyssalascentcore.AbyssalAscentCore;
import com.jkdr.abyssalascentcore.compat.DHCompatLayer;
import com.mojang.logging.LogUtils;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import org.slf4j.Logger;

/** Forces client settings of other mods that are known to break with Distant Horizons. */
@EventBusSubscriber(modid = AbyssalAscentCore.MODID, value = Dist.CLIENT, bus = Bus.MOD)
public class ClientConfigEvents {
    private static final Logger LOGGER = LogUtils.getLogger();

    @SubscribeEvent
    public static void onConfigLoad(ModConfigEvent.Loading event) {
        forceAlexsCavesConfig();
    }

    @SubscribeEvent
    public static void onConfigReload(ModConfigEvent.Reloading event) {
        forceAlexsCavesConfig();
    }

    // Alex's Caves' ambient light colouring causes an OpenGL error at runtime with Distant Horizons.
    private static void forceAlexsCavesConfig() {
        if (!ModList.get().isLoaded("alexscaves") || !DHCompatLayer.isLoaded()) return;

        try {
            if (AlexsCaves.CLIENT_CONFIG.biomeAmbientLightColoring.get()) {
                LOGGER.warn("Alex's Caves biomeAmbientLightColoring was true (usually on first load), disabling it to prevent an OpenGL error with Distant Horizons");
                AlexsCaves.CLIENT_CONFIG.biomeAmbientLightColoring.set(false);
                AlexsCaves.CLIENT_CONFIG.biomeAmbientLightColoring.save();
            }
        } catch (Exception e) {
            LOGGER.error("Failed to force Alex's Caves config", e);
        }
    }
}