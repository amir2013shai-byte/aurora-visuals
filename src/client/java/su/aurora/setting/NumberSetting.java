package su.aurora.setting;

import com.google.gson.JsonElement;
import net.minecraft.util.Mth;

public class NumberSetting extends Setting<Double> {
    private final double min;
    private final double max;
    private final double step;
    private final boolean integer;
    private final String suffix;

    public NumberSetting(String name, double defaultValue, double min, double max, double step, boolean integer, String suffix) {
        super(name, defaultValue);
        this.min = min;
        this.max = max;
        this.step = step;
        this.integer = integer;
        this.suffix = suffix;
    }

    public static NumberSetting of(String name, double def, double min, double max, double step) {
        return new NumberSetting(name, def, min, max, step, false, "");
    }

    public static NumberSetting intOf(String name, int def, int min, int max) {
        return new NumberSetting(name, def, min, max, 1, true, "");
    }

    /** Значение 0..1 для ползунка. */
    public float getProgress() {
        return (float) ((get() - min) / (max - min));
    }

    public void setProgress(float progress) {
        double raw = min + Mth.clamp(progress, 0.0f, 1.0f) * (max - min);
        double stepped = Math.round(raw / step) * step;
        set(Mth.clamp(stepped, min, max));
    }

    public int getInt() {
        return (int) Math.round(get());
    }

    public float getFloat() {
        return (float) (double) get();
    }

    public double getMin() {
        return min;
    }

    public double getMax() {
        return max;
    }

    public boolean isInteger() {
        return integer;
    }

    @Override
    public String displayValue() {
        if (integer) {
            return String.valueOf(getInt()) + suffix;
        }
        return String.format(java.util.Locale.ROOT, "%.2f", get()).replace(",", ".") + suffix;
    }

    @Override
    public JsonElement save() {
        return primitive(get());
    }

    @Override
    public void load(JsonElement element) {
        if (element != null && element.isJsonPrimitive()) {
            set(Mth.clamp(element.getAsDouble(), min, max));
        }
    }
}
