package com.jkdr.abyssalascentcore.compat;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import net.minecraftforge.fml.ModList;

/**
 * Distant Horizons 3.x supports Immersive Portals itself, so there is no portal compat code here any more;
 * this only tells other code whether the mod is present.
 * Old version of Abyssal Ascent Patcher Core had to add more compat features.
 */
public final class DHCompatLayer {
    // Lazy: ModList isn't populated when mixin classes are first loaded.
    private static final Supplier<Boolean> LOADED =
            Suppliers.memoize(() -> ModList.get().isLoaded("distanthorizons"));

    private DHCompatLayer() {}

    public static boolean isLoaded() {
        return LOADED.get();
    }
}