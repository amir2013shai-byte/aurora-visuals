package su.aurora.modules.misc;

import net.minecraft.client.Minecraft;
import su.aurora.AuroraClient;
import su.aurora.core.Category;
import su.aurora.core.Module;
import su.aurora.setting.BooleanSetting;
import su.aurora.setting.StringSetting;

/**
 * BindCommand — модуль-триггер: нажал бинд — отправилась команда.
 */
public class BindCommandModule extends Module {
    private final StringSetting command = addString("Команда", "/home");
    private final BooleanSetting autoDisable = addBoolean("Отключаться после", true);
    private final BooleanSetting asChat = addBoolean("Как сообщение", false);

    public BindCommandModule() {
        super("BindCommand", "Отправляет команду или сообщение по бинду", Category.MISC);
    }

    @Override
    public void onEnable() {
        Minecraft client = Minecraft.getInstance();
        String text = command.get().trim();
        if (client.getConnection() == null || text.isEmpty()) {
            return;
        }
        if (asChat.get()) {
            client.getConnection().sendChat(text);
        } else {
            client.getConnection().sendCommand(text.replaceFirst("^/", ""));
        }
        AuroraClient.NOTIFICATIONS.push("BindCommand", text, 0xFF7CF7B0, 1500L);
        if (autoDisable.get()) {
            setEnabled(false);
        }
    }
}
