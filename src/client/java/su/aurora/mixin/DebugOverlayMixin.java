package su.aurora.mixin;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.DebugScreenOverlay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import su.aurora.AuroraClient;
import su.aurora.core.Module;
import su.aurora.modules.hud.BetterDebugModule;

/**
 * BetterDebug: прячет ванильный F3, когда включён свой оверлей.
 */
@Mixin(DebugScreenOverlay.class)
public class DebugOverlayMixin {
    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void aurora$betterDebug(GuiGraphics graphics, CallbackInfo ci) {
        if (AuroraClient.MODULES == null) {
            return;
        }
        Module module = AuroraClient.MODULES.get("BetterDebug");
        if (module instanceof BetterDebugModule debug && debug.hideVanillaOverlay()) {
            ci.cancel();
        }
    }
}
