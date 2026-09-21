package com.jkdr.abyssalascentcore.client;

import com.jkdr.abyssalascentcore.depth.DimensionStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import qouteall.imm_ptl.core.IPMcHelper;
import qouteall.imm_ptl.core.block_manipulation.BlockManipulationClient;
import qouteall.imm_ptl.core.portal.Portal;
import qouteall.imm_ptl.core.portal.global_portals.GlobalPortalStorage;

import java.util.ArrayList;
import java.util.List;

public final class PortalSounds {
    private PortalSounds() {}

    /**
     * Where a position in the dimension the player is mining into (through a portal) is, relative to the player,
     * in the player's own dimension. null if the player is not mining through a portal or no portal links the two.
     * <p>
     * Vanilla plays the mining "hit" sound at the block's position in the far dimension,
     * which is nowhere near the player's own coordinates and does not go through Immersive Portals' cross-portal sound handling.
     * <p>
     * This is still under development and has a bug regarding sound playing in the wrong direction when mining down
     */
    public static @Nullable Vec3 toOwnDimension(Vec3 remotePosition) {
        ResourceKey<Level> remoteDimension = BlockManipulationClient.remotePointedDim;
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (remoteDimension == null || player == null) return null;

        Level own = player.level();
        ResourceKey<Level> ownDimension = own.dimension();
        Vec3 ear = player.getEyePosition();

        // Dimensions of a dimension stack sit at fixed heights relative to each other,
        // so where a block in one appears in another is just a height shift: no portal has to be found, and it works however many dimensions away the block is.
        // Portals do the same job but a lookup can miss (or pick the wrong one)
        // which leaves the sound at the far dimension's coordinates: silent, or from the wrong direction.
        List<DimensionStack.Entry> stack = DimensionStack.clientStack();
        if (!stack.isEmpty()) {
            double blockDepth = DimensionStack.distanceFromTop(stack, remoteDimension.location(), remotePosition.y);
            double earDepth = DimensionStack.distanceFromTop(stack, ownDimension.location(), ear.y);
            if (!Double.isNaN(blockDepth) && !Double.isNaN(earDepth)) {
                // Depth grows downwards: a block deeper in the stack than the player's ear is below them.
                Vec3 shifted = new Vec3(remotePosition.x, ear.y - (blockDepth - earDepth), remotePosition.z);
                return shifted;
            }
        }

        Vec3 best = null;
        double bestDistance = Double.MAX_VALUE;

        // Portals in the player's dimension that lead to the far one -> the block's position maps back through them.
        for (Portal portal : portalsIn(own)) {
            if (portal.getDestDim() != remoteDimension) continue;
            Vec3 inOwnDimension = portal.inverseTransformPoint(remotePosition);
            double distance = inOwnDimension.distanceToSqr(ear);
            if (distance < bestDistance) {
                bestDistance = distance;
                best = inOwnDimension;
            }
        }

        // Otherwise the portals in the far dimension that lead back to the player's -> the position maps forward through them.
        if (best == null) {
            Level remote = minecraft.level;
            if (remote != null) {
                for (Portal portal : portalsIn(remote)) {
                    if (portal.getDestDim() != ownDimension) continue;
                    Vec3 inOwnDimension = portal.transformPoint(remotePosition);
                    double distance = inOwnDimension.distanceToSqr(ear);
                    if (distance < bestDistance) {
                        bestDistance = distance;
                        best = inOwnDimension;
                    }
                }
            }
        }

        return best;
    }

    private static List<Portal> portalsIn(Level level) {
        List<Portal> portals = new ArrayList<>();
        List<Portal> global = GlobalPortalStorage.getGlobalPortals(level);
        if (global != null) portals.addAll(global);
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null) portals.addAll(IPMcHelper.getNearbyPortalList(level, player.position(), 32.0, portal -> true));
        return portals;
    }
}