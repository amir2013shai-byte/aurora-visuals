package su.aurora.modules.player;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;
import su.aurora.AuroraClient;
import su.aurora.core.Category;
import su.aurora.core.Module;
import su.aurora.setting.BooleanSetting;
import su.aurora.setting.ModeSetting;
import su.aurora.setting.NumberSetting;
import su.aurora.setting.StringSetting;
import su.aurora.util.PlayerUtil;

/**
 * TargetActions — действия при появлении цели: сообщение, команда, уведомление.
 */
public class TargetActionsModule extends Module {
    private final ModeSetting action = addMode("Действие", "Уведомление", "Уведомление", "Сообщение", "Команда");
    private final StringSetting payload = addString("Текст", "цель: %name%");
    private final NumberSetting cooldown = addInt("Задержка (мс)", 4000, 500, 30000);
    private final BooleanSetting onlyPlayers = addBoolean("Только игроки", true);

    private String lastTarget = "";
    private long lastAction;

    public TargetActionsModule() {
        super("TargetActions", "Реакция на появление цели: сообщение или команда", Category.PLAYER);
    }

    @Override
    public void onTick() {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null) {
            return;
        }
        LivingEntity target = PlayerUtil.crosshairTarget(64);
        if (target == null) {
            lastTarget = "";
            return;
        }
        if (onlyPlayers.get() && !(target instanceof net.minecraft.world.entity.player.Player)) {
            return;
        }
        String name = target.getName().getString();
        if (name.equals(lastTarget)) {
            return;
        }
        if (System.currentTimeMillis() - lastAction < cooldown.getInt()) {
            return;
        }
        lastTarget = name;
        lastAction = System.currentTimeMillis();

        String text = payload.get().replace("%name%", name)
                .replace("%hp%", String.format(java.util.Locale.ROOT, "%.1f", target.getHealth()))
                .replace("%dist%", String.format(java.util.Locale.ROOT, "%.0f", target.distanceTo(client.player)));

        switch (action.get()) {
            case "Сообщение" -> {
                if (client.getConnection() != null && !text.isBlank()) {
                    client.getConnection().sendChat(text);
                }
            }
            case "Команда" -> {
                if (client.getConnection() != null && !text.isBlank()) {
                    client.getConnection().sendCommand(text.replaceFirst("^/", ""));
                }
            }
            default -> AuroraClient.NOTIFICATIONS.push("TargetActions", name + " в фокусе", 0xFF7C5CFF, 2000L);
        }
    }
}
