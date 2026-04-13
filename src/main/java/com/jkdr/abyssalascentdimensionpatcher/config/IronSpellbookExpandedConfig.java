package com.jkdr.abyssalascentdimensionpatcher.config;

import net.minecraftforge.common.ForgeConfigSpec;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;

import java.util.HashMap;
import java.util.Map;
import io.redspace.ironsspellbooks.config.SpellDiscovery;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class IronSpellbookExpandedConfig {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static ForgeConfigSpec SPEC;
    public static final Map<String, SpellConfigEntries> SPELL_ADDED_CONFIGS = new HashMap<>();

    public static void addSpellCategory(AbstractSpell spell) {
        String spellId =  spell.getSpellId();
        if (SPELL_ADDED_CONFIGS.containsKey(spellId))
            return;

        BUILDER.push(sanitizeCategory(spellId)); // push 1 (the spell)

        ForgeConfigSpec.ConfigValue<Double> bcIS = BUILDER
                .define("baseCooldownInSeconds", 0.0);

        BUILDER.pop(); // pop 1 (the spell)
        SPELL_ADDED_CONFIGS.put(spellId, new SpellConfigEntries(bcIS));
    }

    public static SpellConfigEntries getSpellConfig(AbstractSpell abstractSpell) {
        // Return null if the spell is not found in our config map
        return SPELL_ADDED_CONFIGS.getOrDefault(abstractSpell.getSpellId(), null);
    }

   static {
        SpellDiscovery.getSpellsForConfig()
            .stream()
            .collect(Collectors.groupingBy(x -> x.getDefaultConfig().schoolResource))
            .forEach((school, spells) -> {
            
                // BUILDER.comment(school.toString()); // <-- This just adds a comment
                BUILDER.push(school.toString()); // <-- This creates the [SCHOOL] category
            
                spells.forEach(IronSpellbookExpandedConfig::addSpellCategory); // This adds all the [SCHOOL.SPELL] sub-categories
            
                BUILDER.pop(); // <-- This closes the [SCHOOL] category
            });

        SPEC = BUILDER.build();
    }


    private static String sanitizeCategory(String spellId) {
        return spellId.replace(':', '_');
    }

    public record SpellConfigEntries(
            ForgeConfigSpec.ConfigValue<Double> baseCooldownInSeconds
    ) {}
}