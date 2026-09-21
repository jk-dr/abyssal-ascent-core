package com.jkdr.abyssalascentcore.commands;

import com.jkdr.abyssalascentcore.AbyssalAscentCore;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** /aa bug_report: tells the sender how to report a bug. */
@Mod.EventBusSubscriber(modid = AbyssalAscentCore.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class BugReportCommand {
    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(
            Commands.literal(AbyssalAscentCore.CMDALIAS)
                .then(Commands.literal("bug_report").executes(context -> {
                    context.getSource().sendSuccess(() -> Component.translatable("message.abyssalascentcore.bug_report"), false);
                    return 1;
                }))
        );
    }
}
