package su.aurora.modules.misc;

import net.minecraft.client.Minecraft;
import su.aurora.core.Category;
import su.aurora.core.Module;
import su.aurora.setting.NumberSetting;

/**
 * Fullbright — полная яркость мира без зелий и факелов.
 */
public class FullbrightModule extends Module {
    private final NumberSetting brightness = addNumber("Яркость", 1.0, 0.1, 1.0, 0.05);

    private Double previousGamma;

    public FullbrightModule() {
        super("Fullbright", "Полная яркость мира", Category.MISC);
    }

    @Override
    public void onEnable() {
        Minecraft client = Minecraft.getInstance();
        previousGamma = client.options.gamma().get();
        client.options.gamma().set(brightness.get());
    }

    @Override
    public void onDisable() {
        Minecraft client = Minecraft.getInstance();
        client.options.gamma().set(previousGamma == null ? 0.5 : previousGamma);
    }

    @Override
    public void onTick() {
        Minecraft client = Minecraft.getInstance();
        if (client.options.gamma().get() < brightness.get() - 0.01) {
            client.options.gamma().set(brightness.get());
        }
    }
}
