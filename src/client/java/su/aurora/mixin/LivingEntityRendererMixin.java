package su.aurora.mixin;

import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import su.aurora.AuroraClient;
import su.aurora.core.Module;
import su.aurora.modules.render.HitColorModule;

/**
 * HitColor: подменяет тинт модели в момент вспышки урона (state.hasRedOverlay).
 */
@Mixin(LivingEntityRenderer.class)
public class LivingEntityRendererMixin {
    @Inject(method = "getModelTint", at = @At("RETURN"), cancellable = true)
    private void aurora$hitColor(LivingEntityRenderState state, CallbackInfoReturnable<Integer> cir) {
        if (AuroraClient.MODULES == null || !state.hasRedOverlay) {
            return;
        }
        Module module = AuroraClient.MODULES.get("HitColor");
        if (module instanceof HitColorModule hitColor && hitColor.tintModel()) {
            cir.setReturnValue(hitColor.tintColor());
        }
    }
}
