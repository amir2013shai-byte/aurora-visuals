package su.aurora.modules.hud;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.effect.MobEffectInstance;
import su.aurora.core.Category;
import su.aurora.core.Module;
import su.aurora.setting.BooleanSetting;
import su.aurora.setting.ColorSetting;
import su.aurora.setting.ModeSetting;
import su.aurora.setting.NumberSetting;
import su.aurora.util.ColorUtil;
import su.aurora.util.Render2D;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * PotionHUD — активные эффекты с таймером и уровнем.
 */
public class PotionHudModule extends Module {
    private final ModeSetting position = addMode("Позиция", "Слева сверху", "Слева сверху", "Справа сверху", "Слева снизу");
    private final NumberSetting limit = addInt("Максимум эффектов", 8, 1, 20);
    private final BooleanSetting onlyExpiring = addBoolean("Только истекающие", false);
    private final BooleanSetting showLevel = addBoolean("Показывать уровень", true);
    private final BooleanSetting colorByEffect = addBoolean("Цвет эффекта", true);
    private final ColorSetting accent = addColor("Акцент", 0xFF7C5CFF);

    public PotionHudModule() {
        super("PotionHUD", "Активные эффекты с таймером", Category.HUD);
    }

    @Override
    public void onHudRender(GuiGraphics graphics, DeltaTracker delta) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null || client.options.hideGui) {
            return;
        }
        List<MobEffectInstance> effects = new ArrayList<>(client.player.getActiveEffects());
        if (onlyExpiring.get()) {
            effects.removeIf(instance -> instance.getDuration() > 20 * 30);
        }
        if (effects.isEmpty()) {
            return;
        }
        effects.sort(Comparator.comparingInt(MobEffectInstance::getDuration));
        if (effects.size() > limit.getInt()) {
            effects = effects.subList(0, limit.getInt());
        }

        int screenWidth = client.getWindow().getGuiScaledWidth();
        int screenHeight = client.getWindow().getGuiScaledHeight();
        float width = 116;
        float rowHeight = 16;
        float x = switch (position.get()) {
            case "Справа сверху" -> screenWidth - width - 8;
            case "Слева снизу" -> 8;
            default -> 8;
        };
        float y = position.is("Слева снизу") ? screenHeight - effects.size() * (rowHeight + 2) - 90 : 34;

        for (MobEffectInstance instance : effects) {
            String name = instance.getEffect().value().getDisplayName().getString();
            String level = instance.getAmplifier() > 0 && showLevel.get()
                    ? " " + roman(instance.getAmplifier() + 1) : "";
            int seconds = instance.getDuration() / 20;
            String time = instance.getDuration() > 20 * 60
                    ? String.format(Locale.ROOT, "%d:%02d", seconds / 60, seconds % 60)
                    : String.format(Locale.ROOT, "%d с", seconds);

            int tint = colorByEffect.get()
                    ? 0xFF000000 | instance.getEffect().value().getColor()
                    : HudStyleModule.resolve(accent);

            Render2D.roundedRect(graphics, x, y, width, rowHeight, 5, 0x8C0C0E14);
            Render2D.roundedRect(graphics, x, y, 3, rowHeight, 3, tint);
            Render2D.textShadow(graphics, name + level, x + 9, y + 4, 0xFFFFFFFF);
            Render2D.textRight(graphics, time, x + width - 6, y + 4,
                    seconds <= 10 ? 0xFFFF6B6B : ColorUtil.withAlpha(0xFFFFFFFF, 190));
            y += rowHeight + 2;
        }
    }

    private String roman(int value) {
        return switch (value) {
            case 1 -> "I";
            case 2 -> "II";
            case 3 -> "III";
            case 4 -> "IV";
            case 5 -> "V";
            default -> String.valueOf(value);
        };
    }
}
