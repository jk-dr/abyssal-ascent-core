//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.jkdr.abyssalascentdimensionpatcher.events;

import com.github.alexmodguy.alexscaves.AlexsCaves;
import com.jkdr.abyssalascentdimensionpatcher.AbyssalAscentDimensionPatcher;
import com.jkdr.abyssalascentdimensionpatcher.util.AAExternalConfig;
import com.jkdr.abyssalascentdimensionpatcher.util.RefreshDH;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import org.slf4j.Logger;

@EventBusSubscriber(
        modid = "abyssalascentdimensionpatcher",
        value = Dist.CLIENT
)
public class playerSpawnClientEvents {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static ClientLevel lastWorld = null;

    @SubscribeEvent
    public static void onClientLogin(ClientPlayerNetworkEvent.LoggingIn event) {

        if (AAExternalConfig.WARN_DH_COMPAT.get()
                && AbyssalAscentDimensionPatcher.getIsDistantHorizonsInstalled()) {

            Minecraft.getInstance().player.sendSystemMessage(
                    Component.literal(
                            "Distant Horizons compatibility in Abyssal Ascent is in early stages and if any issues arise please report to @cobster in the discord."
                    )
            );

            AAExternalConfig.WARN_DH_COMPAT.set(false);
            AAExternalConfig.CLIENT_SPEC.save();
        }
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {

        if (event.phase == Phase.END) {

            Minecraft mc = Minecraft.getInstance();
            ClientLevel currentWorld = mc.level;

            if (currentWorld != null && currentWorld != lastWorld) {

                ClientLevel oldWorld = lastWorld;

                if (oldWorld != null
                        && AbyssalAscentDimensionPatcher.getIsDistantHorizonsInstalled()) {

                    RefreshDH.refreshDh(oldWorld, currentWorld);
                }

                lastWorld = currentWorld;
            }
        }
    }

    @EventBusSubscriber(
            modid = "abyssalascentdimensionpatcher",
            value = Dist.CLIENT,
            bus = Bus.MOD
    )
    public static class ModBusEvents {

        @SubscribeEvent
        public static void onConfigLoad(ModConfigEvent event) {
            forceAlexsCavesConfig();
        }

        @SubscribeEvent
        public static void onConfigReload(ModConfigEvent.Reloading event) {
            forceAlexsCavesConfig();
        }

        private static void forceAlexsCavesConfig() {

            if (ModList.get().isLoaded("alexscaves")
                    && AbyssalAscentDimensionPatcher.getIsDistantHorizonsInstalled()) {

                try {

                    if (AlexsCaves.CLIENT_CONFIG.biomeAmbientLightColoring.get()) {

                        LOGGER.warn(
                                "The alexscaves biomeAmbientLightColouring parameter was set to true "
                                        + "(usually on first load), auto disabling to prevent OpenGL error at runtime"
                        );

                        AlexsCaves.CLIENT_CONFIG.biomeAmbientLightColoring.set(false);
                        AlexsCaves.CLIENT_CONFIG.biomeAmbientLightColoring.save();
                    }

                } catch (Exception e) {

                    LOGGER.error(
                            "Abyssal Ascent Patcher: Failed to force Alex's Caves config. "
                                    + e.getMessage()
                    );
                }
            }
        }
    }
}