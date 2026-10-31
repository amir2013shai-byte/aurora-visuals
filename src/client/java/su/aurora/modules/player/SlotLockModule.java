package su.aurora.modules.player;

import net.minecraft.client.Minecraft;
import su.aurora.core.Category;
import su.aurora.core.Module;
import su.aurora.setting.BooleanSetting;
import su.aurora.setting.StringSetting;

import java.util.HashSet;
import java.util.Set;

/**
 * SlotLock — блокировка слотов хотбара. Закреплённый слот нельзя случайно выбрать колесом.
 */
public class SlotLockModule extends Module {
    private final StringSetting slots = addString("Слоты", "1,9");
    private final BooleanSetting notify = addBoolean("Уведомлять", true);

    private final Set<Integer> locked = new HashSet<>();
    private int lastSlot = -1;

    public SlotLockModule() {
        super("SlotLock", "Закрепляет слоты хотбара, чтобы не переключаться случайно", Category.PLAYER);
    }

    private void parse() {
        locked.clear();
        for (String part : slots.get().split("[,;\\s]+")) {
            if (part.isBlank()) {
                continue;
            }
            try {
                int slot = Integer.parseInt(part.trim()) - 1;
                if (slot >= 0 && slot < 9) {
                    locked.add(slot);
                }
            } catch (NumberFormatException ignored) {
            }
        }
    }

    @Override
    public void onEnable() {
        parse();
        lastSlot = -1;
    }

    @Override
    public void onTick() {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null) {
            return;
        }
        if (locked.isEmpty()) {
            parse();
        }
        int selected = client.player.getInventory().getSelectedSlot();
        if (lastSlot == -1) {
            lastSlot = selected;
            return;
        }
        if (locked.contains(selected) && selected != lastSlot) {
            client.player.getInventory().setSelectedSlot(lastSlot);
            if (notify.get()) {
                su.aurora.AuroraClient.NOTIFICATIONS.push("SlotLock", "Слот " + (selected + 1) + " заблокирован",
                        0xFFFFB74D, 1500L);
            }
            return;
        }
        lastSlot = selected;
    }
}
