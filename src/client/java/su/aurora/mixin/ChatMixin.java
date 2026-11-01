package su.aurora.mixin;

import net.minecraft.client.multiplayer.ClientPacketListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import su.aurora.chat.CommandManager;

/**
 * Чат-клиент: сообщения с префиксом «.» выполняются локально и не уходят на сервер.
 */
@Mixin(ClientPacketListener.class)
public class ChatMixin {
    @Inject(method = "sendChat", at = @At("HEAD"), cancellable = true)
    private void aurora$command(String message, CallbackInfo ci) {
        if (CommandManager.handle(message)) {
            ci.cancel();
        }
    }
}
