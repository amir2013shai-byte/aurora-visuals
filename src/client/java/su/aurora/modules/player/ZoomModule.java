package su.aurora.modules.player;

import net.minecraft.client.Minecraft;
import su.aurora.core.Category;
import su.aurora.core.Module;
import su.aurora.setting.BooleanSetting;
import su.aurora.setting.NumberSetting;

/**
 * Zoom — зум через FOV. При включении запоминает текущий FOV и плавно подводит
 * к нужной кратности, при выключении возвращает как было.
 */
public class ZoomModule extends Module {
    private final NumberSetting amount = addNumber("Кратность", 4.0, 1.5, 15.0, 0.5);
    private final BooleanSetting smooth = addBoolean("Плавно", true);

    private Integer savedFov;
    private float current = 1.0f;

    public ZoomModule() {
        super("Zoom", "Зум камеры на бинд: плавное приближение и возврат", Category.PLAYER);
    }

    @Override
    public void onEnable() {
        Minecraft client = Minecraft.getInstance();
        savedFov = client.options.fov().get();
        current = 1.0f;
    }

    @Override
    public void onDisable() {
        Minecraft client = Minecraft.getInstance();
        if (savedFov != null) {
            client.options.fov().set(savedFov);
            savedFov = null;
        }
    }

    @Override
    public void onTick() {
        Minecraft client = Minecraft.getInstance();
        if (savedFov == null) {
            savedFov = client.options.fov().get();
        }
        float target = amount.getFloat();
        float speed = smooth.get() ? 0.25f : 1.0f;
        current += (target - current) * speed;
        if (Math.abs(target - current) < 0.02f) {
            current = target;
        }
        int fov = Math.max(1, Math.round(savedFov / current));
        if (client.options.fov().get() != fov) {
            client.options.fov().set(fov);
        }
    }
}
