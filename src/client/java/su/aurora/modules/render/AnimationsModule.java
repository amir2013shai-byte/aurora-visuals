package su.aurora.modules.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import su.aurora.core.Category;
import su.aurora.core.Module;
import su.aurora.setting.BooleanSetting;
import su.aurora.setting.NumberSetting;

/**
 * Animations — положение и размер предмета в руке (миксин в ItemInHandRenderer).
 */
public class AnimationsModule extends Module {
    private final NumberSetting scale = addNumber("Масштаб", 1.0, 0.4, 1.6, 0.05);
    private final NumberSetting tiltX = addNumber("Наклон", 0.0, -45.0, 45.0, 1.0);
    private final NumberSetting offsetForward = addNumber("Сдвиг вперёд", 0.0, -0.4, 0.4, 0.02);
    private final NumberSetting offsetDown = addNumber("Сдвиг вниз", 0.0, -0.4, 0.4, 0.02);
    private final NumberSetting offsetSide = addNumber("Сдвиг вбок", 0.0, -0.4, 0.4, 0.02);
    private final BooleanSetting smooth = addBoolean("Плавная интерполяция", true);

    private float currentScale = 1.0f;
    private float currentTilt;

    public AnimationsModule() {
        super("Animations", "Масштаб, наклон и сдвиг предмета в руке", Category.RENDER);
    }

    @Override
    public void onTick() {
        float targetScale = scale.getFloat();
        float targetTilt = tiltX.getFloat();
        if (smooth.get()) {
            currentScale += (targetScale - currentScale) * 0.2f;
            currentTilt += (targetTilt - currentTilt) * 0.2f;
        } else {
            currentScale = targetScale;
            currentTilt = targetTilt;
        }
    }

    /** Применяется из ItemInHandRendererMixin к матрице рук. */
    public void apply(PoseStack poseStack) {
        poseStack.scale(currentScale, currentScale, currentScale);
        poseStack.translate(offsetSide.getFloat(), offsetDown.getFloat(), offsetForward.getFloat());
        if (Math.abs(currentTilt) > 0.01f) {
            poseStack.mulPose(Axis.XP.rotationDegrees(currentTilt));
        }
    }
}
