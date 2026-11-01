package su.aurora.modules.hud;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import su.aurora.core.Category;
import su.aurora.core.Module;
import su.aurora.setting.BooleanSetting;
import su.aurora.setting.ColorSetting;
import su.aurora.setting.ModeSetting;
import su.aurora.setting.NumberSetting;
import su.aurora.util.ColorUtil;
import su.aurora.util.PlayerUtil;
import su.aurora.util.Render2D;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * BetterDebug — свой оверлей вместо ванильного F3: две аккуратные панели
 * с производительностью, координатами, миром и целью.
 * Ванильный F3 прячется миксином в DebugScreenOverlay#render.
 */
public class BetterDebugModule extends Module {
    private final ModeSetting visibility = addMode("Показывать", "Только по F3", "Только по F3", "Всегда");
    private final BooleanSetting hideVanilla = addBoolean("Скрыть ванильный F3", true);
    private final BooleanSetting performance = addBoolean("Производительность", true);
    private final BooleanSetting position = addBoolean("Позиция", true);
    private final BooleanSetting world = addBoolean("Мир", true);
    private final BooleanSetting target = addBoolean("Цель", true);
    private final BooleanSetting entities = addBoolean("Сущности", true);
    private final BooleanSetting compact = addBoolean("Компактно", false);
    private final NumberSetting opacity = addNumber("Прозрачность", 190, 40, 255, 5);
    private final ColorSetting accent = addColor("Акцент", 0xFF7C5CFF);

    private int tickCounter;
    private int tps = 20;
    private long lastTpsSample = System.currentTimeMillis();
    private int entityCount;
    private String biome = "—";
    private String dimension = "—";
    private int entityCooldown;

    public BetterDebugModule() {
        super("BetterDebug", "Свой оверлей вместо ванильного F3", Category.HUD);
    }

    public boolean hideVanillaOverlay() {
        return isEnabled() && hideVanilla.get();
    }

    /** Показывать ли наш оверлей прямо сейчас. */
    private boolean visibleNow() {
        if (!isEnabled()) {
            return false;
        }
        if (visibility.is("Всегда")) {
            return true;
        }
        try {
            return Minecraft.getInstance().getDebugOverlay().showDebugScreen();
        } catch (Throwable throwable) {
            return false;
        }
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
        Minecraft client = Minecraft.getInstance();
        if (client.level == null || client.player == null) {
            return;
        }
        if (++entityCooldown >= 10) {
            entityCooldown = 0;
            int count = 0;
            for (Entity ignored : client.level.entitiesForRendering()) {
                count++;
            }
            entityCount = count;
            BlockPos pos = client.player.blockPosition();
            biome = client.level.getBiome(pos).unwrapKey()
                    .map(key -> key.identifier().getPath()).orElse("—");
            dimension = client.level.dimension().identifier().getPath();
        }
    }

    private static String facing(float yaw) {
        String[] names = {"Юг", "Юго-запад", "Запад", "Северо-запад", "Север", "Северо-восток", "Восток",
                "Юго-восток"};
        int index = Math.round(((yaw % 360f) + 360f) % 360f / 45f) % 8;
        return names[index];
    }

    private static String directionLetter(Direction direction) {
        return switch (direction) {
            case NORTH -> "-Z";
            case SOUTH -> "+Z";
            case WEST -> "-X";
            case EAST -> "+X";
            case UP -> "+Y";
            case DOWN -> "-Y";
        };
    }

    private List<String> leftColumn(Minecraft client) {
        List<String> lines = new ArrayList<>();
        if (performance.get()) {
            Runtime runtime = Runtime.getRuntime();
            long used = (runtime.totalMemory() - runtime.freeMemory()) / (1024L * 1024L);
            long max = runtime.maxMemory() / (1024L * 1024L);
            lines.add("FPS  " + client.getFps());
            lines.add("Пинг  " + PlayerUtil.ping() + " ms");
            lines.add("TPS  " + tps);
            lines.add("Память  " + used + " / " + max + " МБ");
        }
        if (entities.get()) {
            lines.add("Сущности  " + entityCount);
        }
        if (performance.get() || entities.get()) {
            lines.add("Модулей  " + (su.aurora.AuroraClient.MODULES == null ? 0
                    : su.aurora.AuroraClient.MODULES.all().size()));
        }
        return lines;
    }

