package com.jkdr.abyssalascentdimensionpatcher.mixins.patch.c2meSupport.spartanWeaponry;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom; // Use the provided random source

import org.spongepowered.asm.mixin.Overwrite;
import com.oblivioussp.spartanweaponry.util.ItemRandomizer;

@Mixin(value = ItemRandomizer.class, remap = false, priority = 99999)
public abstract class MixinItemRandomizer {


    @Inject(
        method = "generate", // Target method name
        at = @At("HEAD"),    // Inject at the beginning
        cancellable = true   // Allow method to be cancelled
    )
    private static void c2meFix_generate(Level level, List<Item> items, CallbackInfoReturnable<ItemStack> cir) {

        // 1. Get the safe, thread-local random source.
        ThreadLocalRandom rand = ThreadLocalRandom.current();

        // 2. Your randomization logic using the safe random source.
        float weaponRand = rand.nextFloat(); 
        float divider = 1.0f / items.size();
        int idx = Mth.floor(weaponRand / divider);

        // 3. Prevent IndexOutOfBounds
        idx = Math.min(idx, items.size() - 1); 

        // 4. Set the return value (the result of your safe logic).
        cir.setReturnValue(new ItemStack(items.get(idx)));

        // 5. Cancel the original, crashing method logic.
        cir.cancel();
    }
}