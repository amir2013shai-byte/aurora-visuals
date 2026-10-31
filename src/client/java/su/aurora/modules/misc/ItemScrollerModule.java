package su.aurora.modules.misc;

import su.aurora.core.Category;
import su.aurora.core.Module;
import su.aurora.setting.BooleanSetting;

/**
 * ItemScroller — колесо мыши в инвентаре быстро переносит стак (shift-клик по слотам).
 */
public class ItemScrollerModule extends Module {
    private final BooleanSetting shiftOnly = addBoolean("Только с Shift", false);
    private final BooleanSetting wholeStack = addBoolean("Весь стак", true);

    public ItemScrollerModule() {
        super("ItemScroller", "Колесо мыши в инвентаре переносит предметы", Category.MISC);
    }

    public boolean shiftOnly() {
        return shiftOnly.get();
    }

    public boolean wholeStack() {
        return wholeStack.get();
    }
}
