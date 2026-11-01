package su.aurora;

import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Aurora Visuals — открытый визуальный клиент для Minecraft 1.21.11 (Fabric).
 * Идеи модулей вдохновлены Never Visuals, реализация написана с нуля.
 */
public final class Aurora {
    public static final String MOD_ID = "aurora";
    public static final String NAME = "Aurora Visuals";
    public static final String VERSION = "1.5.0";
    public static final Logger LOG = LoggerFactory.getLogger(NAME);

    private Aurora() {
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }
}
