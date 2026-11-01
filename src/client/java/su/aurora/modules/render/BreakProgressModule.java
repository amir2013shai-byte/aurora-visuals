package su.aurora.modules.render;

import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import su.aurora.core.Category;
import su.aurora.core.Module;
import su.aurora.mixin.GameModeAccessor;
import su.aurora.setting.BooleanSetting;
import su.aurora.setting.ColorSetting;
import su.aurora.setting.NumberSetting;
import su.aurora.util.ColorUtil;
import su.aurora.util.Render3D;

/**
 * BreakProgress — прогресс ломания блока: рамка, заливка по проценту и полоска над блоком.
 */
public class BreakProgressModule extends Module {
    private final ColorSetting color = addColor("Цвет", 0xFF7CF7B0);
    private final NumberSetting thickness = addNumber("Толщина", 2.0, 0.5, 6.0, 0.5);
    private final BooleanSetting fill = addBoolean("Заливка", true);
    private final BooleanSetting bar = addBoolean("Полоска сверху", true);

    public BreakProgressModule() {
        super("BreakProgress", "Прогресс ломания блока в мире", Category.RENDER);
    }

    @Override
    public void onWorldRender(WorldRenderContext context) {
        Minecraft client = Minecraft.getInstance();
        if (client.level == null || client.gameMode == null) {
            return;
        }
        GameModeAccessor accessor = (GameModeAccessor) client.gameMode;
        if (!accessor.aurora$isDestroying()) {
            return;
        }
        BlockPos pos = accessor.aurora$destroyBlockPos();
        if (pos == null) {
            return;
        }
        float progress = Math.min(1.0f, accessor.aurora$destroyProgress());
        AABB box = new AABB(pos).inflate(0.01);
        int tint = color.get();
        Render3D.lines(buffer -> buffer.box(box, tint, thickness.getFloat()));
        if (fill.get()) {
            double height = Math.max(0.02, progress);
            AABB fillBox = new AABB(box.minX, box.minY, box.minZ, box.maxX, box.minY + height, box.maxZ);
            Render3D.quads(buffer -> buffer.box(fillBox, ColorUtil.withAlpha(tint, 70)));
        }
        if (bar.get()) {
            double y = pos.getY() + 1.15;
            Vec3 left = new Vec3(pos.getX() - 0.1, y, pos.getZ() + 0.5);
            Vec3 right = new Vec3(pos.getX() + 1.1, y + 0.06, pos.getZ() + 0.5);
            Vec3 depth = new Vec3(0, 0, 0.02);
            Render3D.quads(buffer -> {
                buffer.quad(left, right, right.add(depth), left.add(depth), 0xB0101218);
                Vec3 filledRight = new Vec3(left.x + (right.x - left.x) * progress, right.y, right.z);
                buffer.quad(left.add(0, 0.01, -0.001), filledRight, filledRight.add(depth),
                        left.add(0, 0.01, -0.001).add(depth), tint);
            });
        }
    }

    @Override
    public void onTick() {
        if (Minecraft.getInstance().gameMode == null) {
            return;
        }
    }
}
