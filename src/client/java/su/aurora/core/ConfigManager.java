package su.aurora.core;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.Minecraft;
import su.aurora.Aurora;
import su.aurora.AuroraClient;
import su.aurora.setting.Setting;
import su.aurora.ui.ClickGuiScreen;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Map;

/**
 * Конфиг клиента: config/aurora/config.json.
 * Хранит состояние модулей, их настройки, бинды и оформление меню.
 */
public class ConfigManager {
    private final File file = new File(new File(Minecraft.getInstance().gameDirectory, "config/aurora"), "config.json");
    private final com.google.gson.Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private boolean dirty;

    public void markDirty() {
        this.dirty = true;
    }

    public void save() {
        try {
            JsonObject root = new JsonObject();

            JsonObject modules = new JsonObject();
            for (Module module : AuroraClient.MODULES.all()) {
                JsonObject node = new JsonObject();
                node.addProperty("enabled", module.isEnabled());
                node.addProperty("key", module.getKey());
                JsonObject settings = new JsonObject();
                for (Setting<?> setting : module.getSettings()) {
                    settings.add(setting.getName(), setting.save());
                }
                node.add("settings", settings);
                modules.add(module.getName(), node);
            }
            root.add("modules", modules);

            JsonObject gui = new JsonObject();
            gui.addProperty("theme", ClickGuiScreen.getTheme().name());
            gui.addProperty("scale", ClickGuiScreen.getScale());
            root.add("gui", gui);

            if (file.getParentFile() != null) {
                file.getParentFile().mkdirs();
            }
            Files.writeString(file.toPath(), gson.toJson(root), StandardCharsets.UTF_8);
            dirty = false;
        } catch (Exception exception) {
            Aurora.LOG.error("Не удалось сохранить конфиг", exception);
        }
    }

    public void load() {
        try {
            if (!file.exists()) {
                Aurora.LOG.info("Конфиг не найден, используются настройки по умолчанию");
                return;
            }
            String content = Files.readString(file.toPath(), StandardCharsets.UTF_8);
            JsonObject root = JsonParser.parseString(content).getAsJsonObject();

            if (root.has("modules")) {
                JsonObject modules = root.getAsJsonObject("modules");
                for (Map.Entry<String, JsonElement> entry : modules.entrySet()) {
                    Module module = AuroraClient.MODULES.get(entry.getKey());
                    if (module == null || !entry.getValue().isJsonObject()) {
                        continue;
                    }
                    JsonObject node = entry.getValue().getAsJsonObject();
                    if (node.has("key")) {
                        module.setKey(node.get("key").getAsInt());
                    }
                    if (node.has("settings")) {
                        JsonObject settings = node.getAsJsonObject("settings");
                        for (Setting<?> setting : module.getSettings()) {
                            if (settings.has(setting.getName())) {
                                setting.load(settings.get(setting.getName()));
                            }
                        }
                    }
                    if (node.has("enabled") && node.get("enabled").getAsBoolean()) {
                        module.setEnabled(true);
                    }
                }
            }

            if (root.has("gui")) {
                JsonObject gui = root.getAsJsonObject("gui");
                if (gui.has("theme")) {
                    try {
                        ClickGuiScreen.setTheme(ClickGuiScreen.Theme.valueOf(gui.get("theme").getAsString()));
                    } catch (IllegalArgumentException ignored) {
                    }
                }
                if (gui.has("scale")) {
                    ClickGuiScreen.setScale(gui.get("scale").getAsFloat());
                }
            }
            Aurora.LOG.info("Конфиг загружен: {}", file.getAbsolutePath());
        } catch (Exception exception) {
            Aurora.LOG.error("Не удалось загрузить конфиг", exception);
        }
    }

    public boolean isDirty() {
        return dirty;
    }
}
