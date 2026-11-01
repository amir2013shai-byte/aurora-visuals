package su.aurora.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import su.aurora.AuroraClient;
import su.aurora.core.Module;
import su.aurora.modules.render.NoHurtCamModule;

/**
 * NoHurtCam: гасит bobHurt (тряску камеры при уроне) или ослабляет её.
 */
@Mixin(GameRenderer.class)
public class GameRendererHurtMixin {
    @Inject(method = "bobHurt", at = @At("HEAD"), cancellable = true)
    private void aurora$noHurtCam(PoseStack poseStack, float partialTick, CallbackInfo ci) {
        if (AuroraClient.MODULES == null) {
            return;
        }
        Module module = AuroraClient.MODULES.get("NoHurtCam");
        if (!(module instanceof NoHurtCamModule hurtCam) || !hurtCam.isEnabled()) {
            return;
        }
        if (hurtCam.strength() <= 0.01f) {
            ci.cancel();
            return;
        }
        poseStack.scale(1.0f, 1.0f, hurtCam.strength());
    }
}
