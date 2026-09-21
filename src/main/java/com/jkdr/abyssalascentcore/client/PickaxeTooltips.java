package com.jkdr.abyssalascentcore.client;

import com.jkdr.abyssalascentcore.AbyssalAscentCore;
import com.jkdr.abyssalascentcore.mining.MiningRules;
import com.jkdr.abyssalascentcore.util.LevelNames;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

import java.util.List;

/** Adds "Breaks through <level>" lines at the bottom of the tooltip of every pickaxe listed in the mining rules. */
@EventBusSubscriber(modid = AbyssalAscentCore.MODID, value = Dist.CLIENT)
public class PickaxeTooltips {

    // Lowest priority so the lines end up below what other mods add.
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onTooltip(ItemTooltipEvent event) {
        // Every level from the highest it reaches down to the bottom, including ones in between with no mining requirements.
        List<ResourceLocation> levels = LevelNames.reach(MiningRules.levelsFor(event.getItemStack()));
        for (ResourceLocation level : levels) {
            event.getToolTip().add(Component.translatable("tooltip.abyssalascentcore.breaks_through", LevelNames.of(level)).withStyle(ChatFormatting.GRAY));
        }
    }
}