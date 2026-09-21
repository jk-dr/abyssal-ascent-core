package com.jkdr.abyssalascentcore.commands;

import com.jkdr.abyssalascentcore.AbyssalAscentCore;
import com.jkdr.abyssalascentcore.mining.MiningRules;
import com.jkdr.abyssalascentcore.network.AbyssalNetwork;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = AbyssalAscentCore.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class MiningRulesCommand {
    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(
            Commands.literal(AbyssalAscentCore.CMDALIAS)
                .then(Commands.literal("debug").requires(source -> source.hasPermission(2))
                    .then(Commands.literal("reload_rules").executes(context -> {
                        MiningRules.load();
                        AbyssalNetwork.sendRulesToAll();
                        context.getSource().sendSuccess(
                            () -> Component.translatable("message.abyssalascentcore.rules_reloaded", MiningRules.ruleCount()), true);
                        return MiningRules.ruleCount();
                    })))
        );
    }
}