package com.jkdr.abyssalascentcore.portal;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;
import qouteall.imm_ptl.core.IPMcHelper;
import qouteall.imm_ptl.core.portal.global_portals.GlobalPortalStorage;
import qouteall.imm_ptl.core.portal.global_portals.VerticalConnectingPortal;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.AABB;
import qouteall.imm_ptl.core.collision.CollisionHelper;
import qouteall.imm_ptl.core.collision.PortalCollisionHandler;
import qouteall.imm_ptl.core.ducks.IEEntity;
import qouteall.imm_ptl.core.portal.Portal;


/** Things vanilla checks in a single level that have to look through Immersive Portals portals as well. */
public final class CrossPortal {
    private CrossPortal() {}

    /** How close to a dimension stack portal's plane a falling player counts as being at it (a fast fall covers about 4 blocks a tick). */
    private static final double PLANE_REACH = 6.0;

    /**
     * Whether {@code box} (a box the entity could occupy, such as one for a taller pose) would have part of it inside a block on the far side of a dimension stack portal.
     * Vanilla checks such a box in the entity's own level only, so a player crawling under a portal ceiling is allowed to stand up into blocks of the dimension above,
     * is pushed back down by Immersive Portals' collision, and is allowed to stand up again the next tick.
     */
    public static boolean boxBlockedAcrossPortal(Entity entity, AABB box) {
        try {
            for (Portal portal : GlobalPortalStorage.getGlobalPortals(entity.level())) {
                if (!(portal instanceof VerticalConnectingPortal)) continue;
                // Only a box that is close to the portal's plane can reach across it.
                if (Math.abs(box.getCenter().y - portal.getY()) > PLANE_REACH) continue;

                Level far = portal.getDestinationWorld();
                if (far == null) continue;
                AABB mapped = CollisionHelper.transformBox(portal, box);
                if (mapped == null) continue;
                // Only the part of the box that is actually beyond the portal plane.
                AABB beyond = CollisionHelper.clipBox(mapped, portal.getDestPos(), portal.getContentDirection());
                if (beyond == null) continue;
                if (!far.noCollision(entity, beyond)) return true;
            }
        } catch (RuntimeException ignored) {
            // Never stop a pose change because of a lookup problem.
        }
        return false;
    }

    /**
     * Whether a falling player has run into the plane of a dimension stack portal whose far side has not loaded yet.
     * Immersive Portals holds the player at the plane until it has, which vanilla reads as landing on the ground,
     * so the player's client reports "on ground" and the server charges fall damage for a fall that has not ended.
     */
    public static boolean waitingForFarSide(Player player) {
        if (player.fallDistance <= 0.0F) return false;

        try {
            boolean client = player.level().isClientSide;
            for (Portal portal : GlobalPortalStorage.getGlobalPortals(player.level())) {
                if (!(portal instanceof VerticalConnectingPortal)) continue;
                if (Math.abs(player.getY() - portal.getY()) > PLANE_REACH) continue;

                Level far = portal.getDestinationWorld();
                if (far == null) {
                    // A client has no world for a dimension until the first of its chunks arrives, which is also "not loaded yet".
                    if (client) return true;
                    continue;
                }
                // The same test Immersive Portals uses to decide the player has to wait: the far side of the player's box.
                if (!far.hasChunkAt(BlockPos.containing(portal.transformPoint(player.getBoundingBox().getCenter())))) return true;
            }
        } catch (RuntimeException ignored) {
            // Never block fall damage because of a lookup problem.
        }
        return false;
    }

    /**
     * Whether {@code box} is free of collisions on the far side of the portals the entity is currently touching.
     * Vanilla's sneak edge check only looks at the entity's own level,
     * so standing on a block that is on the other side of a portal looks like standing over a drop and blocks all sneaking movement.
     */
    public static boolean farSideFree(Entity entity, AABB box) {
        PortalCollisionHandler handler = ((IEEntity) entity).ip_getPortalCollisionHandler();
        if (handler == null || !handler.hasCollisionEntry()) return true;

        for (Portal portal : handler.getCollidingPortals()) {
            if (!portal.getHasCrossPortalCollision()) continue;
            AABB far = CollisionHelper.transformBox(portal, box);
            if (far == null) continue;
            // Only the part of the box that is actually on the content side of the far end of the portal.
            AABB content = CollisionHelper.clipBox(far, portal.getDestPos(), portal.getContentDirection());
            if (content == null) continue;
            if (!portal.getDestWorld().noCollision(entity, content)) return false;
        }
        return true;
    }

    /**
     * Whether an entity in the level on the other side of a portal reaches into the space where the block would be placed.
     * A player whose head is in one dimension and body in the other lives in only one of them, so vanilla,
     * which only looks for entities in the level the block goes into, lets a block be placed inside their body.
     */
    public static boolean placementBlockedAcrossPortal(Level level, BlockPos pos, BlockState state) {
        VoxelShape shape = state.getCollisionShape(level, pos);
        if (shape.isEmpty()) return false;

        try {
            AABB bounds = shape.bounds().move(pos);
            Vec3 center = bounds.getCenter();

            List<Portal> portals = new ArrayList<>();
            List<Portal> global = GlobalPortalStorage.getGlobalPortals(level);
            if (global != null) portals.addAll(global);
            portals.addAll(IPMcHelper.getNearbyPortalList(level, center, 8.0, portal -> true));

            for (Portal portal : portals) {
                // Only blocks close to the portal, on its front side: the space a body sticking through it would occupy.
                if (!portal.isInFrontOfPortal(center) || portal.getDistanceToPlane(center) > 3.5 || !portal.isPointInPortalProjection(center)) continue;

                Level destination = portal.getDestinationWorld();
                AABB inDestination = CollisionHelper.transformBox(portal, bounds);
                if (destination == null || inDestination == null) continue;

                for (Entity entity : destination.getEntities((Entity) null, inDestination, e -> e.blocksBuilding && !e.isRemoved())) {
                    if (entity.getBoundingBox().intersects(inDestination)) return true;
                }
            }
        } catch (RuntimeException ignored) {
            // Never block or crash a placement because of a lookup problem.
        }
        return false;
    }
}