    private List<String> rightColumn(Minecraft client) {
        List<String> lines = new ArrayList<>();
        if (client.player == null || client.level == null) {
            return lines;
        }
        if (position.get()) {
            lines.add(String.format(Locale.ROOT, "XYZ  %.1f  %.1f  %.1f", client.player.getX(),
                    client.player.getY(), client.player.getZ()));
            BlockPos pos = client.player.blockPosition();
            lines.add("Блок  " + pos.getX() + " " + pos.getY() + " " + pos.getZ());
            lines.add("Чанк  " + (pos.getX() >> 4) + " " + (pos.getZ() >> 4));
            double speed = client.player.getDeltaMovement().horizontalDistance() * 20.0;
            lines.add(String.format(Locale.ROOT, "Взгляд  %s (%.0f°)  %.1f бл/с", facing(client.player.getYRot()),
                    ((client.player.getYRot() % 360f) + 360f) % 360f, speed));
        }
        if (world.get()) {
            long dayTime = client.level.getDayTime();
            lines.add("Мир  " + dimension + " · " + biome);
            lines.add(String.format(Locale.ROOT, "Время  %02d:%02d  ·  день %d",
                    (int) ((dayTime / 1000L + 6) % 24), (int) ((dayTime % 1000) * 60 / 1000),
                    dayTime / 24000L + 1));
        }
        if (target.get()) {
            HitResult hit = client.hitResult;
            if (hit instanceof BlockHitResult blockHit && hit.getType() == HitResult.Type.BLOCK) {
                BlockPos pos = blockHit.getBlockPos();
                String blockId = client.level.getBlockState(pos).getBlock().getName().getString();
                double distance = client.player.getEyePosition().distanceTo(hit.getLocation());
                lines.add(String.format(Locale.ROOT, "Цель  %s  %s  (%.2f м)", blockId,
                        directionLetter(blockHit.getDirection()), distance));
            } else {
                lines.add("Цель  —");
            }
        }
        return lines;
    }

    @Override
    public void onHudRender(GuiGraphics graphics, DeltaTracker delta) {
        if (!visibleNow()) {
            return;
        }
        Minecraft client = Minecraft.getInstance();
        if (client.options.hideGui) {
            return;
        }
        List<String> left = leftColumn(client);
        List<String> right = rightColumn(client);
        if (left.isEmpty() && right.isEmpty()) {
            return;
        }
        int padding = compact.get() ? 5 : 8;
        int lineHeight = compact.get() ? 9 : 11;
        int gap = 8;

        int leftWidth = 0;
        for (String line : left) {
            leftWidth = Math.max(leftWidth, Render2D.width(line));
        }
        int rightWidth = 0;
        for (String line : right) {
            rightWidth = Math.max(rightWidth, Render2D.width(line));
        }
        int leftHeight = Math.max(1, left.size()) * lineHeight;
        int rightHeight = Math.max(1, right.size()) * lineHeight;

        int accentColor = HudStyleModule.resolve(accent);
        int background = ColorUtil.withAlpha(0x0C0E14, opacity.getInt());

        if (!left.isEmpty()) {
            drawPanel(graphics, padding, left, padding, leftWidth + padding * 2, leftHeight + padding * 2,
                    background, accentColor, lineHeight, padding);
        }
        if (!right.isEmpty()) {
            int x = left.isEmpty() ? padding : padding + leftWidth + padding * 2 + gap;
            drawPanel(graphics, x, right, padding, rightWidth + padding * 2, rightHeight + padding * 2,
                    background, accentColor, lineHeight, padding);
        }
    }

    private void drawPanel(GuiGraphics graphics, int x, List<String> lines, int padding, int width, int height,
                           int background, int accent, int lineHeight, int innerPadding) {
        Render2D.roundedRect(graphics, x, padding, width, height, 5, background);
        Render2D.roundedRect(graphics, x, padding, 2, height, 1, accent);
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            int color = i == 0 ? accent : ColorUtil.withAlpha(0xFFFFFF, 225);
            Render2D.textShadow(graphics, line, x + innerPadding, padding + innerPadding + i * lineHeight, color);
        }
    }
}
