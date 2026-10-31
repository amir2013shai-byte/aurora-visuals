package su.aurora.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import su.aurora.AuroraClient;
import su.aurora.core.Module;
import su.aurora.modules.render.AnimationsModule;

/**
 * Animations: правит матрицу рук перед рендером предмета в руке.
 */
@Mixin(ItemInHandRenderer.class)
public class ItemInHandRendererMixin {
    @Inject(method = "renderHandsWithItems", at = @At("HEAD"))
    private void aurora$animations(float partialTick, PoseStack poseStack, SubmitNodeCollector collector,
                                   LocalPlayer player, int light, CallbackInfo ci) {
        if (AuroraClient.MODULES == null) {
            return;
        }
        Module module = AuroraClient.MODULES.get("Animations");
        if (module instanceof AnimationsModule animations && animations.isEnabled()) {
            animations.apply(poseStack);
        }
    }
}
