package su.aurora;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;
import su.aurora.core.ConfigManager;
import su.aurora.core.Module;
import su.aurora.core.ModuleManager;
import su.aurora.core.Notifications;
import su.aurora.ui.ClickGuiScreen;
import su.aurora.ui.HudRenderer;
import su.aurora.util.Render3D;

import java.util.HashMap;
import java.util.Map;

public class AuroraClient implements ClientModInitializer {
    public static ModuleManager MODULES;
    public static ConfigManager CONFIG;
    public static Notifications NOTIFICATIONS;

    public static KeyMapping openMenuKey;

    private static final Map<Integer, Boolean> KEY_STATES = new HashMap<>();
    private static long lastAutoSave;

    @Override
    public void onInitializeClient() {
        MODULES = new ModuleManager();
        MODULES.init();

        NOTIFICATIONS = new Notifications();
        CONFIG = new ConfigManager();
        CONFIG.load();

        KeyMapping.Category category = KeyMapping.Category.register(Aurora.id("main"));
        openMenuKey = new KeyMapping("key.aurora.menu", InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_SHIFT, category);
        KeyBindingHelper.registerKeyBinding(openMenuKey);

        ClientTickEvents.END_CLIENT_TICK.register(AuroraClient::onClientTick);

        // Мировой рендер: всё, что рисуется в мире (ESP, следы, кейпы, крылья и т.д.)
        WorldRenderEvents.BEFORE_DEBUG_RENDER.register(Render3D::onWorldRender);

        // HUD: островок, watermark, список модулей, таргет-хад
        HudElementRegistry.addLast(Aurora.id("hud"), HudRenderer::render);

        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> {
            CONFIG.save();
            Aurora.LOG.info("Конфиг сохранён при выходе");
        });

        Aurora.LOG.info("{} v{} инициализирован — {} модулей", Aurora.NAME, Aurora.VERSION,
                MODULES.all().size());
    }

    private static void onClientTick(Minecraft client) {
        NOTIFICATIONS.tick();

        while (openMenuKey.consumeClick()) {
            if (client.screen == null && client.player != null) {
                ClickGuiScreen.open();
            }
        }

        if (client.player != null && client.level != null) {
            for (Module module : MODULES.all()) {
                if (module.isEnabled()) {
                    try {
                        module.onTick();
                    } catch (Throwable throwable) {
                        Aurora.LOG.error("[{}] Ошибка в тике", module.getName(), throwable);
                    }
                }
            }
        }

        handleKeybinds(client);

        if (System.currentTimeMillis() - lastAutoSave > 15_000L && CONFIG.isDirty()) {
            lastAutoSave = System.currentTimeMillis();
            CONFIG.save();
        }
    }

    /** Ручной опрос биндов модулей — работает и без открытых экранов настроек. */
    private static void handleKeybinds(Minecraft client) {
        boolean allowed = client.screen == null && client.player != null;
        for (Module module : MODULES.all()) {
            int key = module.getKey();
            if (key <= 0) {
                continue;
            }
            boolean down = allowed && InputConstants.isKeyDown(client.getWindow(), key);
            boolean wasDown = KEY_STATES.getOrDefault(key, false);
            if (down && !wasDown) {
                module.toggle();
                CONFIG.markDirty();
            }
            KEY_STATES.put(key, down);
        }
    }
}
