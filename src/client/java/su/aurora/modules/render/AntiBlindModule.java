package su.aurora.modules.render;

import su.aurora.core.Category;
import su.aurora.core.Module;
import su.aurora.setting.BooleanSetting;

/**
 * AntiBlind — гасит визуальные эффекты слепоты, темноты и тошноты.
 * Работает через LivingEntity#getEffectBlendFactor (это чисто рендер).
 */
public class AntiBlindModule extends Module {
    private final BooleanSetting blindness = addBoolean("Слепота", true);
    private final BooleanSetting darkness = addBoolean("Темнота", true);
    private final BooleanSetting nausea = addBoolean("Тошнота", true);

    public AntiBlindModule() {
        super("AntiBlind", "Без слепоты, темноты и тошноты", Category.RENDER);
    }

    public boolean blindness() {
        return blindness.get();
    }

    public boolean darkness() {
        return darkness.get();
    }

    public boolean nausea() {
        return nausea.get();
    }
}
