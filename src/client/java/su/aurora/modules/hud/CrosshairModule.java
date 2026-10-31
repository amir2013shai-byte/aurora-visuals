package su.aurora.modules.hud;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import su.aurora.core.Category;
import su.aurora.core.Module;
import su.aurora.setting.BooleanSetting;
import su.aurora.setting.ColorSetting;
import su.aurora.setting.ModeSetting;
import su.aurora.setting.NumberSetting;
import su.aurora.util.ColorUtil;
import su.aurora.util.Render2D;

/**
 * Crosshair — свой прицел: крест, круг, точка, ромб.
 */
public class CrosshairModule extends Module {
    private final ModeSetting style = addMode("Вид", "Крест", "Крест", "Круг", "Точка", "Ромб", "Крест + круг");
    private final ColorSetting color = addColor("Цвет", 0xFFFFFFFF);
    private final NumberSetting size = addNumber("Размер", 6, 2, 20, 0.5);
    private final NumberSetting gap = addNumber("Зазор", 3, 0, 10, 0.5);
    private final NumberSetting thickness = addNumber("Толщина", 1.5, 1, 4, 0.5);
    private final BooleanSetting outline = addBoolean("Обводка", true);
    private final BooleanSetting onlyInGame = addBoolean("Только в игре", true);

    public CrosshairModule() {
        super("Crosshair", "Свой прицел на экране", Category.HUD);
    }

    @Override
    public void onHudRender(GuiGraphics graphics, DeltaTracker delta) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null || client.options.hideGui) {
            return;
        }
        if (onlyInGame.get() && client.screen != null) {
            return;
        }

        float centerX = client.getWindow().getGuiScaledWidth() / 2f;
        float centerY = client.getWindow().getGuiScaledHeight() / 2f;
        float half = size.getFloat();
        float thick = thickness.getFloat();
        int shade = 0x99000000;

        if (outline.get()) {
            draw(graphics, centerX, centerY, half + 1, gap.getFloat(), thick + 1, shade);
        }
        draw(graphics, centerX, centerY, half, gap.getFloat(), thick, color.get());
    }

    private void draw(GuiGraphics graphics, float cx, float cy, float half, float gap, float thick, int color) {
        float g = gap;
        float t = thick;
        switch (style.get()) {
            case "Круг" -> circle(graphics, cx, cy, half + g, t, color);
            case "Точка" -> Render2D.rect(graphics, cx - t, cy - t, t * 2, t * 2, color);
            case "Ромб" -> {
                for (int i = 0; i < 4; i++) {
                    double angle = Math.PI / 2 * i;
                    float x1 = (float) (cx + Math.cos(angle) * g);
                    float y1 = (float) (cy + Math.sin(angle) * g);
                    float x2 = (float) (cx + Math.cos(angle) * half);
                    float y2 = (float) (cy + Math.sin(angle) * half);
                    line(graphics, x1, y1, x2, y2, t, color);
                }
            }
            case "Крест + круг" -> {
                drawCross(graphics, cx, cy, half, g, t, color);
                circle(graphics, cx, cy, half + g + 2, Math.max(1, t - 0.5f),
                        ColorUtil.withAlpha(color, ColorUtil.alpha(color) / 2));
            }
            default -> drawCross(graphics, cx, cy, half, g, t, color);
        }
    }

    private void drawCross(GuiGraphics graphics, float cx, float cy, float half, float gap, float thick, int color) {
        Render2D.rect(graphics, cx - half, cy - thick / 2, half - gap, thick, color);
        Render2D.rect(graphics, cx + gap, cy - thick / 2, half - gap, thick, color);
        Render2D.rect(graphics, cx - thick / 2, cy - half, thick, half - gap, color);
        Render2D.rect(graphics, cx - thick / 2, cy + gap, thick, half - gap, color);
    }

    private void circle(GuiGraphics graphics, float cx, float cy, float radius, float thick, int color) {
        int segments = 40;
        for (int i = 0; i < segments; i++) {
            double a1 = Math.PI * 2 * i / segments;
            double a2 = Math.PI * 2 * (i + 1) / segments;
            line(graphics, (float) (cx + Math.cos(a1) * radius), (float) (cy + Math.sin(a1) * radius),
                    (float) (cx + Math.cos(a2) * radius), (float) (cy + Math.sin(a2) * radius), thick, color);
        }
    }

    /** Толстая линия из мелких прямоугольников — GuiGraphics не умеет рисовать GL-линии. */
    private void line(GuiGraphics graphics, float x1, float y1, float x2, float y2, float thick, int color) {
        float dx = x2 - x1;
        float dy = y2 - y1;
        int steps = (int) Math.max(1, Math.ceil(Math.max(Math.abs(dx), Math.abs(dy))));
        for (int i = 0; i <= steps; i++) {
            float px = x1 + dx * i / steps;
            float py = y1 + dy * i / steps;
            Render2D.rect(graphics, px - thick / 2, py - thick / 2, thick, thick, color);
        }
    }
}
