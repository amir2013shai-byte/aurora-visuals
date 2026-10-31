package su.aurora.modules.misc;

import su.aurora.core.Category;
import su.aurora.core.Module;

/**
 * NoPause — одиночная игра не встаёт на паузу, когда открыто меню.
 * Работает через PauseMixin (Minecraft#isPaused).
 */
public class NoPauseModule extends Module {
    public NoPauseModule() {
        super("NoPause", "Игра не встаёт на паузу при открытом меню (одиночная)", Category.MISC);
    }
}
