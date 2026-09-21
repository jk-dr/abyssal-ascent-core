package com.jkdr.abyssalascentcore;

import com.jkdr.abyssalascentcore.client.ClientHintsConfig;
import com.jkdr.abyssalascentcore.client.DepthMeterConfig;
import com.jkdr.abyssalascentcore.config.IronSpellbookExpandedConfig;
import com.jkdr.abyssalascentcore.config.LocatorConfig;
import com.jkdr.abyssalascentcore.config.PortalSpellConfig;
import com.jkdr.abyssalascentcore.config.StackPreloadConfig;
import com.jkdr.abyssalascentcore.config.SummonsConfig;
import com.jkdr.abyssalascentcore.mining.MiningRules;
import com.jkdr.abyssalascentcore.network.AbyssalNetwork;
import com.jkdr.abyssalascentcore.spell.PortalRay;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig.Type;

@Mod(AbyssalAscentCore.MODID)
public class AbyssalAscentCore {
    public static final String MODID = "abyssalascentcore";
    public static final String CMDALIAS = "aa";

    public AbyssalAscentCore() {
        ModLoadingContext ctx = ModLoadingContext.get();
        ctx.registerConfig(Type.COMMON, IronSpellbookExpandedConfig.SPEC, "irons-spells-config-expansion.toml");
        ctx.registerConfig(Type.COMMON, PortalSpellConfig.SPEC);
        ctx.registerConfig(Type.CLIENT, DepthMeterConfig.SPEC);
        ctx.registerConfig(Type.CLIENT, ClientHintsConfig.SPEC, "abyssalascentcore-client-hints.toml");
        ctx.registerConfig(Type.COMMON, LocatorConfig.SPEC, "abyssalascentcore-locator.toml");
        ctx.registerConfig(Type.COMMON, StackPreloadConfig.SPEC, "abyssalascentcore-preload.toml");
        ctx.registerConfig(Type.COMMON, SummonsConfig.SPEC, "abyssalascentcore-summons.toml");

        AbyssalNetwork.init();
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onServerStopped(ServerStoppedEvent event) {
        PortalRay.clearAll();
    }

    @SubscribeEvent
    public void onServerAboutToStart(ServerAboutToStartEvent event) {
        MiningRules.load();
    }
}