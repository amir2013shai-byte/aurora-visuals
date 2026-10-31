package su.aurora.util;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import su.aurora.Aurora;
import su.aurora.AuroraClient;
import su.aurora.core.Module;

/**
 * Рисование в мире. В 1.21.11 вся геометрия мира проходит через
 * OrderedSubmitNodeCollector, поэтому каждый модуль собирает свою геометрию
 * в один submitCustomGeometry-вызов.
 */
public final class Render3D {
    private static WorldRenderContext context;
    private static Vec3 camera = Vec3.ZERO;
    private static float tickDelta;

    private Render3D() {
    }

    public static void onWorldRender(WorldRenderContext ctx) {
        Minecraft client = Minecraft.getInstance();
        if (client.level == null || client.player == null || AuroraClient.MODULES == null) {
            return;
        }
        context = ctx;
        camera = client.gameRenderer.getMainCamera().position();
        tickDelta = client.getDeltaTracker().getGameTimeDeltaPartialTick(true);

        for (Module module : AuroraClient.MODULES.all()) {
            if (!module.isEnabled()) {
                continue;
            }
            try {
                module.onWorldRender(ctx);
            } catch (Throwable throwable) {
                Aurora.LOG.error("[{}] Ошибка мирового рендера", module.getName(), throwable);
            }
        }
        context = null;
    }

    public static float tickDelta() {
        return tickDelta;
    }

    public static Vec3 camera() {
        return camera;
    }

    public static double partialX(net.minecraft.world.entity.Entity entity) {
        return entity.getX(tickDelta);
    }

    public static double partialY(net.minecraft.world.entity.Entity entity) {
        return entity.getY(tickDelta);
    }

    public static double partialZ(net.minecraft.world.entity.Entity entity) {
        return entity.getZ(tickDelta);
    }

    /** Относительные координаты (мир -> камера). */
    public static Vec3 rel(double x, double y, double z) {
        return new Vec3(x - camera.x, y - camera.y, z - camera.z);
    }

    /* ------------------------- линии ------------------------- */

    public interface LineBuilder {
        void build(LineBuffer buffer);
    }

    public static void lines(LineBuilder builder) {
        if (context == null) {
            return;
        }
        submit(RenderTypes.LINES_TRANSLUCENT, (pose, consumer) -> builder.build(new LineBuffer(pose, consumer)));
    }

    public static void linesSolid(LineBuilder builder) {
        if (context == null) {
            return;
        }
        submit(RenderTypes.LINES, (pose, consumer) -> builder.build(new LineBuffer(pose, consumer)));
    }

    /* ------------------------- заливка ------------------------- */

    public interface QuadBuilder {
        void build(QuadBuffer buffer);
    }

    public static void quads(QuadBuilder builder) {
        if (context == null) {
            return;
        }
        submit(RenderTypes.debugQuads(), (pose, consumer) -> builder.build(new QuadBuffer(pose, consumer)));
    }

    private static void submit(RenderType type,
                               net.minecraft.client.renderer.SubmitNodeCollector.CustomGeometryRenderer renderer) {
        context.commandQueue().submitCustomGeometry(context.matrices(), type, renderer);
    }

    /** Буфер линий: все координаты передаются в мировых координатах. */
    public static final class LineBuffer {
        private final PoseStack.Pose pose;
        private final VertexConsumer consumer;

        private LineBuffer(PoseStack.Pose pose, VertexConsumer consumer) {
            this.pose = pose;
            this.consumer = consumer;
        }

        private void vertex(Vec3 position, int color, float width, float nx, float ny, float nz) {
            consumer.addVertex(pose, (float) (position.x - camera.x), (float) (position.y - camera.y),
                            (float) (position.z - camera.z))
                    .setColor(color)
                    .setNormal(pose, nx, ny, nz)
                    .setLineWidth(width);
        }

        public void line(Vec3 from, Vec3 to, int color, float width) {
            Vec3 direction = to.subtract(from).normalize();
            vertex(from, color, width, (float) direction.x, (float) direction.y, (float) direction.z);
            vertex(to, color, width, (float) direction.x, (float) direction.y, (float) direction.z);
        }

        public void line(double x1, double y1, double z1, double x2, double y2, double z2, int color, float width) {
            line(new Vec3(x1, y1, z1), new Vec3(x2, y2, z2), color, width);
        }

