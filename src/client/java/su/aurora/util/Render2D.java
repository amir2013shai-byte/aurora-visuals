package su.aurora.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

public final class Render2D {
    private Render2D() {
    }

    public static Font font() {
        return Minecraft.getInstance().font;
    }

    public static void rect(GuiGraphics graphics, float x, float y, float width, float height, int color) {
        graphics.fill((int) x, (int) y, (int) (x + width), (int) (y + height), color);
    }

    public static void verticalGradient(GuiGraphics graphics, float x, float y, float width, float height,
                                        int top, int bottom) {
        graphics.fillGradient((int) x, (int) y, (int) (x + width), (int) (y + height), top, bottom);
    }

    /** Скруглённый прямоугольник, собранный из горизонтальных строк. */
    public static void roundedRect(GuiGraphics graphics, float x, float y, float width, float height, float radius,
                                   int color) {
        if (width <= 0 || height <= 0) {
            return;
        }
        float r = Math.min(radius, Math.min(width, height) / 2f);
        int innerHeight = (int) (height - r * 2);
        graphics.fill((int) x, (int) (y + r), (int) (x + width), (int) (y + height - r), color);
        for (int i = 0; i < (int) r; i++) {
            double offset = r - Math.sqrt(Math.max(0, r * r - (r - i) * (r - i)));
            int left = (int) (x + offset);
            int right = (int) (x + width - offset);
            graphics.fill(left, (int) (y + i), right, (int) (y + i + 1), color);
            graphics.fill(left, (int) (y + height - i - 1), right, (int) (y + height - i), color);
        }
        if (innerHeight <= 0) {
            graphics.fill((int) x, (int) y, (int) (x + width), (int) (y + height), color);
        }
    }

    public static void roundedOutline(GuiGraphics graphics, float x, float y, float width, float height, float radius,
                                      int color, float thickness) {
        roundedRect(graphics, x, y, width, height, radius, color);
        int innerColor = 0x00000000;
        roundedRect(graphics, x + thickness, y + thickness, width - thickness * 2, height - thickness * 2,
                Math.max(0, radius - thickness), innerColor);
    }

    public static void text(GuiGraphics graphics, String text, float x, float y, int color) {
        graphics.drawString(font(), text, (int) x, (int) y, color, false);
    }

    public static void textShadow(GuiGraphics graphics, String text, float x, float y, int color) {
        graphics.drawString(font(), text, (int) x, (int) y, color, true);
    }

    public static void textCentered(GuiGraphics graphics, String text, float centerX, float y, int color) {
        graphics.drawString(font(), text, (int) (centerX - font().width(text) / 2f), (int) y, color, false);
    }

    public static void textRight(GuiGraphics graphics, String text, float rightX, float y, int color) {
        graphics.drawString(font(), text, (int) (rightX - font().width(text)), (int) y, color, false);
    }

    public static int width(String text) {
        return font().width(text);
    }

    public static void scissor(GuiGraphics graphics, float x, float y, float width, float height) {
        graphics.enableScissor((int) x, (int) y, (int) (x + width), (int) (y + height));
    }

    public static void unscissor(GuiGraphics graphics) {
        graphics.disableScissor();
    }

    public static boolean hovered(double mouseX, double mouseY, float x, float y, float width, float height) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    public static float clamp(float value, float min, float max) {
        return value < min ? min : Math.min(value, max);
    }

    public static float animate(float current, float target, float speed) {
        return current + (target - current) * speed;
    }
}
