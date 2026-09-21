package com.jkdr.abyssalascentcore.mixins.compat.immptl;

import com.jkdr.abyssalascentcore.compat.AircraftTilt;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import qouteall.imm_ptl.core.render.context_management.WorldRenderInfo;

/**
 * The view of the other dimension seen through a portal is built by Immersive Portals from a fresh matrix,
 * discarding whatever rotation the game had already put on it. While the player is riding anything (a horse, a boat, an aircraft)
 * the third person view comes out with no rotation at all, facing north, and an Immersive Aircraft's own tilt,
 * which it adds to the view, is missing from it too.
 * When the rotation has been lost and the player is riding, the camera's rotation is put back,
 * along with the tilt of an Immersive Aircraft, the same way Immersive Aircraft puts it on the view in the dimension the player is in.
 * <p>
 * THIS DOES NOT WORK AT THE MOMENT
 */
@Mixin(value = WorldRenderInfo.class, remap = false)
public abstract class MixinWorldRenderInfoAircraftTilt {

    @Inject(method = "applyAdditionalTransformations", at = @At("TAIL"), remap = false)
    private static void aacore$restoreRiderView(PoseStack poseStack, CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.player.getVehicle() == null) return;

        Camera camera = mc.gameRenderer.getMainCamera();
        Matrix4f pose = poseStack.last().pose();

        // With the camera's rotation on the view, the direction it looks in maps onto straight ahead.
        Vector3f look = new Vector3f(camera.getLookVector()).mulDirection(pose);
        if (look.z() < -0.99F) return;

        Entity vehicle = mc.player.getRootVehicle();
        float partialTick = mc.getFrameTime();
        Matrix4f view = new Matrix4f();
        AircraftTilt.apply(vehicle, partialTick, view);
        view.rotateX((float) Math.toRadians(camera.getXRot()));
        view.rotateY((float) Math.toRadians(camera.getYRot() + 180.0F));

        pose.set(view.mul(pose));
    }
}
