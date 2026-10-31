package su.aurora.ui;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import su.aurora.Aurora;
import su.aurora.AuroraClient;
import su.aurora.core.Module;

/**
 * Единая точка отрисовки HUD-модулей.
 */
public final class HudRenderer {
    private HudRenderer() {
    }

    public static void render(GuiGraphics graphics, DeltaTracker delta) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null || AuroraClient.MODULES == null) {
            return;
        }
        for (Module module : AuroraClient.MODULES.all()) {
            if (!module.isEnabled() || module.getCategory() != su.aurora.core.Category.HUD) {
                continue;
            }
            try {
                module.onHudRender(graphics, delta);
            } catch (Throwable throwable) {
                Aurora.LOG.error("[{}] Ошибка HUD", module.getName(), throwable);
            }
        }
    }
}
