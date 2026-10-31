package su.aurora.modules.render;

import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
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
 * ItemHighlight — подсветка выпавших предметов на земле.
 */
public class ItemHighlightModule extends Module {
    private final ModeSetting mode = addMode("Вид", "Рамка", "Рамка", "Заливка", "Рамка + заливка");
    private final NumberSetting range = addNumber("Дистанция", 24, 4, 64, 1);
    private final ColorSetting color = addColor("Цвет", 0xFFFFD54F);
    private final BooleanSetting pulse = addBoolean("Пульсация", true);

    public ItemHighlightModule() {
        super("ItemHighlight", "Подсвечивает предметы, лежащие на земле", Category.RENDER);
    }

    @Override
    public void onWorldRender(WorldRenderContext context) {
        Minecraft client = Minecraft.getInstance();
        if (client.level == null || client.player == null) {
            return;
        }
        float partial = Render3D.tickDelta();
        double rangeSq = range.get() * range.get();
        float pulseFactor = pulse.get()
                ? (float) ((Math.sin(System.currentTimeMillis() / 300.0) + 1.0) * 0.5)
                : 1.0f;

        for (Entity entity : client.level.entitiesForRendering()) {
            if (!(entity instanceof ItemEntity itemEntity)) {
                continue;
            }
            if (entity.distanceToSqr(client.player) > rangeSq) {
                continue;
            }
            ItemStack stack = itemEntity.getItem();
            if (stack.isEmpty()) {
                continue;
            }
            AABB box = itemEntity.getBoundingBox().inflate(0.12);
            int base = color.get();
            int outline = ColorUtil.withAlpha(base, (int) (160 + 95 * pulseFactor));
            int fill = ColorUtil.withAlpha(base, (int) (40 + 40 * pulseFactor));
            boolean drawOutline = mode.is("Рамка") || mode.is("Рамка + заливка");
            boolean drawFill = mode.is("Заливка") || mode.is("Рамка + заливка");
            if (drawOutline) {
                Render3D.lines(buffer -> buffer.box(box, outline, 2f));
            }
            if (drawFill) {
                Render3D.quads(buffer -> buffer.box(box, fill));
            }
            Render3D.lines(buffer -> buffer.line(
                    box.minX, box.minY - 0.05, box.minZ,
                    box.maxX, box.minY - 0.05, box.maxZ, ColorUtil.withAlpha(base, 90), 1.5f));
        }
    }
}
