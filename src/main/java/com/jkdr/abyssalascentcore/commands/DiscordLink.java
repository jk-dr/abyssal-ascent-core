package com.jkdr.abyssalascentcore.commands;

import com.jkdr.abyssalascentcore.AbyssalAscentCore;
import com.jkdr.abyssalascentcore.util.ModInternalConfig;
import com.jkdr.abyssalascentcore.util.ServerMessages;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = AbyssalAscentCore.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class DiscordLink {
	@SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(
            Commands.literal(AbyssalAscentCore.CMDALIAS)
            .then(Commands.literal("discord").executes(context -> {
                ServerPlayer player = context.getSource().getPlayerOrException();
                ServerMessages.discord(player);
                return 1;
            }))
        );
    }
}