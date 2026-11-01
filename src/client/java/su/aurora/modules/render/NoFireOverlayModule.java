package su.aurora.modules.render;

import su.aurora.core.Category;
import su.aurora.core.Module;

/**
 * NoFireOverlay — не заслонять экран огнём (ScreenEffectRenderer#renderFire).
 */
public class NoFireOverlayModule extends Module {
    public NoFireOverlayModule() {
        super("NoFireOverlay", "Без оверлея огня на экране", Category.RENDER);
    }
}
