package com.jkdr.abyssalascentcore.commands;

import com.jkdr.abyssalascentcore.AbyssalAscentCore;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** /aa help: lists the /aa commands the sender can use. */
@Mod.EventBusSubscriber(modid = AbyssalAscentCore.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class HelpCommand {
    private static final String[] EVERYONE = {"help", "locator", "locator_query", "summons_target", "bug_report", "discord"};
    private static final String[] OPERATORS = {"reload_rules", "tunnel"};

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(
            Commands.literal(AbyssalAscentCore.CMDALIAS)
                .then(Commands.literal("help").executes(context -> {
                    CommandSourceStack source = context.getSource();
                    source.sendSuccess(() -> Component.translatable("message.abyssalascentcore.help.header").withStyle(ChatFormatting.GOLD), false);

                    int shown = 0;
                    for (String key : EVERYONE) {
                        line(source, key);
                        shown++;
                    }
                    if (source.hasPermission(2)) {
                        for (String key : OPERATORS) {
                            line(source, key);
                            shown++;
                        }
                    }
                    return shown;
                }))
        );
    }

    private static void line(CommandSourceStack source, String key) {
        source.sendSuccess(() -> Component.translatable("message.abyssalascentcore.help." + key), false);
    }
}
