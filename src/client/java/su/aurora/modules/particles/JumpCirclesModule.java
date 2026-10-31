package su.aurora.modules.particles;

import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.phys.Vec3;
import su.aurora.core.Category;
import su.aurora.core.Module;
import su.aurora.setting.BooleanSetting;
import su.aurora.setting.ColorSetting;
import su.aurora.setting.ModeSetting;
import su.aurora.setting.NumberSetting;
import su.aurora.util.ColorUtil;
import su.aurora.util.Render3D;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * JumpCircles — волна под ногами при прыжке.
 */
public class JumpCirclesModule extends Module {
    private final ModeSetting mode = addMode("Вид", "Круг", "Круг", "Двойной", "Полигон");
    private final NumberSetting size = addNumber("Размер", 1.0, 0.4, 2.5, 0.05);
    private final NumberSetting life = addInt("Длительность", 700, 200, 2000);
    private final ColorSetting color = addColor("Цвет", 0xFF7C5CFF);
    private final BooleanSetting particles = addBoolean("Частицы", true);

    private static class Ring {
        final Vec3 position;
        final long created;
        final int color;

        Ring(Vec3 position, int color) {
            this.position = position;
            this.color = color;
            this.created = System.currentTimeMillis();
        }
    }

    private final List<Ring> rings = new ArrayList<>();
    private boolean wasOnGround = true;

    public JumpCirclesModule() {
        super("JumpCircle", "Волна под ногами при прыжке", Category.PARTICLES);
        setEnabled(true);
    }

    @Override
    public void onDisable() {
        rings.clear();
    }

    @Override
    public void onTick() {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null || client.level == null) {
            return;
        }
        boolean onGround = client.player.onGround();
        if (wasOnGround && !onGround && client.player.getDeltaMovement().y > 0.05) {
            rings.add(new Ring(new Vec3(client.player.getX(), client.player.getY() + 0.03, client.player.getZ()),
                    color.get()));
            if (particles.get()) {
                for (int i = 0; i < 14; i++) {
                    double angle = Math.random() * Math.PI * 2;
                    double speed = 0.12 + Math.random() * 0.18;
                    client.level.addParticle(ParticleTypes.END_ROD,
                            client.player.getX() + Math.cos(angle) * 0.25,
                            client.player.getY() + 0.1,
                            client.player.getZ() + Math.sin(angle) * 0.25,
                            Math.cos(angle) * speed, 0.02, Math.sin(angle) * speed);
                }
            }
        }
        wasOnGround = onGround;

        Iterator<Ring> iterator = rings.iterator();
        while (iterator.hasNext()) {
            if (System.currentTimeMillis() - iterator.next().created > life.getInt()) {
                iterator.remove();
            }
        }
    }

    @Override
    public void onWorldRender(WorldRenderContext context) {
        if (rings.isEmpty()) {
            return;
        }
        for (Ring ring : rings) {
            float progress = (System.currentTimeMillis() - ring.created) / (float) life.getInt();
            if (progress > 1f) {
                continue;
            }
            double radius = (0.25 + progress * 1.35) * size.get();
            int lineColor = ColorUtil.withAlpha(ring.color, (int) ((1f - progress) * 210));
            int fillColor = ColorUtil.withAlpha(ring.color, (int) ((1f - progress) * 45));
            switch (mode.get()) {
                case "Двойной" -> {
                    Render3D.lines(buffer -> {
                        buffer.circle(ring.position, radius, lineColor, 2.5f, 64);
                        buffer.circle(ring.position, radius * 0.72, ColorUtil.scaleAlpha(lineColor, 0.6f), 2f, 64);
                    });
                    Render3D.quads(buffer -> buffer.ring(ring.position, radius * 0.72, radius, fillColor, 48));
                }
                case "Полигон" -> Render3D.lines(buffer ->
                        buffer.polygon(ring.position, radius, 6, lineColor, 2.5f, progress * 1.5));
                default -> {
                    Render3D.lines(buffer -> buffer.circle(ring.position, radius, lineColor, 2.5f, 64));
                    Render3D.quads(buffer -> buffer.ring(ring.position, radius * 0.85, radius, fillColor, 48));
                }
            }
        }
    }
}
