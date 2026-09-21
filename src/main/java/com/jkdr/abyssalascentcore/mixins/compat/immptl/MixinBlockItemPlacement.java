package com.jkdr.abyssalascentcore.mixins.compat.immptl;

import com.jkdr.abyssalascentcore.portal.CrossPortal;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * With the head in one dimension and the body in the other (standing across a dimension stack portal),
 * The player only exists in one of them, so a block could be placed inside their body in the other. Placement now also checks for
 * entities reaching through the portal from the other side.
 */
@Mixin(BlockItem.class)
public abstract class MixinBlockItemPlacement {

    @Inject(method = "canPlace", at = @At("RETURN"), cancellable = true)
    private void aacore$notInsideEntityAcrossPortal(BlockPlaceContext context, BlockState state, CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValueZ() && CrossPortal.placementBlockedAcrossPortal(context.getLevel(), context.getClickedPos(), state)) {
            cir.setReturnValue(false);
        }
    }
}