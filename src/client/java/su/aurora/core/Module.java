package su.aurora.core;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;
import su.aurora.setting.BooleanSetting;
import su.aurora.setting.ColorSetting;
import su.aurora.setting.ModeSetting;
import su.aurora.setting.NumberSetting;
import su.aurora.setting.Setting;
import su.aurora.setting.StringSetting;

import java.util.ArrayList;
import java.util.List;

public abstract class Module {
    private final String name;
    private final String description;
    private final Category category;
    private boolean enabled;
    private int key = -1;
    private final List<Setting<?>> settings = new ArrayList<>();

    protected Module(String name, String description, Category category) {
        this.name = name;
        this.description = description;
        this.category = category;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Category getCategory() {
        return category;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean state) {
        if (this.enabled == state) {
            return;
        }
        this.enabled = state;
        try {
            if (state) {
                onEnable();
            } else {
                onDisable();
            }
        } catch (Throwable t) {
            su.aurora.Aurora.LOG.error("[{}] Ошибка переключения модуля", name, t);
        }
        if (su.aurora.AuroraClient.NOTIFICATIONS != null) {
            su.aurora.AuroraClient.NOTIFICATIONS.push(name, state ? "включён" : "выключен",
                    state ? 0xFF7CF7B0 : 0xFFFF7C7C);
        }
    }

    public void toggle() {
        setEnabled(!enabled);
    }

    public int getKey() {
        return key;
    }

    public void setKey(int key) {
        this.key = key;
    }

    public boolean hasKey() {
        return key > 0;
    }

    public String getKeyName() {
        if (!hasKey()) {
            return "NONE";
        }
        try {
            return InputConstants.Type.KEYSYM.getOrCreate(key).getDisplayName().getString().toUpperCase();
        } catch (Throwable t) {
            return "KEY" + key;
        }
    }

    public List<Setting<?>> getSettings() {
        return settings;
    }

    /* ---------- жизненный цикл ---------- */

    public void onEnable() {
    }

    public void onDisable() {
    }

    /** Вызывается каждый клиентский тик (20 раз в секунду), только если модуль включён. */
    public void onTick() {
    }

    /** Вызывается в мировом рендере (BEFORE_DEBUG_RENDER), только если модуль включён. */
    public void onWorldRender(WorldRenderContext context) {
    }

    /** Вызывается при отрисовке HUD, только если модуль включён. */
    public void onHudRender(GuiGraphics graphics, DeltaTracker delta) {
    }

    /* ---------- хелперы для настроек ---------- */

    protected BooleanSetting addBoolean(String name, boolean def) {
        BooleanSetting setting = new BooleanSetting(name, def);
        settings.add(setting);
        return setting;
    }

    protected NumberSetting addNumber(String name, double def, double min, double max, double step) {
        NumberSetting setting = NumberSetting.of(name, def, min, max, step);
        settings.add(setting);
        return setting;
    }

    protected NumberSetting addInt(String name, int def, int min, int max) {
        NumberSetting setting = NumberSetting.intOf(name, def, min, max);
        settings.add(setting);
        return setting;
    }

    protected ModeSetting addMode(String name, String def, String... modes) {
        ModeSetting setting = new ModeSetting(name, def, modes);
        settings.add(setting);
        return setting;
    }

    protected ColorSetting addColor(String name, int argb) {
        ColorSetting setting = new ColorSetting(name, argb);
        settings.add(setting);
        return setting;
    }

    protected StringSetting addString(String name, String def) {
        StringSetting setting = new StringSetting(name, def);
        settings.add(setting);
        return setting;
    }

    public Setting<?> getSetting(String name) {
        for (Setting<?> setting : settings) {
            if (setting.getName().equalsIgnoreCase(name)) {
                return setting;
            }
        }
        return null;
    }
}
