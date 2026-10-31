package su.aurora.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EndCrystalRenderer;
import net.minecraft.client.renderer.entity.state.EndCrystalRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import su.aurora.AuroraClient;
import su.aurora.core.Module;
import su.aurora.modules.render.CrystalColorModule;

/**
 * CrystalColor: гасит ванильный рендер кристалла, если он перекрашивается модулем.
 */
@Mixin(EndCrystalRenderer.class)
public class EndCrystalRendererMixin {
    @Inject(method = "submit", at = @At("HEAD"), cancellable = true)
    private void aurora$crystalColor(EndCrystalRenderState state, PoseStack poseStack,
                                     SubmitNodeCollector collector, CameraRenderState cameraState, CallbackInfo ci) {
        if (AuroraClient.MODULES == null) {
            return;
        }
        Module module = AuroraClient.MODULES.get("CrystalColor");
        if (module instanceof CrystalColorModule crystalColor && crystalColor.hideVanillaCrystal()) {
            ci.cancel();
        }
    }
}
