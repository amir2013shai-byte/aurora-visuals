package su.aurora.modules.render;

import su.aurora.core.Category;
import su.aurora.core.Module;

/**
 * NoTotemAnim — без гигантского тотема на весь экран (GameRenderer#displayItemActivation).
 */
public class NoTotemAnimModule extends Module {
    public NoTotemAnimModule() {
        super("NoTotemAnim", "Без анимации тотема на весь экран", Category.RENDER);
    }
}
