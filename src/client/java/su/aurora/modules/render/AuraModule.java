package su.aurora.modules.render;

import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import su.aurora.core.Category;
import su.aurora.core.Module;
import su.aurora.setting.BooleanSetting;
import su.aurora.setting.ColorSetting;
import su.aurora.setting.ModeSetting;
import su.aurora.setting.NumberSetting;
import su.aurora.util.ColorUtil;
import su.aurora.util.Render3D;

/**
 * Aura — чисто визуальное кольцо/сфера вокруг игрока с орбитальными частицами.
 */
public class AuraModule extends Module {
    private final ModeSetting mode = addMode("Форма", "Кольцо", "Кольцо", "Сфера", "Спираль");
    private final NumberSetting radius = addNumber("Радиус", 0.9, 0.4, 3.0, 0.05);
    private final NumberSetting segments = addInt("Сегменты", 64, 16, 128);
    private final ColorSetting color = addColor("Цвет", 0xFF7C5CFF);
    private final BooleanSetting particles = addBoolean("Частицы", true);
    private final BooleanSetting rotate = addBoolean("Вращение", true);

    public AuraModule() {
        super("Aura", "Визуальная аура вокруг игрока", Category.RENDER);
    }

    @Override
    public void onWorldRender(WorldRenderContext context) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null) {
            return;
        }
        float partial = Render3D.tickDelta();
        double x = client.player.getX(partial);
        double y = client.player.getY(partial);
        double z = client.player.getZ(partial);
        Vec3 center = new Vec3(x, y + 0.05, z);
        double r = radius.get();
        double time = System.currentTimeMillis() / 1000.0;
        double phase = rotate.get() ? time * 1.6 : 0;

        switch (mode.get()) {
            case "Кольцо" -> Render3D.lines(buffer -> {
                buffer.circle(center, r, color.get(), 2.5f, segments.getInt());
                buffer.circle(center.add(0, 0.12, 0), r * 0.82, ColorUtil.scaleAlpha(color.get(), 0.55f), 2f,
                        segments.getInt());
            });
            case "Сфера" -> Render3D.lines(buffer -> {
                for (int i = 0; i < 5; i++) {
                    double tilt = Math.toRadians(36 * i + phase * 22);
                    buffer.orbit(center.add(0, r * 0.9, 0), r, ColorUtil.scaleAlpha(color.get(), 0.85f), 2f,
                            segments.getInt(), Math.sin(tilt), phase + i);
                }
            });
            case "Спираль" -> Render3D.lines(buffer -> {
                double height = 1.9;
                Vec3 previous = null;
                for (int i = 0; i <= 64; i++) {
                    double t = i / 64.0;
                    double angle = t * Math.PI * 4 + phase;
                    Vec3 point = center.add(Math.cos(angle) * r, t * height, Math.sin(angle) * r);
                    if (previous != null) {
                        buffer.line(previous, point, ColorUtil.scaleAlpha(color.get(), (float) (0.35 + t * 0.65)), 2f);
                    }
                    previous = point;
                }
            });
            default -> {
            }
        }

        if (particles.get() && client.level != null && Math.random() < 0.35) {
            double angle = Math.random() * Math.PI * 2 + phase;
            double px = x + Math.cos(angle) * r;
            double pz = z + Math.sin(angle) * r;
            client.level.addParticle(ParticleTypes.END_ROD, px, y + 0.1 + Math.random() * 1.5, pz,
                    -Math.cos(angle) * 0.02, 0.012, -Math.sin(angle) * 0.02);
        }
    }
}
