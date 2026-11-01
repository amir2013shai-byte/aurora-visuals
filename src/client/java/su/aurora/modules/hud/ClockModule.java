package su.aurora.modules.hud;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import su.aurora.core.Category;
import su.aurora.core.Module;
import su.aurora.setting.BooleanSetting;
import su.aurora.setting.ColorSetting;
import su.aurora.setting.ModeSetting;
import su.aurora.util.ColorUtil;
import su.aurora.util.Render2D;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * Clock — реальное время и время в мире (часы и номер дня).
 */
public class ClockModule extends Module {
    private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final DateTimeFormatter SHORT = DateTimeFormatter.ofPattern("HH:mm");

    private final ModeSetting position = addMode("Позиция", "Справа снизу", "Справа снизу", "Слева снизу",
            "Слева сверху", "Справа сверху");
    private final BooleanSetting realTime = addBoolean("Реальное время", true);
    private final BooleanSetting seconds = addBoolean("Секунды", false);
    private final BooleanSetting worldTime = addBoolean("Время в мире", true);
    private final BooleanSetting worldDay = addBoolean("Номер дня", true);
    private final ColorSetting accent = addColor("Акцент", 0xFF7C5CFF);

    public ClockModule() {
        super("Clock", "Реальное время и время в мире", Category.HUD);
    }

    @Override
    public void onHudRender(GuiGraphics graphics, DeltaTracker delta) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null || client.options.hideGui) {
            return;
        }
        StringBuilder text = new StringBuilder();
        if (realTime.get()) {
            text.append(LocalTime.now().format(seconds.get() ? FORMAT : SHORT));
        }
        if (worldTime.get() && client.level != null) {
            long dayTime = client.level.getDayTime() % 24000L;
            int hours = (int) ((dayTime / 1000L + 6) % 24);
            int minutes = (int) ((dayTime % 1000) * 60 / 1000);
            text.append(text.isEmpty() ? "" : "  ·  ")
                    .append(String.format(java.util.Locale.ROOT, "%02d:%02d", hours, minutes));
        }
        if (worldDay.get() && client.level != null) {
            long day = client.level.getDayTime() / 24000L + 1;
            text.append(text.isEmpty() ? "" : "  ·  ").append("день ").append(day);
        }
        if (text.isEmpty()) {
            return;
        }

        String plain = text.toString();
        int width = Render2D.width(plain) + 16;
        int screenWidth = client.getWindow().getGuiScaledWidth();
        int screenHeight = client.getWindow().getGuiScaledHeight();
        int x = switch (position.get()) {
            case "Слева сверху", "Слева снизу" -> 8;
            default -> screenWidth - width - 8;
        };
        int y = switch (position.get()) {
            case "Слева сверху", "Справа сверху" -> 8;
            default -> screenHeight - 26;
        };
        Render2D.roundedRect(graphics, x, y, width, 18, 6, 0xB30C0E14);
        Render2D.roundedRect(graphics, x, y, 3, 18, 2, HudStyleModule.resolve(accent));
        Render2D.textShadow(graphics, plain, x + 9, y + 5, ColorUtil.withAlpha(0xFFFFFFFF, 235));
    }
}
