package com.jkdr.abyssalascentcore.events;

import com.jkdr.abyssalascentcore.AbyssalAscentCore;
import com.jkdr.abyssalascentcore.config.SummonsConfig;
import com.jkdr.abyssalascentcore.summons.Summons;
import io.redspace.ironsspellbooks.entity.mobs.MagicSummon;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingChangeTargetEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

/** Keeps a player's summons and the players they may not target apart, in both directions. */
@EventBusSubscriber(modid = AbyssalAscentCore.MODID)
public class SummonTargetEvents {

    @SubscribeEvent
    public static void onChangeTarget(LivingChangeTargetEvent event) {
        if (!(event.getNewTarget() instanceof ServerPlayer target)) return;
        if (!(event.getEntity() instanceof MagicSummon summon)) return;
        if (!(summon.getSummoner() instanceof ServerPlayer owner)) return;

        if (!Summons.mayTarget(owner, target)) event.setCanceled(true);
    }

    /** The first time one of a player's summons attacks another player, its owner is told it can and how to turn that off. */
    @SubscribeEvent
    public static void onSummonAttacksPlayer(LivingAttackEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer victim)) return;

        // The summon itself, or something it cast or threw.
        MagicSummon summon = event.getSource().getDirectEntity() instanceof MagicSummon direct ? direct
                : event.getSource().getEntity() instanceof MagicSummon indirect ? indirect : null;
        if (summon == null || !(summon.getSummoner() instanceof ServerPlayer owner)) return;
        if (owner == victim || Summons.wasWarned(owner) || !Summons.mayTarget(owner, victim)) return;

        Summons.markWarned(owner);
        owner.sendSystemMessage(Component.translatable("message.abyssalascentcore.summons_can_attack"));
    }

    /**
     * The other way round: a player cannot attack a summon that is not allowed to attack them.
     * So a summon that ignores a player (a teammate, or everyone, depending on its owner's setting) cannot be hurt by that player either, and once it
     * is allowed to target them it can be fought back. The owner is not restricted here.
     */
    @SubscribeEvent
    public static void onAttack(LivingAttackEvent event) {
        if (!SummonsConfig.protectFromIgnored()) return;
        if (!(event.getEntity() instanceof MagicSummon summon)) return;
        if (!(summon.getSummoner() instanceof ServerPlayer owner)) return;
        // The player behind the damage, including one who threw or cast it from a distance.
        if (!(event.getSource().getEntity() instanceof ServerPlayer attacker)) return;

        if (!Summons.mayTarget(owner, attacker)) {
            event.setCanceled(true);
            attacker.displayClientMessage(Component.translatable("message.abyssalascentcore.summon_protected"), true);
        }
    }
}
