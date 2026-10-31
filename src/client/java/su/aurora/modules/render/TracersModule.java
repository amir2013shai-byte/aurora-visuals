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
import su.aurora.setting.ModeSetting;
import su.aurora.setting.NumberSetting;
import su.aurora.util.ColorUtil;
import su.aurora.util.Render3D;

/**
 * Tracers — линии от прицела к игрокам и/или мобам.
 */
public class TracersModule extends Module {
    private final ModeSetting targets = addMode("Цели", "Игроки", "Игроки", "Мобы", "Все");
    private final ModeSetting colorMode = addMode("Цвет", "По дистанции", "По дистанции", "Радуга", "Обычный");
    private final ColorSetting color = addColor("Основной цвет", 0xFF7C5CFF);
    private final ColorSetting farColor = addColor("Дальний цвет", 0xFFFF5C7C);
    private final NumberSetting range = addNumber("Дистанция", 64, 8, 256, 1);
    private final NumberSetting thickness = addNumber("Толщина", 2.0, 1.0, 5.0, 0.1);
    private final BooleanSetting startFromCrosshair = addBoolean("Начало по прицелу", true);

    public TracersModule() {
        super("Tracers", "Линии от прицела к цели", Category.RENDER);
    }

    @Override
    public void onWorldRender(WorldRenderContext context) {
        Minecraft client = Minecraft.getInstance();
        if (client.level == null || client.player == null) {
            return;
        }
        Vec3 camera = Render3D.camera();
        Vec3 start = camera;
        if (startFromCrosshair.get()) {
            Vec3 look = client.player.getLookAngle().scale(0.6);
            start = camera.add(look).subtract(0, 0.15, 0);
        }

        float partial = Render3D.tickDelta();
        double maxDistance = range.get();
        Vec3 finalStart = start;

        for (Entity entity : client.level.entitiesForRendering()) {
            if (!(entity instanceof LivingEntity living) || living == client.player || !living.isAlive()) {
                continue;
            }
            boolean isPlayer = living instanceof Player;
            if (living.isInvisible()) {
                continue;
            }
            switch (targets.get()) {
                case "Игроки" -> {
                    if (!isPlayer) {
                        continue;
                    }
                }
                case "Мобы" -> {
                    if (isPlayer) {
                        continue;
                    }
                }
                default -> {
                }
            }

            double distance = living.distanceTo(client.player);
            if (distance > maxDistance) {
                continue;
            }

            Vec3 end = new Vec3(living.getX(partial), living.getY(partial) + living.getBbHeight() * 0.5,
                    living.getZ(partial));
            float progress = (float) Math.min(1.0, distance / maxDistance);
            int lineColor = switch (colorMode.get()) {
                case "Радуга" -> ColorUtil.rainbow(5000L, 0.6f, 1f, 230);
                case "Обычный" -> color.get();
                default -> ColorUtil.lerp(color.get(), farColor.get(), progress);
            };

            Render3D.lines(buffer -> buffer.line(finalStart, end, lineColor, thickness.getFloat()));
        }
    }
}
