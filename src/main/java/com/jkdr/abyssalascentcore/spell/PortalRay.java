package com.jkdr.abyssalascentcore.spell;

import com.jkdr.abyssalascentcore.config.PortalSpellConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import qouteall.imm_ptl.core.IPMcHelper;
import qouteall.imm_ptl.core.api.PortalAPI;
import qouteall.imm_ptl.core.portal.Portal;
import qouteall.imm_ptl.core.portal.global_portals.GlobalPortalStorage;

import java.util.List;

/** Block raycasts that follow Immersive Portals, for spells that need to act on the far side. */
public final class PortalRay {
    /** Set while Teleport's aim is being computed, so its clips run against the far level. */
    public static final ThreadLocal<Trace> BLINK = new ThreadLocal<>();
    /** Set once a landing spot was found on the far side, until the spell teleports its caster there. */
    private static final ThreadLocal<Pending> PENDING = new ThreadLocal<>();
    /** Set by the Portal spell mixin for the duration of one cast. */
    public static final ThreadLocal<Trace> PORTAL_SPELL = new ThreadLocal<>();

    private PortalRay() {}

    private record Pending(ServerLevel level, Entity caster, Vec3 destination) {}

    public static void beginPendingTeleport(Trace trace, Entity caster, Vec3 destination) {
        PENDING.set(new Pending(trace.level(), caster, destination));
    }

    public static void clearPendingTeleport() {
        PENDING.remove();
    }

    /** Drops everything held for the current thread, so a stopped server's levels and entities are not kept alive by it. */
    public static void clearAll() {
        BLINK.remove();
        PENDING.remove();
        PORTAL_SPELL.remove();
    }

    /**
     * Called from every {@code teleportTo(x, y, z)}. If the caster is teleporting to the landing spot that was just found on the far side of a portal,
     * sends it to that level instead. Only an exact caster and nearby position match is taken over, so unrelated teleports are never redirected.
     *
     * @return true if the teleport was handled
     */
    public static boolean crossTeleportIfPending(Entity entity, double x, double y, double z) {
        Pending pending = PENDING.get();
        if (pending == null || pending.caster != entity) return false;

        Vec3 destination = pending.destination;
        if (Math.abs(destination.x - x) > 1.5 || Math.abs(destination.y - y) > 1.5 || Math.abs(destination.z - z) > 1.5) return false;

        PENDING.remove();
        if (entity.level() == pending.level) return false;
        PortalAPI.teleportEntity(entity, pending.level, new Vec3(x, y, z));
        return true;
    }

    /**
     * @param level  the level the ray ended in (the far side of the last portal crossed)
     * @param hit    the block hit, in {@code level}'s coordinates
     * @param portals the portals crossed, in order
     */
    public record Trace(ServerLevel level, BlockHitResult hit, List<Portal> portals) {}

    /** The trace if the ray crosses at least one portal, otherwise {@code null} (use the normal raycast). */
    public static @Nullable Trace trace(Level level, Vec3 start, Vec3 end, ClipContext.Fluid fluid, @Nullable Entity context) {
        if (!(level instanceof ServerLevel) || !PortalSpellConfig.enabled()) return null;

        Tuple<BlockHitResult, List<Portal>> result =
                IPMcHelper.rayTrace(level, new ClipContext(start, end, ClipContext.Block.COLLIDER, fluid, context), true);
        List<Portal> portals = result.getB();
        if (portals.isEmpty() || !(portals.get(portals.size() - 1).getDestWorld() instanceof ServerLevel far)) return null;
        return new Trace(far, result.getA(), List.copyOf(portals));
    }

    /**
     * Stops a ray that would leave through a global (dimension stack) portal at the portal plane,
     * as if it were a ceiling or floor, instead of continuing into the void beyond the world edge.
     */
    public static BlockHitResult clampToPortalPlane(Level level, ClipContext context, BlockHitResult hit) {
        if (!PortalSpellConfig.enabled()) return hit;
        List<Portal> portals = GlobalPortalStorage.getGlobalPortals(level);
        if (portals.isEmpty()) return hit;

        Vec3 from = context.getFrom();
        Vec3 to = context.getTo();
        double nearest = hit.getType() == HitResult.Type.MISS ? Double.MAX_VALUE : hit.getLocation().distanceToSqr(from);
        Vec3 crossing = null;
        Portal crossed = null;
        for (Portal portal : portals) {
            if (!portal.isInFrontOfPortal(from)) continue;
            Vec3 point = portal.rayTrace(from, to);
            if (point == null) continue;
            double distance = point.distanceToSqr(from);
            if (distance < nearest) {
                nearest = distance;
                crossing = point;
                crossed = portal;
            }
        }
        if (crossing == null) return hit;

        Vec3 direction = to.subtract(from).normalize();
        Vec3 normal = crossed.getNormal();
        // The "block" hit is the last one inside the world, just before the plane.
        BlockPos pos = BlockPos.containing(crossing.subtract(direction.scale(1.0E-3)));
        return new BlockHitResult(crossing, Direction.getNearest(normal.x, normal.y, normal.z), pos, false);
    }
}