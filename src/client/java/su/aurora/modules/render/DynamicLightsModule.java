package su.aurora.modules.render;

import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import su.aurora.core.Category;
import su.aurora.core.Module;
import su.aurora.setting.BooleanSetting;
import su.aurora.setting.ColorSetting;
import su.aurora.setting.NumberSetting;
import su.aurora.util.ColorUtil;
import su.aurora.util.Render3D;

import java.util.Set;

/**
 * DynamicLights — светящееся гало вокруг источников света в руках (факел, фонарь,
 * светокамень и т.п.). Настоящий динамический свет требует правки LightEngine,
 * поэтому здесь честная визуальная имитация: гало + пульсация.
 */
public class DynamicLightsModule extends Module {
    private static final Set<Item> LIGHT_ITEMS = Set.of(
            Items.TORCH, Items.SOUL_TORCH, Items.LANTERN, Items.SOUL_LANTERN,
            Items.GLOWSTONE, Items.SEA_LANTERN, Items.SHROOMLIGHT, Items.END_ROD,
            Items.JACK_O_LANTERN, Items.CAMPFIRE, Items.SOUL_CAMPFIRE, Items.LAVA_BUCKET,
            Items.GLOW_BERRIES, Items.BEACON, Items.CONDUIT, Items.REDSTONE_LAMP,
            Items.OCHRE_FROGLIGHT, Items.VERDANT_FROGLIGHT, Items.PEARLESCENT_FROGLIGHT);

    private final NumberSetting radius = addNumber("Радиус", 2.4, 0.5, 8.0, 0.1);
    private final NumberSetting intensity = addNumber("Интенсивность", 120, 20, 255, 5);
    private final BooleanSetting players = addBoolean("Другие игроки", true);
    private final BooleanSetting pulse = addBoolean("Пульсация", true);
    private final ColorSetting color = addColor("Цвет света", 0xFFFFE9A8);

    public DynamicLightsModule() {
        super("DynamicLights", "Светящееся гало у источников света в руках", Category.RENDER);
    }

    private boolean isLight(ItemStack stack) {
        return !stack.isEmpty() && LIGHT_ITEMS.contains(stack.getItem());
    }

    @Override
    public void onWorldRender(WorldRenderContext context) {
        Minecraft client = Minecraft.getInstance();
        if (client.level == null || client.player == null) {
            return;
        }
        float partial = Render3D.tickDelta();
        float time = System.currentTimeMillis() / 1000f;
        float pulsation = pulse.get() ? (float) (0.85 + 0.15 * Math.sin(time * 2.2)) : 1f;
        int alpha = (int) (intensity.get() * pulsation);

        for (Player player : client.level.players()) {
            if (player != client.player && !players.get()) {
                continue;
            }
            if (player.distanceToSqr(client.player) > 64 * 64) {
                continue;
            }
            ItemStack main = player.getMainHandItem();
            ItemStack off = player.getOffhandItem();
            ItemStack source = isLight(main) ? main : isLight(off) ? off : ItemStack.EMPTY;
            if (source.isEmpty()) {
                continue;
            }
            Vec3 pos = new Vec3(player.getX(partial), player.getY(partial) + 1.1, player.getZ(partial));
            drawGlow(pos, alpha);
        }
    }

    private void drawGlow(Vec3 center, int alpha) {
        double r = radius.get();
        int tint = ColorUtil.withAlpha(color.get(), alpha);
        Render3D.quads(buffer -> {
            buffer.disc(center.add(0, -0.6, 0), r * 0.8, ColorUtil.withAlpha(color.get(), (int) (alpha * 0.25)), 18);
            buffer.ring(center.add(0, -0.6, 0), r * 0.55, r * 0.8, ColorUtil.withAlpha(color.get(), (int) (alpha * 0.35)), 18);
        });
        Render3D.lines(buffer -> {
            buffer.circle(center, r * 0.45, ColorUtil.withAlpha(color.get(), alpha), 1.6f, 20);
            buffer.circle(center.add(0, 0.25, 0), r * 0.3, ColorUtil.withAlpha(color.get(), (int) (alpha * 0.7)), 1.2f, 16);
        });
        Render3D.quads(buffer -> buffer.box(
                new net.minecraft.world.phys.AABB(center.x - 0.12, center.y - 0.12, center.z - 0.12,
                        center.x + 0.12, center.y + 0.12, center.z + 0.12), ColorUtil.withAlpha(color.get(), (int) (alpha * 0.9))));
    }
}
