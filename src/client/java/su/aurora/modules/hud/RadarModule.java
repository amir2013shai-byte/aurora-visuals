package su.aurora.modules.hud;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import su.aurora.core.Category;
import su.aurora.core.Module;
import su.aurora.setting.BooleanSetting;
import su.aurora.setting.ColorSetting;
import su.aurora.setting.ModeSetting;
import su.aurora.setting.NumberSetting;
import su.aurora.util.ColorUtil;
import su.aurora.util.Render2D;

/**
 * Radar — круговой радар: игроки, мобы и предметы вокруг в реальном времени.
 */
public class RadarModule extends Module {
    private final ModeSetting position = addMode("Позиция", "Слева сверху", "Слева сверху", "Справа сверху",
            "Слева снизу", "Справа снизу");
    private final NumberSetting size = addNumber("Размер", 62, 40, 120, 2);
    private final NumberSetting range = addNumber("Дистанция", 32, 8, 96, 4);
    private final BooleanSetting players = addBoolean("Игроки", true);
    private final BooleanSetting mobs = addBoolean("Мобы", false);
    private final BooleanSetting items = addBoolean("Предметы", false);
    private final BooleanSetting grid = addBoolean("Сетка", true);
    private final ColorSetting accent = addColor("Акцент", 0xFF7C5CFF);

    public RadarModule() {
        super("Radar", "Круговой радар игроков и мобов", Category.HUD);
    }

    @Override
    public void onHudRender(GuiGraphics graphics, DeltaTracker delta) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null || client.level == null || client.options.hideGui) {
            return;
        }
        int screenWidth = client.getWindow().getGuiScaledWidth();
        int screenHeight = client.getWindow().getGuiScaledHeight();
        int diameter = size.getInt();
        int x = switch (position.get()) {
            case "Справа сверху", "Справа снизу" -> screenWidth - diameter - 8;
            default -> 8;
        };
        int y = switch (position.get()) {
            case "Слева снизу", "Справа снизу" -> screenHeight - diameter - 8;
            default -> 8;
        };

        Render2D.roundedRect(graphics, x, y, diameter, diameter, diameter / 2, 0xB30C0E14);
        if (grid.get()) {
            Render2D.roundedOutline(graphics, x, y, diameter, diameter, diameter / 2,
                    ColorUtil.withAlpha(accent.get(), 90), 1.0f);
            Render2D.rect(graphics, x + diameter / 2f - 0.5f, y + 6, 1, diameter - 12,
                    ColorUtil.withAlpha(accent.get(), 45));
            Render2D.rect(graphics, x + 6, y + diameter / 2f - 0.5f, diameter - 12, 1,
                    ColorUtil.withAlpha(accent.get(), 45));
        }

        float yaw = (float) Math.toRadians(client.player.getYRot());
        double scale = (diameter / 2.0 - 5) / range.get();
        int centerX = x + diameter / 2;
        int centerY = y + diameter / 2;

        for (Entity entity : client.level.entitiesForRendering()) {
            if (entity == client.player) {
                continue;
            }
            boolean isPlayer = entity instanceof Player;
            boolean isItem = entity instanceof ItemEntity;
            if (isPlayer && !players.get()) {
                continue;
            }
            if (isItem && !items.get()) {
                continue;
            }
            if (!isPlayer && !isItem && !(entity instanceof LivingEntity) ) {
                continue;
            }
            if (!isPlayer && !isItem && !mobs.get()) {
                continue;
            }
            double dx = entity.getX() - client.player.getX();
            double dz = entity.getZ() - client.player.getZ();
            double distance = Math.sqrt(dx * dx + dz * dz);
            if (distance > range.get()) {
                continue;
            }
            double rotatedX = dx * Math.cos(-yaw) - dz * Math.sin(-yaw);
            double rotatedZ = dx * Math.sin(-yaw) + dz * Math.cos(-yaw);
            int dotX = centerX + (int) (rotatedX * scale);
            int dotY = centerY + (int) (rotatedZ * scale);
            if (Math.abs(dotX - centerX) > diameter / 2 - 3 || Math.abs(dotY - centerY) > diameter / 2 - 3) {
                continue;
            }
            int dotColor = isPlayer ? 0xFFFF5C7C : isItem ? 0xFFFFD166 : 0xFFFFF0A0;
            graphics.fill(dotX - 1, dotY - 1, dotX + 2, dotY + 2, dotColor);
        }

        // метка севера
        int northX = centerX + (int) (Math.sin(yaw) * (diameter / 2.0 - 5));
        int northY = centerY - (int) (Math.cos(yaw) * (diameter / 2.0 - 5));
        graphics.fill(northX - 1, northY - 1, northX + 2, northY + 2, ColorUtil.withAlpha(accent.get(), 220));
        Render2D.textCentered(graphics, "N", northX, northY - 8, ColorUtil.withAlpha(accent.get(), 200));
    }
}
