package su.aurora.modules.render;

import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import su.aurora.core.Category;
import su.aurora.core.Module;
import su.aurora.setting.BooleanSetting;
import su.aurora.setting.ColorSetting;
import su.aurora.setting.ModeSetting;
import su.aurora.setting.NumberSetting;
import su.aurora.util.ColorUtil;
import su.aurora.util.Render3D;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Trails — цветной след за игроком. Работает как для себя, так и для других игроков.
 */
public class TrailsModule extends Module {
    private final ModeSetting view = addMode("Вид", "Линия", "Линия", "Лента", "Призраки");
    private final ModeSetting colorMode = addMode("Цвет", "Радуга", "Радуга", "Градиент", "Обычный");
    private final ColorSetting color = addColor("Основной цвет", 0xFF7C5CFF);
    private final NumberSetting length = addInt("Длина", 45, 5, 120);
    private final NumberSetting thickness = addNumber("Толщина", 2.5, 1.0, 6.0, 0.1);
    private final BooleanSetting allPlayers = addBoolean("Все игроки", false);
    private final BooleanSetting onlyMoving = addBoolean("Только в движении", true);
    private final BooleanSetting particles = addBoolean("Частицы", true);

    private final Map<UUID, Deque<Vec3>> trails = new HashMap<>();

    public TrailsModule() {
        super("Trails", "Цветной след за игроком", Category.RENDER);
    }

    @Override
    public void onDisable() {
        trails.clear();
    }

    @Override
    public void onTick() {
        Minecraft client = Minecraft.getInstance();
        if (client.level == null || client.player == null) {
            return;
        }
        if (!allPlayers.get()) {
            trails.keySet().removeIf(uuid -> !uuid.equals(client.player.getUUID()));
            addPoint(client.player);
            return;
        }
        for (Player player : client.level.players()) {
            addPoint(player);
        }
        trails.keySet().removeIf(uuid -> client.level.getPlayerByUUID(uuid) == null);
    }

    private void addPoint(Player player) {
        if (onlyMoving.get() && player.getDeltaMovement().lengthSqr() < 0.0009 && player.onGround()) {
            return;
        }
        Deque<Vec3> points = trails.computeIfAbsent(player.getUUID(), uuid -> new ArrayDeque<>());
        Vec3 point = new Vec3(player.getX(), player.getY() + 0.12, player.getZ());
        if (points.isEmpty() || points.peekLast().distanceTo(point) > 0.05) {
            points.addLast(point);
            if (particles.get() && player == Minecraft.getInstance().player
                    && Minecraft.getInstance().level != null
                    && Minecraft.getInstance().level.random.nextInt(3) == 0) {
                int tint = currentColor(0);
                Minecraft.getInstance().level.addParticle(
                        net.minecraft.core.particles.ParticleTypes.END_ROD,
                        point.x + (Math.random() - 0.5) * 0.3,
                        point.y + 0.1,
                        point.z + (Math.random() - 0.5) * 0.3,
                        0, 0.01, 0);
            }
        }
        while (points.size() > length.getInt()) {
            points.removeFirst();
        }
    }

    private int currentColor(int index) {
        return switch (colorMode.get()) {
            case "Радуга" -> ColorUtil.rainbow(4000L, 0.7f, 1f, 220);
            case "Градиент" -> ColorUtil.lerp(color.get(), ColorUtil.withAlpha(0xFFFFFFFF, 60), index / 40f);
            default -> color.get();
        };
    }

    @Override
    public void onWorldRender(WorldRenderContext context) {
        if (trails.isEmpty()) {
            return;
        }
        for (Map.Entry<UUID, Deque<Vec3>> entry : trails.entrySet()) {
            List<Vec3> points = new ArrayList<>(entry.getValue());
            if (points.size() < 2) {
                continue;
            }
            switch (view.get()) {
                case "Лента" -> Render3D.quads(buffer -> {
                    for (int i = 1; i < points.size(); i++) {
                        float progress = i / (float) points.size();
                        Vec3 a = points.get(i - 1);
                        Vec3 b = points.get(i);
                        int color = ColorUtil.scaleAlpha(currentColor(i), 0.12f + progress * 0.75f);
                        double top = 0.32 * (0.4 + progress * 0.6);
                        buffer.quad(a.add(0, top, 0), b.add(0, top, 0), b, a, color);
                    }
                });
                case "Призраки" -> Render3D.lines(buffer -> {
                    for (int i = 0; i < points.size(); i += 6) {
                        float progress = i / (float) points.size();
                        Vec3 point = points.get(i);
                        int color = ColorUtil.scaleAlpha(currentColor(i), 0.12f + progress * 0.65f);
                        buffer.box(new AABB(point.x - 0.3, point.y, point.z - 0.3,
                                point.x + 0.3, point.y + 1.8, point.z + 0.3), color, 1.6f);
                    }
                });
                default -> Render3D.lines(buffer -> {
                    for (int i = 1; i < points.size(); i++) {
                        float progress = i / (float) points.size();
                        int color = ColorUtil.scaleAlpha(currentColor(i), 0.15f + progress * 0.85f);
                        buffer.line(points.get(i - 1), points.get(i), color,
                                thickness.getFloat() * (0.35f + progress * 0.65f));
                    }
                });
            }
        }
    }
}
