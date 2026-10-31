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
 * Cape — собственный плащ. Рисуется в мире как тканевая плоскость с волной.
 */
public class CapeModule extends Module {
    private final ColorSetting color = addColor("Цвет", 0xFF7C5CFF);
    private final ColorSetting second = addColor("Цвет низа", 0xFF00E5FF);
    private final NumberSetting size = addNumber("Размер", 1.0, 0.6, 1.6, 0.05);
    private final NumberSetting wave = addNumber("Волна", 1.0, 0.0, 3.0, 0.1);
    private final BooleanSetting allPlayers = addBoolean("Все игроки", false);

    public CapeModule() {
        super("Capes", "Собственный плащ для тебя и других игроков", Category.RENDER);
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
                renderCape(player, partial);
            }
        } else {
            renderCape(client.player, partial);
        }
    }

    private void renderCape(Player player, float partial) {
        double x = player.getX(partial);
        double y = player.getY(partial);
        double z = player.getZ(partial);
        float yaw = (float) Math.toRadians(player.getYRot());
        Vec3 forward = new Vec3(-Math.sin(yaw), 0, Math.cos(yaw));
        Vec3 right = new Vec3(Math.cos(yaw), 0, Math.sin(yaw));

        double width = 0.42 * size.get();
        double height = 0.78 * size.get();
        double sway = Math.sin(System.currentTimeMillis() / 260.0) * 0.045 * wave.get();
        double lean = player.getDeltaMovement().horizontalDistance() * 0.35 * wave.get();

        Vec3 anchor = new Vec3(x, y + 1.42, z)
                .subtract(forward.scale(0.14))
                .subtract(forward.scale(lean));

        Vec3 topLeft = anchor.add(right.scale(-width));
        Vec3 topRight = anchor.add(right.scale(width));

        int top = color.get();
        int bottom = second.get();

        Render3D.quads(buffer -> {
            int rows = 6;
            for (int row = 0; row < rows; row++) {
                double t0 = row / (double) rows;
                double t1 = (row + 1) / (double) rows;
                Vec3 offset0 = forward.scale(sway * t0).add(0, -height * t0, 0);
                Vec3 offset1 = forward.scale(sway * t1).add(0, -height * t1, 0);
                Vec3 a = topLeft.add(offset0);
                Vec3 b = topRight.add(offset0);
                Vec3 c = topRight.add(offset1);
                Vec3 d = topLeft.add(offset1);
                int rowColor = ColorUtil.lerp(top, bottom, (float) t0);
                buffer.quad(a, b, c, d, ColorUtil.withAlpha(rowColor, 205));
            }
        });
    }
}
