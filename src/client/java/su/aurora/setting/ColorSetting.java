package su.aurora.setting;

import com.google.gson.JsonElement;

public class ColorSetting extends Setting<Integer> {
    public ColorSetting(String name, int argb) {
        super(name, argb);
    }

    public int getRGB() {
        return get() & 0xFFFFFF;
    }

    public int getAlpha() {
        return (get() >>> 24) & 0xFF;
    }

    public int withAlpha(int alpha) {
        return (alpha << 24) | getRGB();
    }

    public void setHue(float hue) {
        int rgb = java.awt.Color.HSBtoRGB(hue % 1.0f, 0.75f, 1.0f);
        set((get() & 0xFF000000) | (rgb & 0xFFFFFF));
    }

    public float getHue() {
        int rgb = getRGB();
        float[] hsb = java.awt.Color.RGBtoHSB((rgb >> 16) & 0xFF, (rgb >> 8) & 0xFF, rgb & 0xFF, null);
        return hsb[0];
    }

    @Override
    public String displayValue() {
        return String.format("#%06X", getRGB());
    }

    @Override
    public JsonElement save() {
        return primitive(get());
    }

    @Override
    public void load(JsonElement element) {
        if (element != null && element.isJsonPrimitive()) {
            set(element.getAsInt());
        }
    }
}
