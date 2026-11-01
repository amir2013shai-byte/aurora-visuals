package su.aurora.core;

import net.minecraft.client.Minecraft;
import su.aurora.Aurora;
import su.aurora.AuroraClient;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * Профили конфигов: config/aurora/profiles/<имя>.json.
 * Профиль — это копия основного config.json, поэтому логика сохранения/загрузки
 * полностью переиспользует {@link ConfigManager}.
 */
public final class ProfileManager {
    private ProfileManager() {
    }

    private static File dir() {
        return new File(new File(Minecraft.getInstance().gameDirectory, "config/aurora"), "profiles");
    }

    private static File configFile() {
        return new File(new File(Minecraft.getInstance().gameDirectory, "config/aurora"), "config.json");
    }

    private static File profileFile(String name) {
        String safe = name.replaceAll("[^a-zA-Z0-9_\\-]", "_").toLowerCase(Locale.ROOT);
        return new File(dir(), safe + ".json");
    }

    public static boolean save(String name) {
        try {
            if (AuroraClient.CONFIG != null) {
                AuroraClient.CONFIG.save();
            }
            dir().mkdirs();
            Files.copy(configFile().toPath(), profileFile(name).toPath(), StandardCopyOption.REPLACE_EXISTING);
            return true;
        } catch (IOException exception) {
            Aurora.LOG.warn("[Профили] не удалось сохранить {}: {}", name, exception.getMessage());
            return false;
        }
    }

    public static boolean load(String name) {
        File source = profileFile(name);
        if (!source.exists()) {
            return false;
        }
        try {
            Files.copy(source.toPath(), configFile().toPath(), StandardCopyOption.REPLACE_EXISTING);
            if (AuroraClient.CONFIG != null) {
                AuroraClient.CONFIG.load();
            }
            return true;
        } catch (IOException exception) {
            Aurora.LOG.warn("[Профили] не удалось загрузить {}: {}", name, exception.getMessage());
            return false;
        }
    }

    public static List<String> list() {
        File[] files = dir().listFiles((folder, fileName) -> fileName.endsWith(".json"));
        if (files == null) {
            return List.of();
        }
        Arrays.sort(files);
        List<String> names = new ArrayList<>();
        for (File file : files) {
            names.add(file.getName().substring(0, file.getName().length() - 5));
        }
        return names;
    }
}
