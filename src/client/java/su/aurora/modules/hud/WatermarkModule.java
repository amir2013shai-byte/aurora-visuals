package su.aurora.modules.hud;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import su.aurora.Aurora;
import su.aurora.core.Category;
import su.aurora.core.Module;
import su.aurora.setting.BooleanSetting;
import su.aurora.setting.ColorSetting;
import su.aurora.setting.ModeSetting;
import su.aurora.ui.ClickGuiScreen;
import su.aurora.util.PlayerUtil;
import su.aurora.util.Render2D;

/**
 * Watermark — логотип клиента, FPS и пинг в углу экрана.
 */
public class WatermarkModule extends Module {
    private final ModeSetting style = addMode("Стиль", "Aurora", "Aurora", "Минимал", "Полоска");
    private final BooleanSetting showFps = addBoolean("Показывать FPS", true);
    private final BooleanSetting showPing = addBoolean("Показывать пинг", true);
    private final BooleanSetting showCoords = addBoolean("Показывать координаты", false);
    private final ColorSetting accent = addColor("Акцент", 0xFF7C5CFF);

    public WatermarkModule() {
        super("Watermark", "Логотип клиента, FPS и пинг", Category.HUD);
        setEnabled(true);
    }

    @Override
    public void onHudRender(GuiGraphics graphics, DeltaTracker delta) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null || client.getWindow() == null || client.options.hideGui) {
            return;
        }
        String name = Aurora.NAME.toUpperCase();
        StringBuilder info = new StringBuilder();
        if (showFps.get()) {
            info.append("  |  ").append(PlayerUtil.fps()).append(" FPS");
        }
        if (showPing.get()) {
            info.append("  |  ").append(PlayerUtil.ping()).append(" ms");
        }
        if (showCoords.get()) {
            info.append(String.format(java.util.Locale.ROOT, "  |  %.0f %.0f %.0f",
                    client.player.getX(), client.player.getY(), client.player.getZ()));
        }

        int accentColor = accent.get();
        switch (style.get()) {
            case "Минимал" -> {
                Render2D.textShadow(graphics, name, 8, 8, 0xFFFFFFFF);
                Render2D.textShadow(graphics, info.toString().trim(), 8, 20, accentColor);
            }
            case "Полоска" -> {
                int width = Render2D.width(name) + 18;
                Render2D.roundedRect(graphics, 6, 6, width, 20, 6, 0x8C0C0E14);
                Render2D.roundedRect(graphics, 8, 12, 3, 8, 2, accentColor);
                Render2D.textShadow(graphics, name, 15, 12, 0xFFFFFFFF);
                Render2D.textShadow(graphics, info.toString().trim(), 15 + Render2D.width(name) + 6, 12,
                        ClickGuiScreen.getTheme().textSecondary());
                int infoWidth = Render2D.width(info.toString().trim());
                Render2D.roundedRect(graphics, 6, 28, Math.max(width, infoWidth + 18), 16, 5, 0x660C0E14);
            }
            default -> {
                int width = Render2D.width(name + info);
                Render2D.roundedRect(graphics, 6, 6, width + 22, 22, 8, 0x990C0E14);
                Render2D.roundedRect(graphics, 6, 6, 3, 22, 3, accentColor);
                Render2D.textShadow(graphics, name, 14, 13, 0xFFFFFFFF);
                Render2D.textShadow(graphics, info.toString(), 14 + Render2D.width(name), 13,
                        ClickGuiScreen.getTheme().textSecondary());
            }
        }
    }
}
