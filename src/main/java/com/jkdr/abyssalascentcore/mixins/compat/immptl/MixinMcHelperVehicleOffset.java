package com.jkdr.abyssalascentcore.mixins.compat.immptl;

import com.jkdr.abyssalascentcore.compat.AircraftSeats;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fml.ModList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import qouteall.imm_ptl.core.McHelper;

/**
 * When a player rides through a portal, Immersive Portals moves the vehicle to the player's new position plus a fixed height (the vehicle's riding offset),
 * which is right for a boat or a horse. An Immersive Aircraft sets the offset to a different place to increase immersion.
 * so the aircraft was put in the wrong place: partly inside the ground (which pushes it down), and the player was then moved to the seat from there.
 * For aircraft the offset is now the real distance between the seat and the aircraft.
 */
@Mixin(value = McHelper.class, remap = false)
public abstract class MixinMcHelperVehicleOffset {

    @Inject(method = "getVehicleOffsetFromPassenger", at = @At("HEAD"), cancellable = true, remap = false)
    private static void aacore$aircraftSeat(Entity vehicle, Entity passenger, CallbackInfoReturnable<Vec3> cir) {
        // AircraftSeats links against the aircraft mod's classes, so it is only touched when that mod is there.
        if (!ModList.get().isLoaded("immersive_aircraft")) return;

        Vec3 seat = AircraftSeats.seatOffset(vehicle, passenger);
        // Immersive Portals wants the offset from the passenger to the vehicle: the seat offset the other way round.
        if (seat != null) cir.setReturnValue(seat.scale(-1.0));
    }
}
