package com.jkdr.abyssalascentcore.commands;

import com.jkdr.abyssalascentcore.AbyssalAscentCore;
import com.jkdr.abyssalascentcore.summons.Summons;
import com.jkdr.abyssalascentcore.summons.SummonsTarget;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * /aa summons_target <all_players|opponents|none>: which players the sender's summons may target.
 * Without an argument it shows the current setting. Any player can use it to modify only their own summons.
 */
@Mod.EventBusSubscriber(modid = AbyssalAscentCore.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class SummonsTargetCommand {
    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        LiteralArgumentBuilder<CommandSourceStack> node = Commands.literal("summons_target").executes(context -> {
            ServerPlayer player = context.getSource().getPlayerOrException();
            SummonsTarget mode = Summons.modeOf(player);
            player.sendSystemMessage(Component.translatable("message.abyssalascentcore.summons_target_query",
                Component.translatable("message.abyssalascentcore.summons_mode." + mode.id)));
            return mode.ordinal();
        });

        for (SummonsTarget mode : SummonsTarget.values()) {
            node.then(Commands.literal(mode.id).executes(context -> {
                ServerPlayer player = context.getSource().getPlayerOrException();

                // Changing back and forth is held to a short cooldown; operators are exempt.
                int wait = Summons.cooldownLeftSeconds(player);
                if (wait > 0) {
                    player.sendSystemMessage(Component.translatable("message.abyssalascentcore.summons_target_cooldown", wait));
                    return 0;
                }

                Summons.set(player, mode);
                Summons.startCooldown(player);
                player.sendSystemMessage(Component.translatable("message.abyssalascentcore.summons_target_set",
                    Component.translatable("message.abyssalascentcore.summons_mode." + mode.id)));

                // Applies straight away: summons already fighting someone they may no longer target are called off.
                int calledOff = Summons.callOffForbiddenTargets(player);
                if (calledOff > 0) {
                    player.sendSystemMessage(Component.translatable("message.abyssalascentcore.summons_called_off", calledOff));
                }
                return mode.ordinal();
            }));
        }

        event.getDispatcher().register(Commands.literal(AbyssalAscentCore.CMDALIAS).then(node));
    }
}
