package su.aurora.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

/**
 * Базовая настройка модуля. Значение всегда сериализуется в JSON-примитив,
 * чтобы конфиг оставался читаемым и совместимым между версиями.
 */
public abstract class Setting<T> {
    private final String name;
    protected T value;
    private boolean visible = true;

    protected Setting(String name, T defaultValue) {
        this.name = name;
        this.value = defaultValue;
    }

    public String getName() {
        return name;
    }

    public T get() {
        return value;
    }

    public void set(T newValue) {
        this.value = newValue;
    }

    public boolean isVisible() {
        return visible;
    }

    public Setting<T> visibleIf(boolean state) {
        this.visible = state;
        return this;
    }

    public abstract String displayValue();

    public abstract JsonElement save();

    public abstract void load(JsonElement element);

    protected static JsonElement primitive(Number number) {
        return new JsonPrimitive(number);
    }

    protected static JsonElement primitive(String string) {
        return new JsonPrimitive(string);
    }

    protected static JsonElement primitive(boolean bool) {
        return new JsonPrimitive(bool);
    }
}
