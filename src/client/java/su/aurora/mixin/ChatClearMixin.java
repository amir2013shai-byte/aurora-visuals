package su.aurora.mixin;

import net.minecraft.client.gui.components.ChatComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import su.aurora.AuroraClient;
import su.aurora.core.Module;

/**
 * AntiClear: не даёт очистить чат.
 */
@Mixin(ChatComponent.class)
public class ChatClearMixin {
    @Inject(method = "clearMessages", at = @At("HEAD"), cancellable = true)
    private void aurora$antiClear(boolean clearHistory, CallbackInfo ci) {
        if (AuroraClient.MODULES == null) {
            return;
        }
        Module module = AuroraClient.MODULES.get("AntiClear");
        if (module != null && module.isEnabled()) {
            ci.cancel();
        }
    }
}
