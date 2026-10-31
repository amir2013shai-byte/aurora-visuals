package su.aurora.modules.render;

import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import su.aurora.core.Category;
import su.aurora.core.Module;
import su.aurora.setting.BooleanSetting;
import su.aurora.setting.ColorSetting;
import su.aurora.setting.NumberSetting;
import su.aurora.util.ColorUtil;
import su.aurora.util.Render3D;

import java.util.ArrayList;
import java.util.List;

/**
 * CrystalColor — свой цвет кристаллов Края.
 * Если включено «Скрыть ванильный» — миксин гасит стандартный рендер,
 * и кристалл рисуется нашим цветом (октаэдр + пульсация + лучи между парами).
 */
public class CrystalColorModule extends Module {
    private final ColorSetting color = addColor("Цвет", 0xFF7C5CFF);
    private final NumberSetting size = addNumber("Размер", 1.0, 0.3, 2.5, 0.05);
    private final BooleanSetting hideVanilla = addBoolean("Скрыть ванильный", true);
    private final BooleanSetting pulse = addBoolean("Пульсация", true);
    private final BooleanSetting beams = addBoolean("Лучи между кристаллами", true);
    private final NumberSetting range = addNumber("Дистанция", 96, 8, 256, 8);

    public CrystalColorModule() {
        super("CrystalColor", "Свой цвет и форма кристаллов Края", Category.RENDER);
    }

    public boolean hideVanillaCrystal() {
        return isEnabled() && hideVanilla.get();
    }

    @Override
    public void onWorldRender(WorldRenderContext context) {
        Minecraft client = Minecraft.getInstance();
        if (client.level == null || client.player == null) {
            return;
        }
        float partial = Render3D.tickDelta();
        double rangeSq = range.get() * range.get();
        float time = System.currentTimeMillis() / 1000f;
        float pulsation = pulse.get() ? (float) (0.85 + 0.15 * Math.sin(time * 3.0)) : 1f;
        int tint = color.get();
        int alpha = (int) (235 * pulsation);
        double s = size.get();
        List<Vec3> centers = new ArrayList<>();

        for (Entity entity : client.level.entitiesForRendering()) {
            if (!(entity instanceof EndCrystal crystal)) {
                continue;
            }
            if (crystal.distanceToSqr(client.player) > rangeSq) {
                continue;
            }
            Vec3 center = new Vec3(crystal.getX(), crystal.getY(partial) + 0.6, crystal.getZ());
            centers.add(center);
            Render3D.lines(buffer -> {
                buffer.crystal(center, s, ColorUtil.withAlpha(tint, alpha), 2.2f);
                buffer.circle(center.add(0, -0.55 * s, 0), 0.55 * s, ColorUtil.withAlpha(tint, 140), 1.6f, 24);
            });
            Render3D.quads(buffer -> {
                AABB core = new AABB(center.x - 0.22 * s, center.y - 0.22 * s, center.z - 0.22 * s,
                        center.x + 0.22 * s, center.y + 0.22 * s, center.z + 0.22 * s);
                buffer.box(core, ColorUtil.withAlpha(tint, (int) (alpha * 0.55f)));
                buffer.disc(center.add(0, -0.55 * s, 0), 0.55 * s, ColorUtil.withAlpha(tint, 40), 20);
            });
        }

        if (beams.get() && centers.size() > 1) {
            Render3D.lines(buffer -> {
                for (int i = 0; i < centers.size(); i++) {
                    for (int j = i + 1; j < centers.size(); j++) {
                        Vec3 a = centers.get(i);
                        Vec3 b = centers.get(j);
                        if (a.distanceTo(b) > 16) {
                            continue;
                        }
                        buffer.line(a, b, ColorUtil.withAlpha(tint, 90), 1.4f);
                    }
                }
            });
        }
    }
}
