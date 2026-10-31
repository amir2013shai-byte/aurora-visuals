package su.aurora.modules.render;

import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import su.aurora.core.Category;
import su.aurora.core.Module;
import su.aurora.setting.BooleanSetting;
import su.aurora.setting.ColorSetting;
import su.aurora.setting.NumberSetting;
import su.aurora.util.ColorUtil;
import su.aurora.util.Render3D;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * BlockPlace — анимация только что поставленных блоков: разлетающаяся рамка + вспышка.
 * Блоки ищутся сканом вокруг игрока (появление «воздух → блок»).
 */
public class BlockPlaceModule extends Module {
    private final NumberSetting range = addNumber("Дистанция", 6, 2, 16, 1);
    private final NumberSetting duration = addNumber("Длительность", 0.5, 0.1, 2.0, 0.05);
    private final NumberSetting scale = addNumber("Размер", 1.0, 0.2, 2.0, 0.05);
    private final BooleanSetting fill = addBoolean("Заливка", true);
    private final ColorSetting color = addColor("Цвет", 0xFF7C5CFF);

    private final Set<BlockPos> known = new HashSet<>();
    private final List<Animation> animations = new ArrayList<>();
    private int cooldown;

    private record Animation(BlockPos pos, long start) {
    }

    public BlockPlaceModule() {
        super("BlockPlace", "Анимация установки блока: вспышка и рамка", Category.RENDER);
    }

    @Override
    public void onEnable() {
        known.clear();
        animations.clear();
        scan(true);
    }

    @Override
    public void onDisable() {
        known.clear();
        animations.clear();
    }

    private void scan(boolean rememberOnly) {
        Minecraft client = Minecraft.getInstance();
        if (client.level == null || client.player == null) {
            return;
        }
        int r = range.getInt();
        BlockPos origin = client.player.blockPosition();
        long now = System.currentTimeMillis();
        for (BlockPos pos : BlockPos.betweenClosed(origin.offset(-r, -r, -r), origin.offset(r, r, r))) {
            BlockPos immutable = pos.immutable();
            boolean solid = !client.level.getBlockState(immutable).isAir();
            if (solid) {
                if (!known.contains(immutable)) {
                    known.add(immutable);
                    if (!rememberOnly) {
                        animations.add(new Animation(immutable, now));
                    }
                }
            } else {
                known.remove(immutable);
            }
        }
    }

    @Override
    public void onTick() {
        if (++cooldown < 3) {
            return;
        }
        cooldown = 0;
        scan(false);
        long limit = (long) (duration.get() * 1000);
        animations.removeIf(animation -> System.currentTimeMillis() - animation.start() > limit);
    }

    @Override
    public void onWorldRender(WorldRenderContext context) {
        if (animations.isEmpty()) {
            return;
        }
        long limit = (long) (duration.get() * 1000);
        int tint = color.get();
        double s = scale.get() * 0.5;
        for (Iterator<Animation> it = animations.iterator(); it.hasNext(); ) {
            Animation animation = it.next();
            long age = System.currentTimeMillis() - animation.start();
            if (age > limit) {
                it.remove();
                continue;
            }
            float progress = age / (float) Math.max(1, limit);
            double grow = s * (0.55 + 0.75 * progress);
            Vec3 center = new Vec3(animation.pos().getX() + 0.5, animation.pos().getY() + 0.5,
                    animation.pos().getZ() + 0.5);
            AABB box = new AABB(center.x - grow, center.y - grow, center.z - grow,
                    center.x + grow, center.y + grow, center.z + grow);
            int alpha = (int) (255 * (1f - progress));
            Render3D.lines(buffer -> buffer.box(box, ColorUtil.withAlpha(tint, alpha), 2.0f));
            if (fill.get()) {
                Render3D.quads(buffer -> buffer.box(box, ColorUtil.withAlpha(tint, (int) (alpha * 0.18f))));
            }
        }
    }
}
