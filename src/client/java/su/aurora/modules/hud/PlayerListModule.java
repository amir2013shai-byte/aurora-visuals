package su.aurora.modules.hud;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.world.entity.player.Player;
import su.aurora.core.Category;
import su.aurora.core.Module;
import su.aurora.setting.BooleanSetting;
import su.aurora.setting.ColorSetting;
import su.aurora.setting.ModeSetting;
import su.aurora.setting.NumberSetting;
import su.aurora.util.ColorUtil;
import su.aurora.util.Render2D;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * PlayerList — список игроков на сервере с пингом и дистанцией.
 */
public class PlayerListModule extends Module {
    private final ModeSetting position = addMode("Позиция", "Справа сверху", "Справа сверху", "Слева сверху", "Справа снизу");
    private final NumberSetting limit = addInt("Максимум игроков", 10, 1, 40);
    private final BooleanSetting showPing = addBoolean("Пинг", true);
    private final BooleanSetting showDistance = addBoolean("Дистанция", true);
    private final BooleanSetting sortByDistance = addBoolean("Сортировать по дистанции", true);
    private final ColorSetting accent = addColor("Акцент", 0xFF7C5CFF);

    public PlayerListModule() {
        super("PlayerList", "Список игроков с пингом и дистанцией", Category.HUD);
    }

    private static class Entry {
        final String name;
        final int ping;
        final double distance;

        Entry(String name, int ping, double distance) {
            this.name = name;
            this.ping = ping;
            this.distance = distance;
        }
    }

    @Override
    public void onHudRender(GuiGraphics graphics, DeltaTracker delta) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null || client.level == null || client.getConnection() == null
                || client.options.hideGui) {
            return;
        }

        List<Entry> entries = new ArrayList<>();
        for (PlayerInfo info : client.getConnection().getOnlinePlayers()) {
            String name = info.getProfile().name();
            double distance = -1;
            Player entity = client.level.getPlayerByUUID(info.getProfile().id());
            if (entity != null && client.player != null) {
                distance = entity.distanceTo(client.player);
            }
            entries.add(new Entry(name, info.getLatency(), distance));
        }
        if (entries.isEmpty()) {
            return;
        }
        if (sortByDistance.get()) {
            entries.sort(Comparator.comparingDouble(entry -> entry.distance < 0 ? Double.MAX_VALUE : entry.distance));
        } else {
            entries.sort(Comparator.comparing(entry -> entry.name));
        }
        if (entries.size() > limit.getInt()) {
            entries = entries.subList(0, limit.getInt());
        }

        int screenWidth = client.getWindow().getGuiScaledWidth();
        int screenHeight = client.getWindow().getGuiScaledHeight();
        float width = 132;
        float rowHeight = 14;
        float x = position.is("Слева сверху") ? 8 : screenWidth - width - 8;
        float y = position.is("Справа снизу")
                ? screenHeight - entries.size() * rowHeight - 96
                : 34;

        Render2D.roundedRect(graphics, x, y - 4, width, entries.size() * rowHeight + 8, 6, 0x730C0E14);
        Render2D.roundedRect(graphics, x, y - 4, 3, entries.size() * rowHeight + 8, 3, accent.get());

        for (Entry entry : entries) {
            double distance = entry.distance;
            if (distance >= 0 && distance > 128) {
                distance = -1;
            }
            Render2D.textShadow(graphics, entry.name, x + 9, y + 2, 0xFFFFFFFF);
            String right = "";
            if (showDistance.get() && distance >= 0) {
                right = String.format(Locale.ROOT, "%.0f м", distance);
            }
            if (showPing.get()) {
                right = (right.isEmpty() ? "" : right + " · ") + entry.ping + " ms";
            }
            if (!right.isEmpty()) {
                int color = entry.ping > 120 ? 0xFFFFB74D : ColorUtil.withAlpha(0xFFFFFFFF, 185);
                Render2D.textRight(graphics, right, x + width - 6, y + 2, color);
            }
            y += rowHeight;
        }
    }
}
