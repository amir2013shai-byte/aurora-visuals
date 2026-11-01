package su.aurora.modules.hud;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.PlayerInfo;
import su.aurora.core.Category;
import su.aurora.core.Module;
import su.aurora.setting.BooleanSetting;
import su.aurora.setting.ColorSetting;
import su.aurora.setting.ModeSetting;
import su.aurora.setting.StringSetting;
import su.aurora.util.ColorUtil;
import su.aurora.util.Render2D;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * StaffList — админы и модераторы в табе: ищет по ключевым словам в нике.
 */
public class StaffListModule extends Module {
    private final ModeSetting position = addMode("Позиция", "Справа сверху", "Справа сверху", "Слева сверху",
            "Справа снизу", "Слева снизу");
    private final StringSetting keywords = addString("Ключевые слова",
            "admin,mod,helper,owner,curator,dev,staff,мод,адм");
    private final BooleanSetting notify = addBoolean("Уведомление в чат", true);
    private final ColorSetting accent = addColor("Акцент", 0xFFFF5C7C);

    private final List<String> staff = new ArrayList<>();
    private final List<String> notified = new ArrayList<>();

    public StaffListModule() {
        super("StaffList", "Список админов и модераторов с сервера", Category.HUD);
    }

    @Override
    public void onTick() {
        Minecraft client = Minecraft.getInstance();
        staff.clear();
        if (client.getConnection() == null) {
            return;
        }
        List<String> words = new ArrayList<>();
        for (String word : keywords.get().toLowerCase(Locale.ROOT).split("[,;\\s]+")) {
            if (!word.isBlank()) {
                words.add(word);
            }
        }
        for (PlayerInfo info : client.getConnection().getOnlinePlayers()) {
            String name = info.getProfile().name();
            String lower = name.toLowerCase(Locale.ROOT);
            for (String word : words) {
                if (lower.contains(word)) {
                    staff.add(name);
                    if (notify.get() && !notified.contains(name)) {
                        notified.add(name);
                        client.gui.getChat().addMessage(net.minecraft.network.chat.Component
                                .literal("§c[Aurora] §fв табе найден §e" + name + " §f(по слову «" + word + "»)"));
                    }
                    break;
                }
            }
        }
    }

    @Override
    public void onHudRender(GuiGraphics graphics, DeltaTracker delta) {
        if (staff.isEmpty()) {
            return;
        }
        Minecraft client = Minecraft.getInstance();
        if (client.player == null || client.options.hideGui) {
            return;
        }
        int width = 96;
        for (String name : staff) {
            width = Math.max(width, Render2D.width(name) + 28);
        }
        int height = 18 + staff.size() * 10;
        int screenWidth = client.getWindow().getGuiScaledWidth();
        int screenHeight = client.getWindow().getGuiScaledHeight();
        int x = switch (position.get()) {
            case "Слева сверху", "Слева снизу" -> 8;
            default -> screenWidth - width - 8;
        };
        int y = switch (position.get()) {
            case "Слева снизу", "Справа снизу" -> screenHeight - height - 8;
            default -> 8;
        };
        Render2D.roundedRect(graphics, x, y, width, height, 6, 0xB30C0E14);
        Render2D.roundedRect(graphics, x, y, 3, height, 2, accent.get());
        Render2D.textShadow(graphics, "STAFF " + staff.size(), x + 9, y + 5, accent.get());
        for (int i = 0; i < staff.size(); i++) {
            Render2D.text(graphics, staff.get(i), x + 9, y + 16 + i * 10,
                    ColorUtil.withAlpha(0xFFFFFFFF, 220));
        }
    }
}
