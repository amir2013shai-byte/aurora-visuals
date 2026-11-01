package su.aurora.modules.render;

import su.aurora.core.Category;
import su.aurora.core.Module;
import su.aurora.setting.BooleanSetting;
import su.aurora.setting.NumberSetting;

/**
 * NoHurtCam — убирает тряску камеры при получении урона (GameRenderer#bobHurt).
 */
public class NoHurtCamModule extends Module {
    private final BooleanSetting keepShake = addBoolean("Оставить покачивание", false);
    private final NumberSetting strength = addNumber("Сила тряски", 0.0, 0.0, 1.0, 0.05);

    public NoHurtCamModule() {
        super("NoHurtCam", "Без тряски камеры при уроне", Category.RENDER);
    }

    public boolean keepShake() {
        return keepShake.get();
    }

    public float strength() {
        return strength.getFloat();
    }
}
