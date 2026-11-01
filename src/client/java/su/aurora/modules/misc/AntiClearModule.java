package su.aurora.modules.misc;

import su.aurora.core.Category;
import su.aurora.core.Module;

/**
 * AntiClear — не даёт серверу/игре очищать историю чата (ChatComponent#clearMessages).
 */
public class AntiClearModule extends Module {
    public AntiClearModule() {
        super("AntiClear", "Чат не очищается по команде сервера", Category.MISC);
    }
}
