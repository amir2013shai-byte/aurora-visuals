package su.aurora.modules.misc;

import net.minecraft.client.Minecraft;
import su.aurora.core.Category;
import su.aurora.core.Module;
import su.aurora.setting.BooleanSetting;

/**
 * ScreenTweaks — чистая картинка: без виньетки и вспышек молний.
 */
public class ScreenTweaksModule extends Module {
    private final BooleanSetting noVignette = addBoolean("Без виньетки", true);
    private final BooleanSetting noLightningFlash = addBoolean("Без вспышек молний", true);
    private final BooleanSetting noBobbing = addBoolean("Без покачивания камеры", false);

    private Boolean previousVignette;
    private Boolean previousFlash;

    public ScreenTweaksModule() {
        super("ScreenTweaks", "Убирает виньетку и лишние вспышки экрана", Category.MISC);
    }

    @Override
    public void onEnable() {
        Minecraft client = Minecraft.getInstance();
        previousVignette = client.options.vignette().get();
        previousFlash = client.options.hideLightningFlash().get();
        client.options.vignette().set(!noVignette.get());
        client.options.hideLightningFlash().set(noLightningFlash.get());
    }

    @Override
    public void onDisable() {
        Minecraft client = Minecraft.getInstance();
        client.options.vignette().set(previousVignette == null || previousVignette);
        client.options.hideLightningFlash().set(previousFlash != null && previousFlash);
    }

    @Override
    public void onTick() {
        Minecraft client = Minecraft.getInstance();
        if (noBobbing.get()) {
            client.options.bobView().set(false);
        }
        if (client.options.vignette().get() != !noVignette.get()) {
            client.options.vignette().set(!noVignette.get());
        }
    }
}
