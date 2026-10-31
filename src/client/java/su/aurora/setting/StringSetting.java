package su.aurora.setting;

import com.google.gson.JsonElement;

public class StringSetting extends Setting<String> {
    private final int maxLength;

    public StringSetting(String name, String defaultValue) {
        this(name, defaultValue, 128);
    }

    public StringSetting(String name, String defaultValue, int maxLength) {
        super(name, defaultValue);
        this.maxLength = maxLength;
    }

    public int getMaxLength() {
        return maxLength;
    }

    @Override
    public String displayValue() {
        return get().isEmpty() ? "—" : get();
    }

    @Override
    public JsonElement save() {
        return primitive(get());
    }

    @Override
    public void load(JsonElement element) {
        if (element != null && element.isJsonPrimitive()) {
            set(element.getAsString());
        }
    }
}
