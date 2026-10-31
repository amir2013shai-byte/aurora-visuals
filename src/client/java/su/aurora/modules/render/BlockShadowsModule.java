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
import su.aurora.setting.NumberSetting;
import su.aurora.util.ColorUtil;
import su.aurora.util.Render3D;

/**
 * BlockShadows — мягкие тени под сущностями: тёмный диск на земле, гаснет с дистанцией.
 */
public class BlockShadowsModule extends Module {
    private final NumberSetting size = addNumber("Размер", 0.42, 0.1, 1.5, 0.02);
    private final NumberSetting opacity = addNumber("Прозрачность", 90, 10, 220, 5);
    private final NumberSetting range = addNumber("Дистанция", 32, 4, 96, 4);
    private final BooleanSetting onlyPlayers = addBoolean("Только игроки", false);
    private final ColorSetting color = addColor("Цвет тени", 0xFF000000);

    public BlockShadowsModule() {
        super("BlockShadows", "Тени под сущностями и игроками", Category.RENDER);
    }

    @Override
    public void onWorldRender(WorldRenderContext context) {
        Minecraft client = Minecraft.getInstance();
        if (client.level == null || client.player == null) {
            return;
        }
        float partial = Render3D.tickDelta();
        double rangeSq = range.get() * range.get();
        int base = color.get();
        int alpha = opacity.getInt();

        for (Entity entity : client.level.entitiesForRendering()) {
            if (!(entity instanceof LivingEntity living) || living == client.player) {
                continue;
            }
            if (onlyPlayers.get() && !(living instanceof Player)) {
                continue;
            }
            if (living.distanceToSqr(client.player) > rangeSq) {
                continue;
            }
            Vec3 feet = new Vec3(living.getX(partial), living.getY(partial) + 0.02, living.getZ(partial));
            double s = size.get() * Math.max(0.6, living.getBbWidth());
            double distanceFade = 1.0 - Math.min(1.0, living.distanceTo(client.player) / range.get());
            int tint = ColorUtil.withAlpha(base, (int) (alpha * (0.35 + 0.65 * distanceFade)));
            Render3D.quads(buffer -> buffer.disc(feet, s, tint, 20));
        }
    }
}
