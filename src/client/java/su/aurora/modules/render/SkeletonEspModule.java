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
 * SkeletonESP — скелет-стикмен внутри игрока: голова, позвоночник, руки и ноги.
 * Пропорции фиксированные, анимация шага считается по скорости движения.
 */
public class SkeletonEspModule extends Module {
    private final ModeSetting colorMode = addMode("Цвет", "Акцент", "Акцент", "По здоровью", "Радуга");
    private final ColorSetting color = addColor("Основной цвет", 0xFF7C5CFF);
    private final NumberSetting range = addNumber("Дистанция", 32, 4, 96, 1);
    private final NumberSetting thickness = addNumber("Толщина", 2.0, 1.0, 4.0, 0.1);
    private final BooleanSetting onlyPlayers = addBoolean("Только игроки", true);

    public SkeletonEspModule() {
        super("SkeletonESP", "Скелет игрока внутри модели", Category.RENDER);
    }

    @Override
    public void onWorldRender(WorldRenderContext context) {
        Minecraft client = Minecraft.getInstance();
        if (client.level == null || client.player == null) {
            return;
        }
        float partial = Render3D.tickDelta();
        double rangeSq = range.get() * range.get();

        for (Entity candidate : client.level.entitiesForRendering()) {
            if (!(candidate instanceof LivingEntity entity)) {
                continue;
            }
            if (entity == client.player || !entity.isAlive() || entity.isInvisible()) {
                continue;
            }
            if (onlyPlayers.get() && !(entity instanceof Player)) {
                continue;
            }
            if (entity.distanceToSqr(client.player) > rangeSq) {
                continue;
            }
            renderSkeleton(entity, partial);
        }
    }

    private void renderSkeleton(LivingEntity entity, float partial) {
        double x = entity.getX(partial);
        double y = entity.getY(partial);
        double z = entity.getZ(partial);
        float yaw = (float) Math.toRadians(entity.getYRot());
        Vec3 forward = new Vec3(-Math.sin(yaw), 0, Math.cos(yaw));
        Vec3 right = new Vec3(Math.cos(yaw), 0, Math.sin(yaw));

        double scale = Math.max(0.6, entity.getBbHeight() / 1.8);
        double hipY = y + 0.72 * scale;
        double chestY = y + 1.12 * scale;
        double neckY = y + 1.34 * scale;
        double headY = y + 1.55 * scale;

        double speed = Math.min(1.0, entity.getDeltaMovement().horizontalDistance() * 6.0);
        double swing = Math.sin(System.currentTimeMillis() / 110.0) * 0.22 * speed;
        float width = thickness.getFloat();

        int baseColor = switch (colorMode.get()) {
            case "По здоровью" -> ColorUtil.lerp(0xFFFF4D4D, 0xFF7CF7B0,
                    entity.getMaxHealth() <= 0 ? 0 : entity.getHealth() / entity.getMaxHealth());
            case "Радуга" -> ColorUtil.rainbow(5000L, 0.65f, 1f, 235);
            default -> color.get();
        };

        Vec3 root = new Vec3(x, hipY, z);
        Vec3 chest = new Vec3(x, chestY, z);
        Vec3 neck = new Vec3(x, neckY, z);
        Vec3 head = new Vec3(x, headY, z);

        Vec3 leftShoulder = chest.add(right.scale(-0.24 * scale));
        Vec3 rightShoulder = chest.add(right.scale(0.24 * scale));
        Vec3 leftElbow = leftShoulder.add(0, -0.24 * scale, 0).add(forward.scale(swing));
        Vec3 rightElbow = rightShoulder.add(0, -0.24 * scale, 0).subtract(forward.scale(swing));
        Vec3 leftHand = leftElbow.add(0, -0.22 * scale, 0).add(forward.scale(swing * 0.6));
        Vec3 rightHand = rightElbow.add(0, -0.22 * scale, 0).subtract(forward.scale(swing * 0.6));

        Vec3 leftHip = root.add(right.scale(-0.12 * scale));
        Vec3 rightHip = root.add(right.scale(0.12 * scale));
        Vec3 leftKnee = leftHip.add(forward.scale(swing)).add(0, -0.36 * scale, 0);
        Vec3 rightKnee = rightHip.subtract(forward.scale(swing)).add(0, -0.36 * scale, 0);
        Vec3 leftFoot = leftKnee.add(0, -0.36 * scale, 0).add(forward.scale(swing * 0.3));
        Vec3 rightFoot = rightKnee.add(0, -0.36 * scale, 0).subtract(forward.scale(swing * 0.3));

        Render3D.lines(buffer -> {
            buffer.line(root, chest, baseColor, width);
            buffer.line(chest, neck, baseColor, width);
            buffer.line(neck, head, baseColor, width);
            buffer.circle(head, 0.11 * scale, baseColor, width, 16);

            buffer.line(leftShoulder, leftElbow, baseColor, width);
            buffer.line(leftElbow, leftHand, ColorUtil.scaleAlpha(baseColor, 0.85f), width);
            buffer.line(rightShoulder, rightElbow, baseColor, width);
            buffer.line(rightElbow, rightHand, ColorUtil.scaleAlpha(baseColor, 0.85f), width);
            buffer.line(leftShoulder, rightShoulder, ColorUtil.scaleAlpha(baseColor, 0.7f), width);

            buffer.line(leftHip, leftKnee, baseColor, width);
            buffer.line(leftKnee, leftFoot, ColorUtil.scaleAlpha(baseColor, 0.85f), width);
            buffer.line(rightHip, rightKnee, baseColor, width);
            buffer.line(rightKnee, rightFoot, ColorUtil.scaleAlpha(baseColor, 0.85f), width);
            buffer.line(leftHip, rightHip, ColorUtil.scaleAlpha(baseColor, 0.7f), width);
        });
    }
}
