package su.aurora.setting;

import com.google.gson.JsonElement;

import java.util.Arrays;
import java.util.List;

public class ModeSetting extends Setting<String> {
    private final List<String> modes;

    public ModeSetting(String name, String defaultMode, String... modes) {
        super(name, defaultMode);
        this.modes = Arrays.asList(modes);
    }

    public List<String> getModes() {
        return modes;
    }

    public boolean is(String mode) {
        return get().equalsIgnoreCase(mode);
    }

    public void next() {
        int index = modes.indexOf(get());
        set(modes.get((index + 1) % modes.size()));
    }

    public void previous() {
        int index = modes.indexOf(get());
        set(modes.get((index - 1 + modes.size()) % modes.size()));
    }

    @Override
    public void set(String newValue) {
        if (modes.contains(newValue)) {
            super.set(newValue);
        }
    }

    @Override
    public String displayValue() {
        return get();
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
