package su.aurora.modules.render;

import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
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
import java.util.Set;

/**
 * Trajectory — предсказание полёта снаряда из рук: луки, жемчуг Края, снежки, зелья.
 */
public class TrajectoryModule extends Module {
    private static final Set<Item> PROJECTILES = Set.of(
            Items.BOW, Items.CROSSBOW, Items.ENDER_PEARL, Items.SNOWBALL, Items.EGG,
            Items.SPLASH_POTION, Items.LINGERING_POTION, Items.EXPERIENCE_BOTTLE,
            Items.TRIDENT, Items.FISHING_ROD);

    private final ColorSetting color = addColor("Цвет", 0xFF7C5CFF);
    private final NumberSetting speed = addNumber("Начальная скорость", 1.5, 0.3, 4.0, 0.1);
    private final NumberSetting gravity = addNumber("Гравитация", 0.05, 0.0, 0.2, 0.005);
    private final NumberSetting drag = addNumber("Сопротивление", 0.99, 0.9, 1.0, 0.005);
    private final NumberSetting steps = addNumber("Длина расчёта", 120, 20, 400, 10);
    private final BooleanSetting onlyWhenAiming = addBoolean("Только при прицеливании", true);
    private final BooleanSetting marker = addBoolean("Метка попадания", true);

    public TrajectoryModule() {
        super("Trajectory", "Предсказание полёта снаряда", Category.RENDER);
    }

    private boolean holdingProjectile(Minecraft client) {
        ItemStack main = client.player.getMainHandItem();
        ItemStack off = client.player.getOffhandItem();
        return PROJECTILES.contains(main.getItem()) || PROJECTILES.contains(off.getItem());
    }

    @Override
    public void onWorldRender(WorldRenderContext context) {
        Minecraft client = Minecraft.getInstance();
        if (client.level == null || client.player == null) {
            return;
        }
        if (!holdingProjectile(client)) {
            return;
        }
        if (onlyWhenAiming.get() && !client.player.isUsingItem() && !client.options.keyUse.isDown()) {
            return;
        }

        Vec3 position = client.player.getEyePosition();
        Vec3 velocity = client.player.getLookAngle().scale(speed.get());
        List<Vec3> points = new ArrayList<>();
        points.add(position);
        Vec3 impact = null;

        int maxSteps = steps.getInt();
        for (int i = 0; i < maxSteps; i++) {
            Vec3 next = position.add(velocity);
            BlockHitResult hit = client.level.clip(new ClipContext(position, next, ClipContext.Block.COLLIDER,
                    ClipContext.Fluid.NONE, client.player));
            if (hit.getType() == HitResult.Type.BLOCK) {
                impact = hit.getLocation();
                points.add(impact);
                break;
            }
            points.add(next);
            position = next;
            velocity = velocity.scale(drag.get()).add(0, -gravity.get(), 0);
            if (position.y < client.level.getMinY() - 8) {
                break;
            }
        }

        int tint = color.get();
        Render3D.lines(buffer -> {
            for (int i = 1; i < points.size(); i++) {
                float fade = 1f - (i / (float) Math.max(1, points.size()));
                buffer.line(points.get(i - 1), points.get(i), ColorUtil.withAlpha(tint, (int) (255 * (0.35f + fade))),
                        2.0f);
            }
        });
        if (marker.get() && impact != null) {
            Vec3 hitPoint = impact;
            Render3D.lines(buffer -> buffer.cross(hitPoint, 0.25, tint, 2.0f));
            Render3D.quads(buffer -> buffer.disc(hitPoint, 0.2, ColorUtil.withAlpha(tint, 90), 14));
        }
    }
}
