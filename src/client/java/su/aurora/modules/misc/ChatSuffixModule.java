package su.aurora.modules.misc;

import su.aurora.core.Category;
import su.aurora.core.Module;
import su.aurora.setting.BooleanSetting;
import su.aurora.setting.StringSetting;

/**
 * ChatSuffix — свой префикс и суффикс у исходящих сообщений (не команд).
 * Применяется в ChatMixin через @ModifyVariable.
 */
public class ChatSuffixModule extends Module {
    private final StringSetting prefix = addString("Префикс", "");
    private final StringSetting suffix = addString("Суффикс", " ᴀᴜʀᴏʀᴀ");
    private final BooleanSetting commandsToo = addBoolean("И к командам", false);

    public ChatSuffixModule() {
        super("ChatSuffix", "Свой префикс и суффикс у сообщений", Category.MISC);
    }

    /** Применить префикс/суффикс к исходящему сообщению. */
    public String apply(String message, boolean command) {
        if (message == null || message.isBlank()) {
            return message;
        }
        if (command && !commandsToo.get()) {
            return message;
        }
        String pre = prefix.get() == null ? "" : prefix.get();
        String suf = suffix.get() == null ? "" : suffix.get();
        return pre + message + suf;
    }
}
