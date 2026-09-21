package com.jkdr.abyssalascentcore.mixins.compat.ironsspellbooks;

import com.jkdr.abyssalascentcore.spell.PortalRay;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.entity.spells.portal.PortalData;
import io.redspace.ironsspellbooks.entity.spells.portal.PortalEntity;
import io.redspace.ironsspellbooks.entity.spells.portal.PortalPos;
import io.redspace.ironsspellbooks.spells.ender.PortalSpell;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * The Portal spell places its portal at the block the caster aims at, in the caster's level.
 * When the aim ray crosses an Immersive Portals portal, the portal is placed on the far side, in the level the ray ended in.
 * Iron's portals already support having each end in a different dimension.
 */
@Mixin(PortalSpell.class)
public abstract class MixinPortalSpell {

    @Inject(method = "onCast", at = @At("HEAD"), remap = false)
    private void aacore$beginCast(CallbackInfo ci) {
        PortalRay.PORTAL_SPELL.remove();
    }

    @Inject(method = "onCast", at = @At("RETURN"), remap = false)
    private void aacore$endCast(CallbackInfo ci) {
        PortalRay.PORTAL_SPELL.remove();
    }

    @WrapOperation(method = "onCast", remap = false, at = @At(value = "INVOKE",
            target = "Lio/redspace/ironsspellbooks/api/util/Utils;getTargetBlock(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/level/ClipContext$Fluid;D)Lnet/minecraft/world/phys/BlockHitResult;", remap = false))
    private BlockHitResult aacore$aimThroughPortals(Level level, LivingEntity entity, ClipContext.Fluid fluid, double reach, Operation<BlockHitResult> original) {
        Vec3 start = entity.getEyePosition();
        Vec3 end = start.add(entity.getLookAngle().normalize().scale(reach));
        PortalRay.Trace trace = PortalRay.trace(level, start, end, fluid, entity);
        if (trace == null) return original.call(level, entity, fluid, reach);

        PortalRay.PORTAL_SPELL.set(trace);
        return trace.hit();
    }

    // The "drop to the floor" raycast under the aimed point.
    @WrapOperation(method = "onCast", remap = false, at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/Level;clip(Lnet/minecraft/world/level/ClipContext;)Lnet/minecraft/world/phys/BlockHitResult;", remap = true))
    private BlockHitResult aacore$clipInTargetLevel(Level level, ClipContext context, Operation<BlockHitResult> original) {
        PortalRay.Trace trace = PortalRay.PORTAL_SPELL.get();
        return original.call(trace != null ? trace.level() : level, context);
    }

    @WrapOperation(method = "onCast", remap = false, at = @At(value = "INVOKE",
            target = "Lio/redspace/ironsspellbooks/entity/spells/portal/PortalPos;of(Lnet/minecraft/resources/ResourceKey;Lnet/minecraft/world/phys/Vec3;F)Lio/redspace/ironsspellbooks/entity/spells/portal/PortalPos;", remap = false))
    private PortalPos aacore$recordTargetDimension(ResourceKey<Level> dimension, Vec3 position, float rotation, Operation<PortalPos> original) {
        PortalRay.Trace trace = PortalRay.PORTAL_SPELL.get();
        return original.call(trace != null ? trace.level().dimension() : dimension, position, rotation);
    }

    @WrapOperation(method = "onCast", remap = false, at = @At(value = "INVOKE",
            target = "Lio/redspace/ironsspellbooks/spells/ender/PortalSpell;setupPortalEntity(Lnet/minecraft/world/level/Level;Lio/redspace/ironsspellbooks/entity/spells/portal/PortalData;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/phys/Vec3;F)Lio/redspace/ironsspellbooks/entity/spells/portal/PortalEntity;", remap = false))
    private PortalEntity aacore$spawnInTargetLevel(PortalSpell spell, Level level, PortalData data, Player owner, Vec3 position, float rotation, Operation<PortalEntity> original) {
        PortalRay.Trace trace = PortalRay.PORTAL_SPELL.get();
        return original.call(spell, trace != null ? trace.level() : level, data, owner, position, rotation);
    }
}