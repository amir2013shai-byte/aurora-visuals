package su.aurora.modules.render;

import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import su.aurora.core.Category;
import su.aurora.core.Module;
import su.aurora.setting.BooleanSetting;
import su.aurora.setting.ColorSetting;
import su.aurora.setting.NumberSetting;
import su.aurora.util.ColorUtil;
import su.aurora.util.Render3D;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Footprints — следы ног на земле. Позиции собираются в тик, рисуются затухающими квадами.
 */
public class FootprintsModule extends Module {
    private final NumberSetting life = addNumber("Время жизни", 4.0, 0.5, 15.0, 0.5);
    private final NumberSetting size = addNumber("Размер", 0.22, 0.05, 0.8, 0.01);
    private final NumberSetting maxSteps = addNumber("Максимум шагов", 40, 4, 200, 4);
    private final BooleanSetting onlyPlayers = addBoolean("Только игроки", true);
    private final BooleanSetting self = addBoolean("Свои следы", true);
    private final ColorSetting color = addColor("Цвет", 0xFFFFFFFF);
    private final NumberSetting range = addNumber("Дистанция записи", 24, 4, 64, 1);

    private final Map<UUID, Deque<Step>> steps = new HashMap<>();

    private record Step(Vec3 left, Vec3 right, long time, float yaw) {
    }

    public FootprintsModule() {
        super("Footprints", "Следы ног на земле за игроками", Category.RENDER);
    }

    @Override
    public void onDisable() {
        steps.clear();
    }

    @Override
    public void onTick() {
        Minecraft client = Minecraft.getInstance();
        if (client.level == null || client.player == null) {
            return;
        }
        double rangeSq = range.get() * range.get();
        for (Entity entity : client.level.entitiesForRendering()) {
            if (!(entity instanceof LivingEntity living) || !living.onGround()) {
                continue;
            }
            if (!self.get() && living == client.player) {
                continue;
            }
            if (onlyPlayers.get() && !(living instanceof Player)) {
                continue;
            }
            if (living == client.player || living.distanceToSqr(client.player) > rangeSq) {
                continue;
            }
            if (living.walkAnimation.speed() < 0.05f) {
                continue;
            }
            Deque<Step> queue = steps.computeIfAbsent(living.getUUID(), key -> new ArrayDeque<>());
            long now = System.currentTimeMillis();
            if (!queue.isEmpty() && now - queue.peekLast().time() < 350) {
                continue;
            }
            float yaw = living.getYRot() * (float) Math.PI / 180f;
            Vec3 forward = new Vec3(-Math.sin(yaw), 0, Math.cos(yaw));
            Vec3 right = new Vec3(Math.cos(yaw), 0, Math.sin(yaw));
            double width = 0.16;
            Vec3 center = new Vec3(living.getX(), living.getY() + 0.02, living.getZ());
            Vec3 left = center.add(right.scale(-width));
            Vec3 rightPoint = center.add(right.scale(width));
            queue.addLast(new Step(left, rightPoint, now, yaw));
            while (queue.size() > maxSteps.getInt()) {
                queue.removeFirst();
            }
        }
        long limit = (long) (life.get() * 1000);
        for (Iterator<Deque<Step>> it = steps.values().iterator(); it.hasNext(); ) {
            Deque<Step> queue = it.next();
            queue.removeIf(step -> System.currentTimeMillis() - step.time() > limit);
            if (queue.isEmpty()) {
                it.remove();
            }
        }
    }

    @Override
    public void onWorldRender(WorldRenderContext context) {
        long limit = (long) (life.get() * 1000);
        float s = size.getFloat();
        int base = color.get();
        for (Deque<Step> queue : steps.values()) {
            for (Step step : queue) {
                long age = System.currentTimeMillis() - step.time();
                float fade = 1f - Math.min(1f, age / (float) Math.max(1, limit));
                int tint = ColorUtil.withAlpha(base, (int) (255 * fade * 0.75f));
                Vec3 dir = new Vec3(-Math.sin(step.yaw()), 0, Math.cos(step.yaw()));
                Vec3 side = new Vec3(Math.cos(step.yaw()), 0, Math.sin(step.yaw()));
                for (Vec3 point : List.of(step.left(), step.right())) {
                    Vec3 a = point.add(dir.scale(-s));
                    Vec3 b = point.add(dir.scale(s));
                    Vec3 c = b.add(side.scale(s * 0.55));
                    Vec3 d = a.add(side.scale(s * 0.55));
                    List<Vec3> quad = new ArrayList<>(List.of(a, b, c, d));
                    Render3D.quads(buffer -> buffer.quad(quad.get(0), quad.get(1), quad.get(2), quad.get(3), tint));
                }
            }
        }
    }
}
