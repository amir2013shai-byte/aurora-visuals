package su.aurora.modules.render;

import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import su.aurora.core.Category;
import su.aurora.core.Module;
import su.aurora.setting.BooleanSetting;
import su.aurora.setting.ColorSetting;
import su.aurora.setting.ModeSetting;
import su.aurora.setting.NumberSetting;
import su.aurora.util.ColorUtil;
import su.aurora.util.Render3D;

/**
 * HitColor — свой цвет вспышки при попадании.
 * Основной путь — миксин в LivingEntityRenderer#getModelTint (тинт модели),
 * страховка — полупрозрачная рамка вокруг получившей урон сущности.
 */
public class HitColorModule extends Module {
    private final ModeSetting mode = addMode("Вид", "Модель + рамка", "Модель", "Рамка", "Модель + рамка");
    private final ColorSetting color = addColor("Цвет вспышки", 0xFF7C5CFF);
    private final NumberSetting range = addNumber("Дистанция", 16, 2, 64, 1);
    private final BooleanSetting onlyPlayers = addBoolean("Только игроки", false);

    public HitColorModule() {
        super("HitColor", "Свой цвет вспышки при попадании по цели", Category.RENDER);
    }

    public boolean tintModel() {
        return isEnabled() && (mode.is("Модель") || mode.is("Модель + рамка"));
    }

    public boolean outline() {
        return isEnabled() && (mode.is("Рамка") || mode.is("Модель + рамка"));
    }

    /** Цвет в формате ARGB для тинта модели. */
    public int tintColor() {
        return ColorUtil.withAlpha(color.get(), 235);
    }

    @Override
    public void onWorldRender(WorldRenderContext context) {
        if (!outline()) {
            return;
        }
        Minecraft client = Minecraft.getInstance();
        if (client.level == null || client.player == null) {
            return;
        }
        float partial = Render3D.tickDelta();
        double rangeSq = range.get() * range.get();

        for (Entity candidate : client.level.entitiesForRendering()) {
            if (!(candidate instanceof LivingEntity entity) || entity == client.player) {
                continue;
            }
            if (entity.hurtTime <= 0) {
                continue;
            }
            if (onlyPlayers.get() && !(entity instanceof Player)) {
                continue;
            }
            if (entity.distanceToSqr(client.player) > rangeSq) {
                continue;
            }
            float progress = entity.hurtTime / (float) Math.max(1, entity.hurtDuration);
            int lineColor = ColorUtil.withAlpha(color.get(), (int) (60 + 190 * progress));
            AABB box = entity.getBoundingBox().move(-entity.getX() + entity.getX(partial),
                    -entity.getY() + entity.getY(partial), -entity.getZ() + entity.getZ(partial)).inflate(0.05);
            Render3D.lines(buffer -> buffer.box(box, lineColor, 2.4f));
            Render3D.quads(buffer -> buffer.box(box, ColorUtil.withAlpha(color.get(), (int) (18 + 40 * progress))));
        }
    }
}
