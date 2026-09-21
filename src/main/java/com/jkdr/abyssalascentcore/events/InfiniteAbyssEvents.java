package com.jkdr.abyssalascentcore.events;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.jkdr.abyssalascentcore.AbyssalAscentCore;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Infinite Abyss punishes players who lack the Depth Resistance potion with Depth Sickness (wither) in its deep layers.
 * The pack no longer wants the potion to be needed, so Depth Sickness can never be applied.
 * (The sixth layer's entry procedure is handled in MixinSixthLayerPlayerEntersDimension, the cold mechanic is untouched.)
 */
@EventBusSubscriber(modid = AbyssalAscentCore.MODID)
public class InfiniteAbyssEvents {
    // Resolved on first use -> registries aren't ready at class load, and Infinite Abyss may be absent.
    private static final Supplier<MobEffect> DEPTH_SICKNESS = Suppliers.memoize(() ->
            ForgeRegistries.MOB_EFFECTS.getValue(new ResourceLocation("infinite_abyss", "depth_sickness")));

    @SubscribeEvent
    public static void onEffectApplicable(MobEffectEvent.Applicable event) {
        MobEffect sickness = DEPTH_SICKNESS.get();
        if (sickness != null && event.getEffectInstance().getEffect() == sickness) {
            event.setResult(Event.Result.DENY);
        }
    }
}