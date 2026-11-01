package su.aurora.modules.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import su.aurora.core.Category;
import su.aurora.core.Module;
import su.aurora.setting.BooleanSetting;
import su.aurora.setting.ColorSetting;
import su.aurora.setting.NumberSetting;
import su.aurora.util.ColorUtil;
import su.aurora.util.Render3D;

/**
 * ItemESP — подсветка выпавших предметов с подписями и цветом по редкости.
 */
public class ItemEspModule extends Module {
    private final NumberSetting range = addNumber("Дистанция", 48, 4, 128, 4);
    private final BooleanSetting labels = addBoolean("Подписи", true);
    private final BooleanSetting distance = addBoolean("Дистанция в подписи", true);
    private final BooleanSetting byRarity = addBoolean("Цвет по редкости", true);
    private final BooleanSetting fill = addBoolean("Заливка", true);
    private final ColorSetting color = addColor("Обычный цвет", 0xFFFFD166);

    public ItemEspModule() {
        super("ItemESP", "Подсветка предметов на земле с подписями", Category.RENDER);
    }

    private int colorFor(ItemEntity entity) {
        if (!byRarity.get()) {
            return color.get();
        }
        Rarity rarity = entity.getItem().getRarity();
        return switch (rarity) {
            case UNCOMMON -> 0xFFFFFF55;
            case RARE -> 0xFF55AAFF;
            case EPIC -> 0xFFCC55FF;
            default -> color.get();
        };
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

        for (Entity entity : client.level.entitiesForRendering()) {
            if (!(entity instanceof ItemEntity item)) {
                continue;
            }
            if (item.getItem().isEmpty() || item.distanceToSqr(client.player) > rangeSq) {
                continue;
            }
            Vec3 position = new Vec3(item.getX(partial), item.getY(partial), item.getZ(partial));
            AABB box = new AABB(position.x - 0.18, position.y, position.z - 0.18,
                    position.x + 0.18, position.y + 0.36, position.z + 0.18);
            int tint = colorFor(item);
            Render3D.lines(buffer -> buffer.box(box, tint, 1.8f));
            if (fill.get()) {
                Render3D.quads(buffer -> buffer.box(box, ColorUtil.withAlpha(tint, 35)));
            }
            if (labels.get()) {
                String text = item.getItem().getHoverName().getString();
                if (item.getItem().getCount() > 1) {
                    text += " x" + item.getItem().getCount();
                }
                if (distance.get()) {
                    text += String.format(java.util.Locale.ROOT, "  %.0f m", item.distanceTo(client.player));
                }
                try {
                    PoseStack matrices = context.matrices();
                    matrices.pushPose();
                    matrices.translate(position.x - camera.x, position.y + 0.6 - camera.y, position.z - camera.z);
                    matrices.mulPose(client.gameRenderer.getMainCamera().rotation());
                    float scale = -0.022f;
                    matrices.scale(scale, scale, scale);
                    int width = client.font.width(text);
                    context.commandQueue().submitText(matrices, -width / 2f, 0f,
                            Component.literal(text).getVisualOrderText(), true,
                            Font.DisplayMode.SEE_THROUGH, 0xF000F0, tint, 0, 0);
                    matrices.popPose();
                } catch (Throwable throwable) {
                    su.aurora.Aurora.LOG.warn("[ItemESP] подпись не нарисовалась: {}", throwable.getMessage());
                    return;
                }
            }
        }
    }
}
