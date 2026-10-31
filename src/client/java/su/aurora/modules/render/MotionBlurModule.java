package su.aurora.modules.render;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import su.aurora.Aurora;
import su.aurora.core.Category;
import su.aurora.core.Module;
import su.aurora.mixin.GameRendererAccessor;
import su.aurora.setting.ModeSetting;

/**
 * MotionBlur — смаз картинки через пост-эффект (post_effect/motion_blur_*.json,
 * шейдер minecraft:post/box_blur). Сила выбирается режимом: у каждого своя цепочка.
 *
 * Ограничение: пост-эффект статичный, он не учитывает скорость камеры —
 * для «честного» motion blur нужен апдейт uniform'ов каждый кадр.
 */
public class MotionBlurModule extends Module {
    private final ModeSetting strength = addMode("Сила", "Средний", "Лёгкий", "Средний", "Сильный");

    public MotionBlurModule() {
        super("MotionBlur", "Смаз картинки при движении (пост-эффект)", Category.RENDER);
    }

    private Identifier chainId() {
        return switch (strength.get()) {
            case "Лёгкий" -> Aurora.id("motion_blur_light");
            case "Сильный" -> Aurora.id("motion_blur_strong");
            default -> Aurora.id("motion_blur_medium");
        };
    }

    @Override
    public void onEnable() {
        apply();
    }

    @Override
    public void onDisable() {
        try {
            Minecraft.getInstance().gameRenderer.clearPostEffect();
        } catch (Throwable ignored) {
        }
    }

    @Override
    public void onTick() {
        apply();
    }

    private void apply() {
        Minecraft client = Minecraft.getInstance();
        try {
            Identifier wanted = chainId();
            if (!wanted.equals(client.gameRenderer.currentPostEffect())) {
                ((GameRendererAccessor) client.gameRenderer).aurora$setPostEffect(wanted);
            }
        } catch (Throwable throwable) {
            Aurora.LOG.warn("[MotionBlur] пост-эффект недоступен: {}", throwable.toString());
        }
    }
}
