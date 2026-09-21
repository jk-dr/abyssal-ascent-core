package com.jkdr.abyssalascentcore.commands;

import com.jkdr.abyssalascentcore.AbyssalAscentCore;
import com.jkdr.abyssalascentcore.config.LocatorConfig;
import com.jkdr.abyssalascentcore.depth.Locator;
import com.jkdr.abyssalascentcore.depth.LocatorMode;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * /aa locator team <disable|dot-only|enable>: what the player shares with, and sees of, their teammates.
 * /aa locator global <disable|dot-only|enable>: the same for everyone who is not a teammate.
 * /aa locator query: shows both settings.
 * Any player can use these for themselves to conceal themselves if they wish.
 */
@Mod.EventBusSubscriber(modid = AbyssalAscentCore.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class LocatorCommand {
    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(
            Commands.literal(AbyssalAscentCore.CMDALIAS)
                .then(Commands.literal("locator")
                    .then(scope(Locator.Scope.TEAM))
                    .then(scope(Locator.Scope.GLOBAL))
                    .then(Commands.literal("query").executes(context -> {
                        ServerPlayer player = context.getSource().getPlayerOrException();
                        LocatorMode team = Locator.modeOf(player, Locator.Scope.TEAM);
                        LocatorMode global = Locator.modeOf(player, Locator.Scope.GLOBAL);
                        player.sendSystemMessage(Component.translatable("message.abyssalascentcore.locator_query",
                            Component.translatable("message.abyssalascentcore.locator_mode." + team.id),
                            Component.translatable("message.abyssalascentcore.locator_mode." + global.id)));
                        if (!LocatorConfig.showNames()) {
                            player.sendSystemMessage(Component.translatable("message.abyssalascentcore.locator_names_off"));
                        }
                        return 1;
                    })))
        );
    }

    private static LiteralArgumentBuilder<CommandSourceStack> scope(Locator.Scope scope) {
        LiteralArgumentBuilder<CommandSourceStack> node = Commands.literal(scope.id);
        for (LocatorMode mode : LocatorMode.values()) {
            node.then(Commands.literal(mode.id).executes(context -> {
                ServerPlayer player = context.getSource().getPlayerOrException();
                Locator.set(player, scope, mode);
                player.sendSystemMessage(Component.translatable("message.abyssalascentcore.locator_set",
                    Component.translatable("message.abyssalascentcore.locator_scope." + scope.id),
                    Component.translatable("message.abyssalascentcore.locator_mode." + mode.id)));
                return mode.ordinal();
            }));
        }
        return node;
    }
}
