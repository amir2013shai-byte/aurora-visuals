package su.aurora.modules.misc;

import su.aurora.core.Category;
import su.aurora.core.Module;
import su.aurora.setting.BooleanSetting;
import su.aurora.setting.ColorSetting;
import su.aurora.setting.NumberSetting;

/**
 * CustomFog — собственный цвет и плотность тумана.
 * Цвет подменяется миксином в FogRenderer#setupFog.
 */
public class CustomFogModule extends Module {
    private final ColorSetting color = addColor("Цвет тумана", 0xFF101426);
    private final NumberSetting density = addNumber("Плотность", 1.0, 0.0, 4.0, 0.05);
    private final BooleanSetting onlyInWorld = addBoolean("Только в мире", true);

    public CustomFogModule() {
        super("CustomFog", "Свой цвет и плотность тумана", Category.MISC);
    }

    public int fogColor() {
        int argb = color.get();
        float factor = density.getFloat();
        int alpha = Math.min(255, (int) (((argb >>> 24) & 0xFF) * Math.max(0.05f, Math.min(factor, 1.0f))));
        return (alpha << 24) | (argb & 0xFFFFFF);
    }
}