        public void box(AABB box, int color, float width) {
            double minX = box.minX, minY = box.minY, minZ = box.minZ;
            double maxX = box.maxX, maxY = box.maxY, maxZ = box.maxZ;
            // низ
            line(minX, minY, minZ, maxX, minY, minZ, color, width);
            line(maxX, minY, minZ, maxX, minY, maxZ, color, width);
            line(maxX, minY, maxZ, minX, minY, maxZ, color, width);
            line(minX, minY, maxZ, minX, minY, minZ, color, width);
            // верх
            line(minX, maxY, minZ, maxX, maxY, minZ, color, width);
            line(maxX, maxY, minZ, maxX, maxY, maxZ, color, width);
            line(maxX, maxY, maxZ, minX, maxY, maxZ, color, width);
            line(minX, maxY, maxZ, minX, maxY, minZ, color, width);
            // стойки
            line(minX, minY, minZ, minX, maxY, minZ, color, width);
            line(maxX, minY, minZ, maxX, maxY, minZ, color, width);
            line(maxX, minY, maxZ, maxX, maxY, maxZ, color, width);
            line(minX, minY, maxZ, minX, maxY, maxZ, color, width);
        }

        public void circle(Vec3 center, double radius, int color, float width, int segments) {
            for (int i = 0; i < segments; i++) {
                double a1 = (Math.PI * 2 * i) / segments;
                double a2 = (Math.PI * 2 * (i + 1)) / segments;
                line(center.x + Math.cos(a1) * radius, center.y, center.z + Math.sin(a1) * radius,
                        center.x + Math.cos(a2) * radius, center.y, center.z + Math.sin(a2) * radius, color, width);
            }
        }

        public void circleY(Vec3 center, double radius, int color, float width, int segments, double yOffset) {
            circle(center.add(0, yOffset, 0), radius, color, width, segments);
        }

        /** Наклонённое кольцо — «орбита» вокруг цели. */
        public void orbit(Vec3 center, double radius, int color, float width, int segments, double tilt, double phase) {
            for (int i = 0; i < segments; i++) {
                double a1 = (Math.PI * 2 * i) / segments + phase;
                double a2 = (Math.PI * 2 * (i + 1)) / segments + phase;
                line(center.x + Math.cos(a1) * radius, center.y + Math.sin(a1) * radius * tilt,
                        center.z + Math.sin(a1) * radius, center.x + Math.cos(a2) * radius,
                        center.y + Math.sin(a2) * radius * tilt, center.z + Math.sin(a2) * radius, color, width);
            }
        }

        public void diamond(Vec3 center, double size, int color, float width) {
            Vec3 top = center.add(0, size, 0);
            Vec3 bottom = center.add(0, -size, 0);
            Vec3[] ring = {
                    center.add(size, 0, 0),
                    center.add(0, 0, size),
                    center.add(-size, 0, 0),
                    center.add(0, 0, -size)
            };
            for (int i = 0; i < 4; i++) {
                line(top.x, top.y, top.z, ring[i].x, ring[i].y, ring[i].z, color, width);
                line(bottom.x, bottom.y, bottom.z, ring[i].x, ring[i].y, ring[i].z, color, width);
                Vec3 next = ring[(i + 1) % 4];
                line(ring[i].x, ring[i].y, ring[i].z, next.x, next.y, next.z, color, width);
            }
        }

        public void crystal(Vec3 center, double size, int color, float width) {
            diamond(center, size, color, width);
            Vec3 offset = center.add(size * 0.6, size * 0.4, size * 0.6);
            diamond(offset, size * 0.45, color, width);
        }

        public void pyramid(Vec3 center, double size, double height, int color, float width, boolean inverted) {
            double baseY = inverted ? center.y : center.y - height / 2;
            double topY = inverted ? center.y - height : center.y + height / 2;
            Vec3[] base = {
                    new Vec3(center.x + size, baseY, center.z + size),
                    new Vec3(center.x - size, baseY, center.z + size),
                    new Vec3(center.x - size, baseY, center.z - size),
                    new Vec3(center.x + size, baseY, center.z - size)
            };
            Vec3 top = new Vec3(center.x, topY, center.z);
            for (int i = 0; i < 4; i++) {
                Vec3 next = base[(i + 1) % 4];
                line(base[i].x, base[i].y, base[i].z, next.x, next.y, next.z, color, width);
                line(base[i].x, base[i].y, base[i].z, top.x, top.y, top.z, color, width);
            }
        }

        public void cross(Vec3 center, double size, int color, float width) {
            line(center.x - size, center.y, center.z, center.x + size, center.y, center.z, color, width);
            line(center.x, center.y - size, center.z, center.x, center.y + size, center.z, color, width);
            line(center.x, center.y, center.z - size, center.x, center.y, center.z + size, color, width);
        }

