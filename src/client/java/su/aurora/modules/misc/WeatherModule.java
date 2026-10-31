package su.aurora.modules.misc;

import su.aurora.core.Category;
import su.aurora.core.Module;
import su.aurora.setting.BooleanSetting;

/**
 * Weather — управление погодой на клиенте: убрать дождь, снег и звук дождя.
 */
public class WeatherModule extends Module {
    private final BooleanSetting noRain = addBoolean("Без дождя", true);
    private final BooleanSetting noSnow = addBoolean("Без снега", true);
    private final BooleanSetting noSound = addBoolean("Без звука дождя", false);

    public WeatherModule() {
        super("Weather", "Убирает дождь и снег на клиенте", Category.MISC);
    }

    public boolean hideRain() {
        return isEnabled() && noRain.get();
    }

    public boolean hideSnow() {
        return isEnabled() && noSnow.get();
    }

    public boolean hideSound() {
        return isEnabled() && noSound.get();
    }
}
