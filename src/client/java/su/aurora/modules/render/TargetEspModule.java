package su.aurora.modules.render;

import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.minecraft.world.entity.LivingEntity;
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
import su.aurora.util.PlayerUtil;
import su.aurora.util.Render3D;

/**
 * TargetESP — подсветка цели. Режимы: круг, рамка, призрак, ромб, кристаллы, кольцо, пирамиды.
 */
public class TargetEspModule extends Module {
    private final ModeSetting mode = addMode("Режим", "Круг",
            "Круг", "Рамка", "Призрак", "Ромб", "Кристаллы", "Кольцо", "Пирамиды");
    private final NumberSetting size = addNumber("Размер", 1.0, 0.4, 3.0, 0.05);
    private final NumberSetting alpha = addInt("Прозрачность", 210, 20, 255);
    private final BooleanSetting ripple = addBoolean("Рябь", true);
    private final BooleanSetting glow = addBoolean("Заливка", true);
    private final BooleanSetting onlyPlayers = addBoolean("Только игроки", true);
    private final NumberSetting range = addNumber("Дистанция", 16, 3, 64, 1);
    private final ColorSetting color = addColor("Цвет", 0xFF5CE1E6);

    public TargetEspModule() {
        super("TargetESP", "Подсвечивает цель кругом, рамкой или фигурой", Category.RENDER);
        setEnabled(true);
    }

    private LivingEntity findTarget() {
        LivingEntity target = PlayerUtil.crosshairTarget(range.get());
        if (target == null) {
            target = PlayerUtil.nearestPlayer(range.get());
        }
        if (target != null && onlyPlayers.get() && !(target instanceof Player)) {
            return null;
        }
        return target;
    }

    @Override
    public void onWorldRender(WorldRenderContext context) {
        LivingEntity target = findTarget();
        if (target == null) {
            return;
        }
        float partial = Render3D.tickDelta();
        double x = target.getX(partial);
        double y = target.getY(partial);
        double z = target.getZ(partial);
        Vec3 center = new Vec3(x, y, z);
        int baseColor = ColorUtil.withAlpha(color.get(), alpha.getInt());
        float pulse = (float) ((Math.sin(System.currentTimeMillis() / 260.0) + 1.0) * 0.5);
        double scale = size.get();

        switch (mode.get()) {
            case "Круг" -> Render3D.lines(buffer -> {
                buffer.circle(center.add(0, 0.05, 0), 0.75 * scale, baseColor, 2.5f, 72);
                if (ripple.get()) {
                    double wave = 0.75 * scale + pulse * 0.5 * scale;
                    buffer.circle(center.add(0, 0.05, 0), wave,
                            ColorUtil.scaleAlpha(baseColor, 1f - pulse), 2f, 72);
                }
            });
            case "Рамка" -> {
                AABB box = target.getBoundingBox().inflate(0.05 * scale);
                Render3D.lines(buffer -> buffer.box(box, baseColor, 2.5f));
                if (glow.get()) {
                    Render3D.quads(buffer -> buffer.box(box, ColorUtil.scaleAlpha(baseColor, 0.22f)));
                }
            }
            case "Призрак" -> {
                AABB box = target.getBoundingBox().inflate(0.08 * scale);
                Render3D.lines(buffer -> {
                    buffer.box(box, baseColor, 2f);
                    buffer.box(box.inflate(0.12 + pulse * 0.1), ColorUtil.scaleAlpha(baseColor, 0.6f), 1.5f);
                });
                Render3D.quads(buffer -> buffer.box(box, ColorUtil.scaleAlpha(baseColor, 0.16f + pulse * 0.08f)));
            }
            case "Ромб" -> {
                Vec3 middle = center.add(0, target.getBbHeight() / 2.0, 0);
                Render3D.lines(buffer -> buffer.diamond(middle, 1.1 * scale, baseColor, 2.5f));
            }
            case "Кристаллы" -> Render3D.lines(buffer -> {
                double height = target.getBbHeight();
                for (int i = 0; i < 3; i++) {
                    double angle = (System.currentTimeMillis() / 700.0) + (Math.PI * 2 * i) / 3.0;
                    Vec3 point = center.add(Math.cos(angle) * 0.8 * scale, height * (0.35 + i * 0.25),
                            Math.sin(angle) * 0.8 * scale);
                    buffer.crystal(point, 0.22 * scale, baseColor, 1.8f);
                }
                buffer.circle(center.add(0, 0.05, 0), 0.8 * scale, baseColor, 2f, 60);
            });
            case "Кольцо" -> {
                double height = target.getBbHeight();
                Render3D.lines(buffer -> {
                    for (int i = 0; i < 3; i++) {
                        double offset = height * (0.25 + 0.25 * i) + Math.sin(System.currentTimeMillis() / 500.0 + i) * 0.1;
                        buffer.orbit(center.add(0, offset, 0), 0.85 * scale,
                                ColorUtil.scaleAlpha(baseColor, 0.9f - i * 0.2f), 2f, 48,
                                0.35 + i * 0.25, System.currentTimeMillis() / 900.0 + i);
                    }
                });
            }
            case "Пирамиды" -> {
                Vec3 middle = center.add(0, target.getBbHeight() / 2.0, 0);
                Render3D.lines(buffer -> {
                    buffer.pyramid(middle, 0.9 * scale, 1.6 * scale, baseColor, 2.2f, false);
                    double phase = (System.currentTimeMillis() % 2000L) / 2000.0;
                    buffer.pyramid(middle.add(0, 1.6 * scale + phase * 0.8, 0), 0.5 * scale, 0.9 * scale,
                            ColorUtil.scaleAlpha(baseColor, 0.7f), 1.8f, true);
                });
            }
            default -> {
            }
        }
    }
}
