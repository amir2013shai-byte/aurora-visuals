package su.aurora.setting;

import com.google.gson.JsonElement;

public class BooleanSetting extends Setting<Boolean> {
    public BooleanSetting(String name, boolean defaultValue) {
        super(name, defaultValue);
    }

    public void toggle() {
        set(!get());
    }

    @Override
    public String displayValue() {
        return get() ? "ON" : "OFF";
    }

    @Override
    public JsonElement save() {
        return primitive(get());
    }

    @Override
    public void load(JsonElement element) {
        if (element != null && element.isJsonPrimitive()) {
            set(element.getAsBoolean());
        }
    }
}
