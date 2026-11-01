package su.aurora.mixin;

import net.minecraft.client.GuiMessageTag;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MessageSignature;
import net.minecraft.sounds.SoundEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import su.aurora.AuroraClient;
import su.aurora.core.Module;
import su.aurora.modules.misc.ChatMentionModule;

import java.util.Locale;

/**
 * ChatMention: звук, когда в чате упомянут ник или своё слово.
 */
@Mixin(ChatComponent.class)
public class ChatMessageMixin {
    @Inject(method = "addMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/MessageSignature;Lnet/minecraft/client/GuiMessageTag;)V",
            at = @At("HEAD"))
    private void aurora$mention(Component message, MessageSignature signature, GuiMessageTag tag, CallbackInfo ci) {
        if (AuroraClient.MODULES == null || message == null) {
            return;
        }
        Module module = AuroraClient.MODULES.get("ChatMention");
        if (!(module instanceof ChatMentionModule mention) || !mention.isEnabled() || !mention.sound()) {
            return;
        }
        Minecraft client = Minecraft.getInstance();
        if (client.player == null) {
            return;
        }
        String text = message.getString().toLowerCase(Locale.ROOT);
        if (text.contains(client.player.getGameProfile().name().toLowerCase(Locale.ROOT))) {
            client.player.playSound(SoundEvents.NOTE_BLOCK_PLING.value(), mention.volume(), 2.0f);
            return;
        }
        String extra = mention.extra();
        if (extra != null && !extra.isBlank()) {
            for (String word : extra.split("[,;\\s]+")) {
                if (!word.isBlank() && text.contains(word.toLowerCase(Locale.ROOT))) {
                    client.player.playSound(SoundEvents.NOTE_BLOCK_BELL.value(), mention.volume(), 1.8f);
                    return;
                }
            }
        }
    }
}
