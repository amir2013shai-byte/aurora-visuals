package su.aurora.mixin;

import net.minecraft.client.multiplayer.ClientPacketListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import su.aurora.AuroraClient;
import su.aurora.core.Module;
import su.aurora.modules.misc.ChatSuffixModule;
import su.aurora.chat.CommandManager;

/**
 * Чат-клиент: сообщения с префиксом «.» выполняются локально и не уходят на сервер.
 */
@Mixin(ClientPacketListener.class)
public class ChatMixin {
    @ModifyVariable(method = "sendChat", at = @At("HEAD"), argsOnly = true)
    private String aurora$suffix(String message) {
        if (AuroraClient.MODULES == null || message == null) {
            return message;
        }
        Module module = AuroraClient.MODULES.get("ChatSuffix");
        if (module instanceof ChatSuffixModule suffix && suffix.isEnabled()) {
            boolean command = message.startsWith("/") || message.startsWith(".");
            return suffix.apply(message, command);
        }
        return message;
    }

    @Inject(method = "sendChat", at = @At("HEAD"), cancellable = true)
    private void aurora$command(String message, CallbackInfo ci) {
        if (CommandManager.handle(message)) {
            ci.cancel();
        }
    }
}
