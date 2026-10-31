package su.aurora.mixin;

import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.fog.FogRenderer;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import su.aurora.AuroraClient;
import su.aurora.core.Module;
import su.aurora.modules.misc.CustomFogModule;
import su.aurora.util.ColorUtil;

/**
 * CustomFog: подменяет цвет тумана в конце расчёта FogRenderer#setupFog.
 */
@Mixin(FogRenderer.class)
public class FogRendererMixin {
    @Inject(method = "setupFog", at = @At("RETURN"), cancellable = true)
    private void aurora$setupFog(Camera camera, int renderDistance, DeltaTracker deltaTracker, float partialTick,
                                 ClientLevel level, CallbackInfoReturnable<Vector4f> cir) {
        if (AuroraClient.MODULES == null) {
            return;
        }
        Module module = AuroraClient.MODULES.get("CustomFog");
        if (!(module instanceof CustomFogModule fog) || !fog.isEnabled()) {
            return;
        }
        int color = fog.fogColor();
        cir.setReturnValue(new Vector4f(
                ColorUtil.red(color) / 255.0f,
                ColorUtil.green(color) / 255.0f,
                ColorUtil.blue(color) / 255.0f,
                1.0f));
    }
}
