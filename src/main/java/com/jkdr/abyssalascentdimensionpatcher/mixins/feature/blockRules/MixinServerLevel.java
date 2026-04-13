package com.jkdr.abyssalascentdimensionpatcher.mixins.feature.blockRules;

// Import the new interface
import com.jkdr.abyssalascentdimensionpatcher.data.dimensionRoofData;
import com.jkdr.abyssalascentdimensionpatcher.interfaces.ServerLevelDataAccessor;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(ServerLevel.class)
// FIX: Implement the new interface
public abstract class MixinServerLevel implements ServerLevelDataAccessor {

    private static final Logger LOGGER = LoggerFactory.getLogger("AbyssalAscentPatcher");

    @Unique
    private dimensionRoofData roofData;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void initRoofData(CallbackInfo ci) {
        ServerLevel self = (ServerLevel)(Object)this;

        ResourceKey<Level> dim = self.dimension();

        if (dim.location().equals(new ResourceLocation("infinite_abyss", "first_layer"))) {
            LOGGER.info("INIT BLOCK BREAKING ACTIONS FOR {}", dim);
            this.roofData = new dimensionRoofData(
                self,
                List.of(ResourceKey.create(Registries.ITEM, 
                    new ResourceLocation("kubejs", "tectonic_abyssal_pickaxe")
                )),
                List.of(
                    ResourceKey.create(Registries.BLOCK, new ResourceLocation("minecraft", "obsidian"))
                ),
                123
            );
        } else if (dim.location().equals(new ResourceLocation("deeperdarker", "otherside"))) {
            LOGGER.info("INIT BLOCK BREAKING ACTIONS FOR {}", dim);
            this.roofData = new dimensionRoofData(
                self,
                List.of(ResourceKey.create(Registries.ITEM, 
                    new ResourceLocation("kubejs", "tectonic_abyssal_pickaxe")
                )),
                List.of(
                    ResourceKey.create(Registries.BLOCK, new ResourceLocation("minecraft", "obsidian"))
                ),
                123
            );
        }
        else if (dim.location().equals(new ResourceLocation("infernalcross", "dimension"))) {
            LOGGER.info("INIT BLOCK BREAKING ACTIONS FOR {}", dim);
            this.roofData = new dimensionRoofData(
                self,
                List.of(
                    ResourceKey.create(Registries.ITEM, new ResourceLocation("cataclysm", "black_steel_pickaxe")),
                    ResourceKey.create(Registries.ITEM, new ResourceLocation("kubejs", "tectonic_abyssal_pickaxe"))
                ),
                List.of(
                    ResourceKey.create(Registries.BLOCK, new ResourceLocation("minecraft", "packed_mud")),
                    ResourceKey.create(Registries.BLOCK, new ResourceLocation("alexscaves", "coprolith")),
                    ResourceKey.create(Registries.BLOCK, new ResourceLocation("alexscaves", "guanostone")),
                    ResourceKey.create(Registries.BLOCK, new ResourceLocation("minecraft", "obsidian"))
                ),
                59
            );
        } else if (dim.location().equals(new ResourceLocation("dimension_of_caves", "cave"))) {
            LOGGER.info("INIT BLOCK BREAKING ACTIONS FOR {}", dim);
            this.roofData = new dimensionRoofData(
                self,
                List.of(
                    ResourceKey.create(Registries.ITEM, new ResourceLocation("cataclysm", "black_steel_pickaxe")),
                    ResourceKey.create(Registries.ITEM, new ResourceLocation("kubejs", "tectonic_abyssal_pickaxe"))
                ),
                List.of(
                    ResourceKey.create(Registries.BLOCK, new ResourceLocation("minecraft", "obsidian"))
                ),
                123
            );
        }
        else if (dim.location().equals(new ResourceLocation("minecraft", "the_nether"))) {
            LOGGER.info("INIT BLOCK BREAKING ACTIONS FOR {}", dim);
            this.roofData = new dimensionRoofData(
                self,
                List.of(
                    ResourceKey.create(Registries.ITEM, new ResourceLocation("kubejs", "ignitium_pickaxe")),
                    ResourceKey.create(Registries.ITEM, new ResourceLocation("cataclysm", "black_steel_pickaxe")),
                    ResourceKey.create(Registries.ITEM, new ResourceLocation("kubejs", "tectonic_abyssal_pickaxe"))
                ),
                List.of(
                    ResourceKey.create(Registries.BLOCK, new ResourceLocation("minecraft", "obsidian"))
                ),
                250
            );
        }  else if (dim.location().equals(new ResourceLocation("undergarden", "undergarden"))) {
            LOGGER.info("INIT BLOCK BREAKING ACTIONS FOR {}", dim);
            this.roofData = new dimensionRoofData(
                self,
                List.of(
                    ResourceKey.create(Registries.ITEM, new ResourceLocation("kubejs", "arcane_steel_pickaxe")),
                    ResourceKey.create(Registries.ITEM, new ResourceLocation("minecraft", "netherite_pickaxe")),
                    ResourceKey.create(Registries.ITEM, new ResourceLocation("kubejs", "ignitium_pickaxe")),
                    ResourceKey.create(Registries.ITEM, new ResourceLocation("cataclysm", "black_steel_pickaxe")),
                    ResourceKey.create(Registries.ITEM, new ResourceLocation("kubejs", "tectonic_abyssal_pickaxe"))
                ),
                List.of(
                    ResourceKey.create(Registries.BLOCK, new ResourceLocation("minecraft", "obsidian")),
                    ResourceKey.create(Registries.BLOCK, new ResourceLocation("kubejs", "hard_depthrock"))

                ),
                null
            );
        } else {
            //Just setting to 999 so the check gets skipped
            this.roofData = new dimensionRoofData(self, List.of(), List.of(), 999);
        }
    }

    @Unique
    @Override // This @Override annotation confirms we are correctly implementing the interface
    public dimensionRoofData getRoofData() {
        return this.roofData;
    }
}

