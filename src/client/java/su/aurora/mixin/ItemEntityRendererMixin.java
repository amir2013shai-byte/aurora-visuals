package su.aurora.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.ItemEntityRenderer;
import net.minecraft.client.renderer.entity.state.ItemEntityRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import su.aurora.AuroraClient;
import su.aurora.core.Module;
import su.aurora.modules.render.ItemPhysicsModule;

/**
 * ItemPhysics: кладёт предмет на землю плоско (поворот + подъём + масштаб).
 */
@Mixin(ItemEntityRenderer.class)
public class ItemEntityRendererMixin {
    @Inject(method = "submit", at = @At("HEAD"))
    private void aurora$itemPhysics(ItemEntityRenderState state, PoseStack poseStack, SubmitNodeCollector collector,
                                    CameraRenderState cameraState, CallbackInfo ci) {
        if (AuroraClient.MODULES == null) {
            return;
        }
        Module module = AuroraClient.MODULES.get("ItemPhysics");
        if (module instanceof ItemPhysicsModule physics && physics.isEnabled()) {
            poseStack.translate(0.0, physics.lift(), 0.0);
            poseStack.mulPose(Axis.XP.rotationDegrees(physics.rotation()));
            poseStack.scale(physics.scale(), physics.scale(), physics.scale());
        }
    }
}
