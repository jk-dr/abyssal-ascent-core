package com.jkdr.abyssalascentcore.client;

import com.jkdr.abyssalascentcore.AbyssalAscentCore;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

/** A tip about using the preferred garbage collector for Distant Horizons, shown in chat each time a world is joined (it can be switched off in the client config). */
@EventBusSubscriber(modid = AbyssalAscentCore.MODID, value = Dist.CLIENT)
public class ClientHints {

    @SubscribeEvent
    public static void onClientLogin(ClientPlayerNetworkEvent.LoggingIn event) {
        if (!ClientHintsConfig.SHOW_GC_HINT.get()) return;

        event.getPlayer().sendSystemMessage(Component.translatable("message.abyssalascentcore.gc_hint",
                Component.translatable("message.abyssalascentcore.prefix").withStyle(ChatFormatting.GRAY)));
    }
}
