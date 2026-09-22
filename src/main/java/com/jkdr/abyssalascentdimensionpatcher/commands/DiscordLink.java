package com.jkdr.abyssalascentdimensionpatcher.commands;

import com.jkdr.abyssalascentdimensionpatcher.AbyssalAscentDimensionPatcher;
import com.jkdr.abyssalascentdimensionpatcher.util.ModInternalConfig;
import com.jkdr.abyssalascentdimensionpatcher.util.ServerMessages;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = AbyssalAscentDimensionPatcher.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class DiscordLink {
	@SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(
            Commands.literal(ModInternalConfig.DISCORD_CMD)
            .executes(context -> {
                ServerPlayer player = context.getSource().getPlayerOrException();
                ServerMessages.discord(player);
                return 1; // Commands should return a value
            })
        );
    }
}