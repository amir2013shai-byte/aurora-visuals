package su.aurora.mixin;

import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import su.aurora.AuroraClient;
import su.aurora.core.Module;

/**
 * NoTotemAnim: не показывает анимацию активации предмета (тотем) на весь экран.
 */
@Mixin(GameRenderer.class)
public class TotemMixin {
    @Inject(method = "displayItemActivation", at = @At("HEAD"), cancellable = true)
    private void aurora$noTotem(ItemStack stack, CallbackInfo ci) {
        if (AuroraClient.MODULES == null) {
            return;
        }
        Module module = AuroraClient.MODULES.get("NoTotemAnim");
        if (module != null && module.isEnabled()) {
            ci.cancel();
        }
    }
}
