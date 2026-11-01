package su.aurora.modules.hud;

import su.aurora.core.Category;
import su.aurora.core.Module;
import su.aurora.setting.BooleanSetting;
import su.aurora.setting.ColorSetting;
import su.aurora.util.ColorUtil;

/**
 * HudStyle — единый стиль HUD:
 * можно отключить индивидуальные цвета у всех окон (включая минимапу) и задать один общий акцент.
 */
public class HudStyleModule extends Module {
    private final BooleanSetting override = addBoolean("Единый цвет HUD", false);
    private final ColorSetting globalAccent = addColor("Общий акцент", 0xFF7C5CFF);
    private final BooleanSetting dimIcons = addBoolean("Приглушать подписи", false);

    public HudStyleModule() {
        super("HudStyle", "Общий цвет всех окон HUD вместо личных настроек", Category.HUD);
    }

    /**
     * Цвет для HUD-элемента: либо глобальный (если включён «Единый цвет HUD»),
     * либо собственный цвет модуля.
     */
    public static int resolve(ColorSetting own) {
        Module module = su.aurora.AuroraClient.MODULES == null ? null
                : su.aurora.AuroraClient.MODULES.get("HudStyle");
        if (module instanceof HudStyleModule style && style.isEnabled() && style.override.get()) {
            return style.globalAccent.get();
        }
        return own == null ? 0xFFFFFFFF : own.get();
    }

    /** Приглушённый вариант для мелких подписей. */
    public static int dim(int color, int alpha) {
        return ColorUtil.withAlpha(color, alpha);
    }

    public static boolean dims() {
        Module module = su.aurora.AuroraClient.MODULES == null ? null
                : su.aurora.AuroraClient.MODULES.get("HudStyle");
        return module instanceof HudStyleModule style && style.isEnabled() && style.dimIcons.get();
    }
}
