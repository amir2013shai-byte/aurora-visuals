package su.aurora.modules.hud;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.LivingEntity;
import su.aurora.core.Category;
import su.aurora.core.Module;
import su.aurora.setting.BooleanSetting;
import su.aurora.setting.ColorSetting;
import su.aurora.setting.ModeSetting;
import su.aurora.setting.NumberSetting;
import su.aurora.util.ColorUtil;
import su.aurora.util.PlayerUtil;
import su.aurora.util.Render2D;

/**
 * TargetHud — карточка цели: имя, здоровье, дистанция.
 */
public class TargetHudModule extends Module {
    private final ModeSetting position = addMode("Позиция", "Снизу слева", "Снизу слева", "Центр", "Снизу справа");
    private final NumberSetting range = addNumber("Дистанция", 16, 3, 64, 1);
    private final BooleanSetting showHealthBar = addBoolean("Полоса здоровья", true);
    private final BooleanSetting showAvatar = addBoolean("Аватар", true);
    private final ColorSetting accent = addColor("Акцент", 0xFFFF5C7C);

    public TargetHudModule() {
        super("TargetHud", "Информация о текущей цели", Category.HUD);
        setEnabled(true);
    }

    @Override
    public void onHudRender(GuiGraphics graphics, DeltaTracker delta) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null || client.options.hideGui) {
            return;
        }
        LivingEntity target = PlayerUtil.crosshairTarget(range.get());
        if (target == null) {
            return;
        }
        String name = target.getName().getString();
        float percent = PlayerUtil.healthPercent(target);
        float hp = target.getHealth();
        double distance = target.distanceTo(client.player);

        boolean avatar = showAvatar.get() && target instanceof net.minecraft.client.player.AbstractClientPlayer;
        float avatarSize = 26;
        float width = avatar ? 176 : 148;
        float height = (showHealthBar.get() ? 44 : 30) + (avatar ? 4 : 0);
        int screenWidth = client.getWindow().getGuiScaledWidth();
        int screenHeight = client.getWindow().getGuiScaledHeight();
        float x = switch (position.get()) {
            case "Центр" -> (screenWidth - width) / 2f;
            case "Снизу справа" -> screenWidth - width - 8;
            default -> 8;
        };
        float y = screenHeight - height - 30;

        Render2D.roundedRect(graphics, x, y, width, height, 7, 0xB30C0E14);
        Render2D.roundedRect(graphics, x, y, 3, height, 3, HudStyleModule.resolve(accent));
        float textX = x + 10;
        if (avatar) {
            net.minecraft.client.player.AbstractClientPlayer skinPlayer =
                    (net.minecraft.client.player.AbstractClientPlayer) target;
            Render2D.roundedRect(graphics, x + 7, y + 6, avatarSize + 2, avatarSize + 2, 5, 0x66000000);
            net.minecraft.client.gui.components.PlayerFaceRenderer.draw(graphics,
                    skinPlayer.getSkin(), (int) (x + 8), (int) (y + 7), (int) avatarSize);
            textX = x + 8 + avatarSize + 8;
        }
        Render2D.textShadow(graphics, name, textX, y + 7, 0xFFFFFFFF);
        String info = String.format(java.util.Locale.ROOT, "%.1f HP · %.0f m", hp, distance);
        Render2D.textRight(graphics, info, x + width - 8, y + 7, 0xFF9AA4B2);

        if (showHealthBar.get()) {
            float barX = textX;
            float barY = y + 24;
            float barWidth = x + width - 10 - textX;
            Render2D.roundedRect(graphics, barX, barY, barWidth, 8, 4, 0x40FFFFFF);
            int healthColor = ColorUtil.lerp(0xFFFF4D4D, 0xFF7CF7B0, percent);
            Render2D.roundedRect(graphics, barX, barY, Math.max(6, barWidth * percent), 8, 4, healthColor);
        }
    }
}
