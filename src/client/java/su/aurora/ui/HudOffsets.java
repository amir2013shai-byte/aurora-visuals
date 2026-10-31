package su.aurora.ui;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.fabricmc.loader.api.FabricLoader;
import su.aurora.Aurora;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * Смещения HUD-элементов, которые задаются редактором HUD (перетаскиванием).
 * Хранятся отдельно от конфига модулей: config/aurora/hud_offsets.json
 */
public final class HudOffsets {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Map<String, float[]> OFFSETS = new HashMap<>();
    private static boolean loaded;

    private HudOffsets() {
    }

    private static Path file() {
        return FabricLoader.getInstance().getConfigDir().resolve("aurora").resolve("hud_offsets.json");
    }

    public static void ensureLoaded() {
        if (loaded) {
            return;
        }
        loaded = true;
        Path path = file();
        if (!Files.exists(path)) {
            return;
        }
        try (Reader reader = Files.newBufferedReader(path)) {
            Map<String, float[]> data = GSON.fromJson(reader,
                    new TypeToken<Map<String, float[]>>() {
                    }.getType());
            if (data != null) {
                OFFSETS.putAll(data);
            }
        } catch (IOException | RuntimeException exception) {
            Aurora.LOG.warn("[HUD] не удалось прочитать смещения: {}", exception.getMessage());
        }
    }

    public static float[] get(String module) {
        ensureLoaded();
        float[] value = OFFSETS.get(module);
        return value != null && value.length >= 2 ? value : new float[]{0f, 0f};
    }

    public static void set(String module, float x, float y) {
        ensureLoaded();
        OFFSETS.put(module, new float[]{x, y});
    }

    public static void add(String module, float dx, float dy) {
        float[] current = get(module);
        set(module, current[0] + dx, current[1] + dy);
    }

    public static void reset(String module) {
        ensureLoaded();
        OFFSETS.remove(module);
    }

    public static boolean isMoved(String module) {
        float[] offset = get(module);
        return Math.abs(offset[0]) > 0.5f || Math.abs(offset[1]) > 0.5f;
    }

    public static void save() {
        ensureLoaded();
        Path path = file();
        try {
            Files.createDirectories(path.getParent());
            try (Writer writer = Files.newBufferedWriter(path)) {
                GSON.toJson(OFFSETS, writer);
            }
        } catch (IOException exception) {
            Aurora.LOG.warn("[HUD] не удалось сохранить смещения: {}", exception.getMessage());
        }
    }
}
