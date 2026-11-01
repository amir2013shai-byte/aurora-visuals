package su.aurora.modules.render;

import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.VoxelShape;
import su.aurora.core.Category;
import su.aurora.core.Module;
import su.aurora.setting.BooleanSetting;
import su.aurora.setting.ColorSetting;
import su.aurora.setting.NumberSetting;
import su.aurora.util.ColorUtil;
import su.aurora.util.Render3D;

/**
 * BlockOverlay — обводка блока под прицелом с учётом его реальной формы.
 */
public class BlockOverlayModule extends Module {
    private final ColorSetting color = addColor("Цвет", 0xFF7C5CFF);
    private final NumberSetting thickness = addNumber("Толщина", 2.0, 0.5, 6.0, 0.5);
    private final BooleanSetting fill = addBoolean("Заливка", false);
    private final BooleanSetting shape = addBoolean("Точная форма", true);

    public BlockOverlayModule() {
        super("BlockOverlay", "Обводка блока под прицелом", Category.RENDER);
    }

    @Override
    public void onWorldRender(WorldRenderContext context) {
        Minecraft client = Minecraft.getInstance();
        if (client.level == null || client.player == null) {
            return;
        }
        HitResult hit = client.hitResult;
        if (!(hit instanceof BlockHitResult blockHit)) {
            return;
        }
        BlockPos pos = blockHit.getBlockPos();
        BlockState state = client.level.getBlockState(pos);
        if (state.isAir()) {
            return;
        }
        AABB box;
        if (shape.get()) {
            VoxelShape voxelShape = state.getShape(client.level, pos);
            box = voxelShape.isEmpty() ? new AABB(pos) : voxelShape.bounds().move(pos);
        } else {
            box = new AABB(pos);
        }
        box = box.inflate(0.002);
        AABB finalBox = box;
        Render3D.lines(buffer -> buffer.box(finalBox, color.get(), thickness.getFloat()));
        if (fill.get()) {
            Render3D.quads(buffer -> buffer.box(finalBox, ColorUtil.withAlpha(color.get(), 40)));
        }
    }
}
