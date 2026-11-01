package su.aurora.modules.misc;

import su.aurora.core.Category;
import su.aurora.core.Module;
import su.aurora.setting.BooleanSetting;
import su.aurora.setting.NumberSetting;
import su.aurora.setting.StringSetting;

/**
 * ChatMention — звук и подсветка, когда в чате упоминают твой ник (или свои слова).
 */
public class ChatMentionModule extends Module {
    private final BooleanSetting ownName = addBoolean("Мой ник", true);
    private final StringSetting extra = addString("Свои слова", "");
    private final BooleanSetting sound = addBoolean("Звук", true);
    private final NumberSetting volume = addNumber("Громкость", 0.9, 0.1, 2.0, 0.1);
    private final BooleanSetting highlight = addBoolean("Подсветка в чате", true);

    public ChatMentionModule() {
        super("ChatMention", "Уведомление при упоминании в чате", Category.MISC);
    }

    public boolean ownName() {
        return ownName.get();
    }

    public String extra() {
        return extra.get();
    }

    public boolean sound() {
        return sound.get();
    }

    public float volume() {
        return volume.getFloat();
    }

    public boolean highlight() {
        return highlight.get();
    }
}
