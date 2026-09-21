package com.jkdr.abyssalascentcore.mixins.feature.cfgexpansion.ironsspellbooks;

import com.jkdr.abyssalascentcore.config.IronSpellbookExpandedConfig;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(
    value = {MagicManager.class},
    remap = false
)
public abstract class MixinSpellCooldowns {
    @Inject(
        method = {"getEffectiveSpellCooldown"},
        at = {@At("RETURN")},
        cancellable = true
    )
    private static void addCustomCooldown(AbstractSpell spell, Player player, CastSource castSource, CallbackInfoReturnable<Integer> cir) {
        String spellId = spell.getSpellId();
        IronSpellbookExpandedConfig.SpellConfigEntries configEntries = (IronSpellbookExpandedConfig.SpellConfigEntries)IronSpellbookExpandedConfig.SPELL_ADDED_CONFIGS.get(spellId);
        int originalCooldown = (Integer)cir.getReturnValue();
        int addedTicks = 0;
        if (configEntries != null) {
            double addedCooldownSeconds = (Double)configEntries.baseCooldownInSeconds().get();
            addedTicks = (int)(addedCooldownSeconds * (double)20.0F);
        }

        cir.setReturnValue(originalCooldown + addedTicks);
    }
}
