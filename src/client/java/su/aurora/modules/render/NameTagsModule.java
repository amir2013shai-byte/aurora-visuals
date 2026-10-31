package su.aurora.modules.render;

import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import com.mojang.blaze3d.vertex.PoseStack;
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
 * NameTags — ники и полоса здоровья прямо в мире, над головой.
 * Текст отправляется через submitText, полоса рисуется квадами.
 */
public class NameTagsModule extends Module {
    private final ModeSetting targets = addMode("Цели", "Игроки", "Игроки", "Все живые");
    private final NumberSetting range = addNumber("Дистанция", 32, 4, 128, 1);
    private final BooleanSetting showHealth = addBoolean("Здоровье", true);
    private final BooleanSetting healthBar = addBoolean("Полоса здоровья", true);
    private final BooleanSetting showDistance = addBoolean("Дистанция до цели", false);
    private final NumberSetting textScale = addNumber("Размер текста", 1.0, 0.5, 2.0, 0.05);
    private final ColorSetting nameColor = addColor("Цвет имени", 0xFFFFFFFF);

    public NameTagsModule() {
        super("NameTags", "Ники и HP-бар над головой в мире", Category.RENDER);
        setEnabled(true);
    }

    @Override
    public void onWorldRender(WorldRenderContext context) {
        Minecraft client = Minecraft.getInstance();
        if (client.level == null || client.player == null) {
            return;
        }
        float partial = Render3D.tickDelta();
        double rangeSq = range.get() * range.get();
        Vec3 camera = Render3D.camera();
        Font font = client.font;

        for (Entity candidate : client.level.entitiesForRendering()) {
            if (!(candidate instanceof LivingEntity entity) || entity == client.player || !entity.isAlive()) {
                continue;
            }
            if (targets.is("Игроки") && !(entity instanceof Player)) {
                continue;
            }
            if (entity.distanceToSqr(client.player) > rangeSq || entity.isInvisible()) {
                continue;
            }

            double x = entity.getX(partial);
            double y = entity.getY(partial) + entity.getBbHeight() + 0.55;
            double z = entity.getZ(partial);

            String text = entity.getName().getString();
            if (showHealth.get()) {
                text += String.format(java.util.Locale.ROOT, "  %.1f", PlayerUtil.health(entity));
            }
            if (showDistance.get()) {
                text += String.format(java.util.Locale.ROOT, "  %.0f m", entity.distanceTo(client.player));
            }

            try {
                PoseStack matrices = context.matrices();
                matrices.pushPose();
                matrices.translate(x - camera.x, y - camera.y, z - camera.z);
                matrices.mulPose(client.gameRenderer.getMainCamera().rotation());
                float scale = -0.025f * textScale.getFloat();
                matrices.scale(scale, scale, scale);

                int width = font.width(text);
                context.commandQueue().submitText(matrices, -width / 2f, 0f,
                        Component.literal(text).getVisualOrderText(), true,
                        Font.DisplayMode.SEE_THROUGH, 0xF000F0, nameColor.get(), 0, 0);
                matrices.popPose();
            } catch (Throwable throwable) {
                su.aurora.Aurora.LOG.warn("[NameTags] не удалось нарисовать текст: {}", throwable.getMessage());
                return;
            }

            if (healthBar.get()) {
                float percent = PlayerUtil.healthPercent(entity);
                double barWidth = 0.9;
                double yBar = y + 0.06;
                Vec3 left = new Vec3(x - barWidth / 2, yBar, z);
                int barColor = ColorUtil.lerp(0xFFFF4D4D, 0xFF7CF7B0, percent);
                Render3D.quads(buffer -> {
                    buffer.quad(left, left.add(barWidth, 0, 0), left.add(barWidth, 0.09, 0), left.add(0, 0.09, 0),
                            0xB0101218);
                    buffer.quad(left, left.add(barWidth * percent, 0, 0), left.add(barWidth * percent, 0.09, 0),
                            left.add(0, 0.09, 0), barColor);
                });
            }
        }
    }
}
