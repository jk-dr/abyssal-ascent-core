package com.jkdr.abyssalascentcore.compat;

import immersive_aircraft.entity.VehicleEntity;
import immersive_aircraft.entity.misc.PositionDescriptor;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector4f;

import java.util.List;

/**
 * Where a passenger sits in an Immersive Aircraft vehicle. Only reach this class once the "immersive_aircraft" mod is
 * known to be loaded: it links against the mod's own classes.
 */
public final class AircraftSeats {
    private AircraftSeats() {}

    /**
     * The world-space offset from the aircraft's position to the seat the passenger is in,
     * worked out the way the aircraft itself places its riders (each seat is a point in the aircraft's own tilted and rotated space),
     * or null when the vehicle is not an aircraft or has no seat for them.
     */
    @Nullable
    public static Vec3 seatOffset(Entity vehicle, Entity passenger) {
        if (!(vehicle instanceof VehicleEntity aircraft)) return null;

        List<Entity> passengers = aircraft.getPassengers();
        int index = passengers.indexOf(passenger);
        // Immersive Portals takes the player off the aircraft before it works this out,
        // so the player is not in the list any more: they were the pilot (the first seat) of one more passenger than there are now.
        int seatsInUse = index >= 0 ? passengers.size() : passengers.size() + 1;
        if (index < 0) index = 0;

        List<List<PositionDescriptor>> layouts = aircraft.getPassengerPositions();
        int layout = seatsInUse - 1;
        if (layout < 0 || layout >= layouts.size()) return null;
        List<PositionDescriptor> seats = layouts.get(layout);
        if (index >= seats.size()) return null;

        PositionDescriptor seat = seats.get(index);
        float z = seat.z();
        if (passenger instanceof Animal) z += 0.2F;
        float y = seat.y() + (float) passenger.getMyRidingOffset();

        // The aircraft's transform includes its own position (as floats),
        // so subtracting that same float position leaves just the rotated seat offset.
        Vector4f world = aircraft.getVehicleTransform().transform(new Vector4f(seat.x(), y, z, 1.0F));
        return new Vec3(world.x - (float) vehicle.getX(), world.y - (float) vehicle.getY(), world.z - (float) vehicle.getZ());
    }
}
