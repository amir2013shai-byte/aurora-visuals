package su.aurora.modules.hud;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import su.aurora.core.Category;
import su.aurora.core.Module;
import su.aurora.setting.BooleanSetting;
import su.aurora.setting.ColorSetting;
import su.aurora.setting.ModeSetting;
import su.aurora.util.ColorUtil;
import su.aurora.util.PlayerUtil;
import su.aurora.util.Render2D;

import java.util.Locale;

/**
 * ServerInfo — пинг, TPS (по частоте тиков клиента), онлайн и время сессии.
 */
public class ServerInfoModule extends Module {
    private final ModeSetting position = addMode("Позиция", "Слева сверху", "Слева сверху", "Справа сверху",
            "Слева снизу", "Справа снизу");
    private final BooleanSetting showPing = addBoolean("Пинг", true);
    private final BooleanSetting showTps = addBoolean("TPS", true);
    private final BooleanSetting showOnline = addBoolean("Онлайн", true);
    private final BooleanSetting showUptime = addBoolean("Время сессии", false);
    private final ColorSetting accent = addColor("Акцент", 0xFF7C5CFF);

    private int tickCounter;
    private int tps = 20;
    private long lastTpsSample = System.currentTimeMillis();
    private final long sessionStart = System.currentTimeMillis();

    public ServerInfoModule() {
        super("ServerInfo", "Пинг, TPS, онлайн и время сессии", Category.HUD);
    }

    @Override
    public void onTick() {
        tickCounter++;
        long now = System.currentTimeMillis();
        if (now - lastTpsSample >= 1000) {
            tps = Math.min(20, (int) Math.round(tickCounter * 1000.0 / (now - lastTpsSample)));
            tickCounter = 0;
            lastTpsSample = now;
        }
    }

    @Override
    public void onHudRender(GuiGraphics graphics, DeltaTracker delta) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null || client.options.hideGui) {
            return;
        }
        StringBuilder text = new StringBuilder();
        if (showPing.get()) {
            text.append("PING ").append(PlayerUtil.ping()).append(" ms");
        }
        if (showTps.get()) {
            String color = tps >= 19 ? "§a" : tps >= 15 ? "§e" : "§c";
            text.append(text.isEmpty() ? "" : "  ·  ").append("TPS ").append(color).append(tps);
        }
        if (showOnline.get()) {
            int online = client.getConnection() == null ? 0 : client.getConnection().getOnlinePlayers().size();
            text.append(text.isEmpty() ? "" : "  ·  ").append("ONLINE ").append(online);
        }
        if (showUptime.get()) {
            long seconds = (System.currentTimeMillis() - sessionStart) / 1000;
            text.append(text.isEmpty() ? "" : "  ·  ")
                    .append(String.format(Locale.ROOT, "%02d:%02d", seconds / 60, seconds % 60));
        }
        if (text.isEmpty()) {
            return;
        }

        String plain = text.toString();
        int width = Render2D.width(plain) + 16;
        int height = 18;
        int screenWidth = client.getWindow().getGuiScaledWidth();
        int screenHeight = client.getWindow().getGuiScaledHeight();
        int x = switch (position.get()) {
            case "Справа сверху", "Справа снизу" -> screenWidth - width - 8;
            default -> 8;
        };
        int y = switch (position.get()) {
            case "Слева снизу", "Справа снизу" -> screenHeight - height - 8;
            default -> 8;
        };
        Render2D.roundedRect(graphics, x, y, width, height, 6, 0xB30C0E14);
        Render2D.roundedRect(graphics, x, y, 3, height, 2, accent.get());
        Render2D.textShadow(graphics, plain, x + 9, y + 5, ColorUtil.withAlpha(0xFFFFFFFF, 235));
    }
}
