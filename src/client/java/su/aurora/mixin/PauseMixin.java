package su.aurora.mixin;

import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import su.aurora.AuroraClient;
import su.aurora.core.Module;

/**
 * NoPause: Minecraft#isPaused всегда false, если модуль включён.
 */
@Mixin(Minecraft.class)
public class PauseMixin {
    @Inject(method = "isPaused", at = @At("RETURN"), cancellable = true)
    private void aurora$noPause(CallbackInfoReturnable<Boolean> cir) {
        if (AuroraClient.MODULES == null) {
            return;
        }
        Module module = AuroraClient.MODULES.get("NoPause");
        if (module != null && module.isEnabled()) {
            cir.setReturnValue(false);
        }
    }
}
