package com.jkdr.abyssalascentcore.compat;

import immersive_aircraft.entity.VehicleEntity;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.fml.ModList;
import org.joml.Matrix4f;

/** Immersive Aircraft's tilt of the view, kept apart so nothing loads its classes when the mod is not installed. */
public final class AircraftTilt {
    private static final boolean LOADED = ModList.get() != null && ModList.get().isLoaded("immersive_aircraft");

    private AircraftTilt() {}

    /** Puts the roll and pitch of the aircraft on the matrix if the entity is one that tilts the view. */
    public static void apply(Entity vehicle, float partialTick, Matrix4f view) {
        if (!LOADED) return;
        applyTilt(vehicle, partialTick, view);
    }

    private static void applyTilt(Entity vehicle, float partialTick, Matrix4f view) {
        if (vehicle instanceof VehicleEntity aircraft && aircraft.adaptPlayerRotation) {
            view.rotateZ((float) Math.toRadians(aircraft.getRoll(partialTick)));
            view.rotateX((float) Math.toRadians(aircraft.getViewXRot(partialTick)));
        }
    }
}
