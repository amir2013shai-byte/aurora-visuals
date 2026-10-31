package su.aurora.modules.render;

import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import su.aurora.core.Category;
import su.aurora.core.Module;
import su.aurora.setting.BooleanSetting;
import su.aurora.setting.ColorSetting;
import su.aurora.setting.NumberSetting;
import su.aurora.util.ColorUtil;
import su.aurora.util.Render3D;

/**
 * Wings — косметические крылья за спиной, машут в такт движению.
 */
public class WingsModule extends Module {
    private final ColorSetting color = addColor("Цвет", 0xFFFFFFFF);
    private final ColorSetting tip = addColor("Цвет кончиков", 0xFF7C5CFF);
    private final NumberSetting size = addNumber("Размер", 1.0, 0.5, 2.0, 0.05);
    private final NumberSetting speed = addNumber("Скорость взмаха", 1.0, 0.2, 3.0, 0.1);
    private final BooleanSetting allPlayers = addBoolean("Все игроки", false);

    public WingsModule() {
        super("Wings", "Косметические крылья с анимацией", Category.RENDER);
    }

    @Override
    public void onWorldRender(WorldRenderContext context) {
        Minecraft client = Minecraft.getInstance();
        if (client.level == null || client.player == null) {
            return;
        }
        float partial = Render3D.tickDelta();
        if (allPlayers.get()) {
            for (Player player : client.level.players()) {
                renderWings(player, partial);
            }
        } else {
            renderWings(client.player, partial);
        }
    }

    private void renderWings(Player player, float partial) {
        double x = player.getX(partial);
        double y = player.getY(partial);
        double z = player.getZ(partial);
        float yaw = (float) Math.toRadians(player.getYRot());
        Vec3 forward = new Vec3(-Math.sin(yaw), 0, Math.cos(yaw));
        Vec3 right = new Vec3(Math.cos(yaw), 0, Math.sin(yaw));
        Vec3 up = new Vec3(0, 1, 0);

        double flap = Math.sin(System.currentTimeMillis() / 320.0 * speed.get()) * 0.35 + 0.35;
        Vec3 anchor = new Vec3(x, y + 1.25, z).subtract(forward.scale(0.16));
        double span = 0.85 * size.get();
        double height = 0.95 * size.get();

        int base = color.get();
        int tips = tip.get();

        Render3D.quads(buffer -> {
            for (int side = -1; side <= 1; side += 2) {
                Vec3 root = anchor;
                Vec3 shoulder = anchor.add(right.scale(side * span * 0.35)).add(up.scale(0.25 * flap));
                Vec3 tipPoint = anchor.add(right.scale(side * span))
                        .add(up.scale(height * (0.5 + flap * 0.4)))
                        .subtract(forward.scale(0.05));
                Vec3 lower = anchor.add(right.scale(side * span * 0.65)).add(0, -height * 0.55, 0)
                        .subtract(forward.scale(0.05));

                buffer.quad(root, shoulder, tipPoint, lower, ColorUtil.withAlpha(base, 150));

                Vec3 innerFeather = shoulder.add(0, -height * 0.22, 0);
                Vec3 midFeather = anchor.add(right.scale(side * span * 0.62)).add(0, height * 0.12, 0);
                Vec3 outerFeather = tipPoint.add(0, -height * 0.18, 0);
                Vec3 lowFeather = lower.add(right.scale(side * span * 0.12));
                buffer.quad(innerFeather, midFeather, outerFeather, lowFeather, ColorUtil.withAlpha(tips, 120));
            }
        });
    }
}