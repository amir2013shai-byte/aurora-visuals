package su.aurora.util;

import net.minecraft.util.Mth;

public final class ColorUtil {
    private ColorUtil() {
    }

    public static int rgba(int r, int g, int b, int a) {
        return (Mth.clamp(a, 0, 255) << 24) | (Mth.clamp(r, 0, 255) << 16) | (Mth.clamp(g, 0, 255) << 8)
                | Mth.clamp(b, 0, 255);
    }

    public static int red(int color) {
        return (color >> 16) & 0xFF;
    }

    public static int green(int color) {
        return (color >> 8) & 0xFF;
    }

    public static int blue(int color) {
        return color & 0xFF;
    }

    public static int alpha(int color) {
        return (color >>> 24) & 0xFF;
    }

    public static int withAlpha(int color, int alpha) {
        return (Mth.clamp(alpha, 0, 255) << 24) | (color & 0xFFFFFF);
    }

    public static int scaleAlpha(int color, float factor) {
        return withAlpha(color, (int) (alpha(color) * Mth.clamp(factor, 0f, 1f)));
    }

    public static int lerp(int from, int to, float delta) {
        float t = Mth.clamp(delta, 0f, 1f);
        return rgba(
                (int) Mth.lerp(t, red(from), red(to)),
                (int) Mth.lerp(t, green(from), green(to)),
                (int) Mth.lerp(t, blue(from), blue(to)),
                (int) Mth.lerp(t, alpha(from), alpha(to)));
    }

    /** Плавный радужный цвет для следов и аур. */
    public static int rainbow(long speed, float saturation, float brightness, int alpha) {
        float hue = (System.currentTimeMillis() % (long) speed) / speed;
        return withAlpha(java.awt.Color.HSBtoRGB(hue, saturation, brightness), alpha);
    }

    public static int pulse(int color, float speed, float min, float max) {
        float t = (float) ((Math.sin(System.currentTimeMillis() / 1000.0 * speed) + 1.0) * 0.5);
        float factor = Mth.lerp(t, min, max);
        return withAlpha(color, (int) (alpha(color) * factor));
    }

    public static int darken(int color, float factor) {
        return rgba((int) (red(color) * (1 - factor)), (int) (green(color) * (1 - factor)),
                (int) (blue(color) * (1 - factor)), alpha(color));
    }

    public static int mix(int first, int second, float delta) {
        return lerp(first, second, delta);
    }
}
