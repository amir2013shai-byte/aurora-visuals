package su.aurora.modules.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import su.aurora.core.Category;
import su.aurora.core.Module;
import su.aurora.setting.BooleanSetting;
import su.aurora.setting.ColorSetting;
import su.aurora.setting.NumberSetting;
import su.aurora.setting.StringSetting;
import su.aurora.util.ColorUtil;
import su.aurora.util.Render3D;

import java.util.ArrayList;
import java.util.List;

/**
 * Waypoints — метки в мире: луч вверх, ромб на поверхности и подпись с дистанцией.
 * Формат списка: "имя:x:y:z;имя2:x:y:z".
 */
public class WaypointsModule extends Module {
    private final StringSetting points = addString("Точки", "");
    private final ColorSetting color = addColor("Цвет", 0xFF7C5CFF);
    private final BooleanSetting beam = addBoolean("Луч", true);
    private final BooleanSetting labels = addBoolean("Подписи", true);
    private final BooleanSetting distance = addBoolean("Дистанция", true);
    private final NumberSetting height = addNumber("Высота луча", 40, 4, 160, 4);
    private final NumberSetting width = addNumber("Толщина", 0.12, 0.02, 0.6, 0.02);

    public WaypointsModule() {
        super("Waypoints", "Метки в мире: луч, ромб и подпись с дистанцией", Category.RENDER);
    }

    private record Point(String name, double x, double y, double z) {
    }

    private List<Point> parse() {
        List<Point> result = new ArrayList<>();
        String raw = points.get();
        if (raw == null || raw.isBlank()) {
            return result;
        }
        for (String part : raw.split(";")) {
            String[] split = part.trim().split(":");
            if (split.length < 4) {
                continue;
            }
            try {
                result.add(new Point(split[0], Double.parseDouble(split[1]), Double.parseDouble(split[2]),
                        Double.parseDouble(split[3])));
            } catch (NumberFormatException ignored) {
            }
        }
        return result;
    }

    @Override
    public void onWorldRender(WorldRenderContext context) {
        List<Point> list = parse();
        if (list.isEmpty()) {
            return;
        }
        Minecraft client = Minecraft.getInstance();
        Vec3 camera = Render3D.camera();
        int tint = color.get();
        double w = width.get();
        double h = height.get();

        for (Point point : list) {
            Vec3 base = new Vec3(point.x(), point.y(), point.z());
            Vec3 a = base.add(-w, 0, -w);
            Vec3 b = base.add(w, 0, -w);
            Vec3 c = base.add(w, 0, w);
            Vec3 d = base.add(-w, 0, w);
            Render3D.quads(buffer -> {
                if (beam.get()) {
                    Vec3 topA = a.add(0, h, 0);
                    Vec3 topB = b.add(0, h, 0);
                    Vec3 topC = c.add(0, h, 0);
                    Vec3 topD = d.add(0, h, 0);
                    buffer.quad(a, b, b.add(0, h, 0), a.add(0, h, 0), ColorUtil.withAlpha(tint, 26));
                    buffer.quad(b, c, c.add(0, h, 0), b.add(0, h, 0), ColorUtil.withAlpha(tint, 26));
                    buffer.quad(c, d, d.add(0, h, 0), c.add(0, h, 0), ColorUtil.withAlpha(tint, 26));
                    buffer.quad(d, a, a.add(0, h, 0), d.add(0, h, 0), ColorUtil.withAlpha(tint, 26));
                    buffer.quad(topA, topD, topC, topB, ColorUtil.withAlpha(tint, 18));
                }
                buffer.quad(a, b, c, d, ColorUtil.withAlpha(tint, 90));
            });
            Render3D.lines(buffer -> {
                buffer.diamond(base.add(0, 0.9, 0), 0.45, tint, 2.0f);
                if (beam.get()) {
                    buffer.line(base, base.add(0, h, 0), ColorUtil.withAlpha(tint, 120), 1.6f);
                }
            });

            if (labels.get()) {
                StringBuilder text = new StringBuilder(point.name());
                if (distance.get() && client.player != null) {
                    text.append(String.format(java.util.Locale.ROOT, "  %.0f m", client.player.position().distanceTo(base)));
                }
                try {
                    PoseStack matrices = context.matrices();
                    matrices.pushPose();
                    matrices.translate(point.x() - camera.x, point.y() + 1.6 - camera.y, point.z() - camera.z);
                    matrices.mulPose(client.gameRenderer.getMainCamera().rotation());
                    float scale = -0.025f;
                    matrices.scale(scale, scale, scale);
                    int textWidth = client.font.width(text.toString());
                    context.commandQueue().submitText(matrices, -textWidth / 2f, 0f,
                            Component.literal(text.toString()).getVisualOrderText(), true,
                            Font.DisplayMode.SEE_THROUGH, 0xF000F0, tint, 0, 0);
                    matrices.popPose();
                } catch (Throwable throwable) {
                    su.aurora.Aurora.LOG.warn("[Waypoints] подпись не нарисовалась: {}", throwable.getMessage());
                    return;
                }
            }
        }
    }

    /** Ближайшая точка к игроку — для HUD-стрелки (используется другими модулями). */
    public Vec3 nearest() {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null) {
            return null;
        }
        Point best = null;
        double bestDistance = Double.MAX_VALUE;
        for (Point point : parse()) {
            double d = client.player.position().distanceToSqr(point.x(), point.y(), point.z());
            if (d < bestDistance) {
                bestDistance = d;
                best = point;
            }
        }
        return best == null ? null : new Vec3(best.x(), best.y(), best.z());
    }

    /** Габарит для отладки/редактора. */
    public AABB markerBox(Point point) {
        return new AABB(point.x() - 0.5, point.y(), point.z() - 0.5, point.x() + 0.5, point.y() + 1, point.z() + 0.5);
    }
}
