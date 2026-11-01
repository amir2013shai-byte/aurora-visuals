package su.aurora.modules.hud;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import su.aurora.core.Category;
import su.aurora.core.Module;
import su.aurora.setting.ColorSetting;
import su.aurora.setting.ModeSetting;
import su.aurora.setting.NumberSetting;
import su.aurora.util.ColorUtil;
import su.aurora.util.Render2D;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * FpsGraph — график FPS за последние несколько секунд.
 */
public class FpsGraphModule extends Module {
    private final ModeSetting position = addMode("Позиция", "Слева сверху", "Слева сверху", "Справа сверху",
            "Слева снизу", "Справа снизу");
    private final NumberSetting width = addNumber("Ширина", 110, 60, 260, 5);
    private final NumberSetting height = addNumber("Высота", 40, 20, 120, 2);
    private final NumberSetting samples = addNumber("Точек", 60, 20, 200, 5);
    private final ColorSetting accent = addColor("Цвет", 0xFF7C5CFF);

    private final Deque<Integer> history = new ArrayDeque<>();

    public FpsGraphModule() {
        super("FpsGraph", "График FPS за последние секунды", Category.HUD);
    }

    @Override
    public void onTick() {
        Minecraft client = Minecraft.getInstance();
        history.addLast(client.getFps());
        while (history.size() > samples.getInt()) {
            history.removeFirst();
        }
    }

    @Override
    public void onHudRender(GuiGraphics graphics, DeltaTracker delta) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null || client.options.hideGui || history.isEmpty()) {
            return;
        }
        int w = width.getInt();
        int h = height.getInt();
        int screenWidth = client.getWindow().getGuiScaledWidth();
        int screenHeight = client.getWindow().getGuiScaledHeight();
        int x = switch (position.get()) {
            case "Справа сверху", "Справа снизу" -> screenWidth - w - 8;
            default -> 8;
        };
        int y = switch (position.get()) {
            case "Слева снизу", "Справа снизу" -> screenHeight - h - 8;
            default -> 8;
        };

        Render2D.roundedRect(graphics, x, y, w, h, 6, 0xB30C0E14);
        int max = 1;
        for (int fps : history) {
            max = Math.max(max, fps);
        }
        int count = history.size();
        int index = 0;
        int previousX = x + 4;
        int previousY = y + h - 4;
        for (int fps : history) {
            int px = x + 4 + (int) ((w - 8) * (index / (float) Math.max(1, count - 1)));
            int py = y + h - 4 - (int) ((h - 16) * (fps / (float) max));
            graphics.fill(previousX, previousY, px + 1, py + 1, ColorUtil.withAlpha(accent.get(), 150));
            graphics.fill(px, py - 1, px + 1, py + 1, ColorUtil.withAlpha(accent.get(), 230));
            previousX = px;
            previousY = py;
            index++;
        }
        Render2D.textShadow(graphics, "FPS " + client.getFps(), x + 6, y + 5,
                ColorUtil.withAlpha(0xFFFFFFFF, 235));
    }
}