        public void polygon(Vec3 center, double radius, int sides, int color, float width, double phase) {
            Vec3[] points = new Vec3[sides];
            for (int i = 0; i < sides; i++) {
                double angle = (Math.PI * 2 * i) / sides + phase;
                points[i] = center.add(Math.cos(angle) * radius, 0, Math.sin(angle) * radius);
            }
            for (int i = 0; i < sides; i++) {
                Vec3 next = points[(i + 1) % sides];
                line(points[i].x, points[i].y, points[i].z, next.x, next.y, next.z, color, width);
            }
        }
    }

    /** Буфер заливки (debugQuads), рисует двусторонние полигоны. */
    public static final class QuadBuffer {
        private final PoseStack.Pose pose;
        private final VertexConsumer consumer;

        private QuadBuffer(PoseStack.Pose pose, VertexConsumer consumer) {
            this.pose = pose;
            this.consumer = consumer;
        }

        private void vertex(Vec3 position, int color) {
            consumer.addVertex(pose, (float) (position.x - camera.x), (float) (position.y - camera.y),
                    (float) (position.z - camera.z)).setColor(color);
        }

        public void quad(Vec3 a, Vec3 b, Vec3 c, Vec3 d, int color) {
            vertex(a, color);
            vertex(b, color);
            vertex(c, color);
            vertex(d, color);
            // обратная сторона
            vertex(d, color);
            vertex(c, color);
            vertex(b, color);
            vertex(a, color);
        }

        public void face(AABB box, int color) {
            box(box, color);
        }

        public void box(AABB box, int color) {
            Vec3 p1 = new Vec3(box.minX, box.minY, box.minZ);
            Vec3 p2 = new Vec3(box.maxX, box.minY, box.minZ);
            Vec3 p3 = new Vec3(box.maxX, box.minY, box.maxZ);
            Vec3 p4 = new Vec3(box.minX, box.minY, box.maxZ);
            Vec3 p5 = new Vec3(box.minX, box.maxY, box.minZ);
            Vec3 p6 = new Vec3(box.maxX, box.maxY, box.minZ);
            Vec3 p7 = new Vec3(box.maxX, box.maxY, box.maxZ);
            Vec3 p8 = new Vec3(box.minX, box.maxY, box.maxZ);
            quad(p1, p2, p3, p4, color);
            quad(p5, p8, p7, p6, color);
            quad(p1, p5, p6, p2, color);
            quad(p2, p6, p7, p3, color);
            quad(p3, p7, p8, p4, color);
            quad(p4, p8, p5, p1, color);
        }

        public void ring(Vec3 center, double innerRadius, double outerRadius, int color, int segments) {
            for (int i = 0; i < segments; i++) {
                double a1 = (Math.PI * 2 * i) / segments;
                double a2 = (Math.PI * 2 * (i + 1)) / segments;
                Vec3 a = center.add(Math.cos(a1) * innerRadius, 0, Math.sin(a1) * innerRadius);
                Vec3 b = center.add(Math.cos(a1) * outerRadius, 0, Math.sin(a1) * outerRadius);
                Vec3 c = center.add(Math.cos(a2) * outerRadius, 0, Math.sin(a2) * outerRadius);
                Vec3 d = center.add(Math.cos(a2) * innerRadius, 0, Math.sin(a2) * innerRadius);
                quad(a, b, c, d, color);
            }
        }

        public void disc(Vec3 center, double radius, int color, int segments) {
            ring(center, 0.0, radius, color, segments);
        }

        /** Вертикальная плоскость — для кейпов и плащей. */
        public void plane(Vec3 corner, Vec3 right, Vec3 down, int color) {
            Vec3 a = corner;
            Vec3 b = corner.add(right);
            Vec3 c = corner.add(right).add(down);
            Vec3 d = corner.add(down);
            quad(a, b, c, d, color);
        }

        public void gradientPlane(Vec3 corner, Vec3 right, Vec3 down, int colorTop, int colorBottom, int steps) {
            Vec3 verticalStep = down.scale(1.0 / steps);
            for (int i = 0; i < steps; i++) {
                float progress = i / (float) steps;
                int color = ColorUtil.lerp(colorTop, colorBottom, progress);
                Vec3 top = corner.add(verticalStep.scale(i));
                plane(top, right, verticalStep, color);
            }
        }
    }
}
