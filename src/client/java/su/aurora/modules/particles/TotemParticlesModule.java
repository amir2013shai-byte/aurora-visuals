package su.aurora.modules.particles;

import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleOptions;
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
 * TotemParticles — свои частицы и волна при срабатывании тотема.
 * Ловит ванильную частицу TOTEM_OF_UNDYING через миксин ParticleEngine.
 */
public class TotemParticlesModule extends Module {
    private final ModeSetting mode = addMode("Стиль", "Взрыв", "Взрыв", "Вихрь", "Сфера", "Кольцо");
    private final NumberSetting amount = addInt("Количество", 60, 10, 300);
    private final NumberSetting life = addInt("Длительность", 900, 300, 2500);
    private final BooleanSetting hideVanilla = addBoolean("Убрать ванильные", true);
    private final ColorSetting color = addColor("Цвет", 0xFFFFE082);

    private static class Burst {
        final Vec3 position;
        final long created;

        Burst(Vec3 position) {
            this.position = position;
            this.created = System.currentTimeMillis();
        }
    }

    private final List<Burst> bursts = new ArrayList<>();

    public TotemParticlesModule() {
        super("TotemParticles", "Свои частицы при срабатывании тотема", Category.PARTICLES);
        setEnabled(true);
    }

    @Override
    public void onDisable() {
        bursts.clear();
    }

    @Override
    public void onTick() {
        Iterator<Burst> iterator = bursts.iterator();
        while (iterator.hasNext()) {
            if (System.currentTimeMillis() - iterator.next().created > life.getInt()) {
                iterator.remove();
            }
        }
    }

    /** Вызывается из миксина ParticleEngine#add. Возвращает true, если ванильную частицу нужно скрыть. */
    public boolean onParticle(ParticleOptions options, double x, double y, double z) {
        Minecraft client = Minecraft.getInstance();
        if (client.level == null || !(options instanceof net.minecraft.core.particles.SimpleParticleType simple)) {
            return false;
        }
        if (simple != ParticleTypes.TOTEM_OF_UNDYING) {
            return false;
        }
        Vec3 position = new Vec3(x, y, z);
        bursts.add(new Burst(position));

        int count = amount.getInt();
        for (int i = 0; i < count; i++) {
            double theta = Math.random() * Math.PI * 2;
            double phi = Math.acos(2 * Math.random() - 1);
            double speed = 0.15 + Math.random() * 0.45;
            double dx = Math.sin(phi) * Math.cos(theta) * speed;
            double dy = Math.cos(phi) * speed;
            double dz = Math.sin(phi) * Math.sin(theta) * speed;
            ParticleOptions particle = switch (mode.get()) {
                case "Вихрь" -> ParticleTypes.END_ROD;
                case "Сфера" -> ParticleTypes.ELECTRIC_SPARK;
                case "Кольцо" -> ParticleTypes.FIREWORK;
                default -> ParticleTypes.TOTEM_OF_UNDYING;
            };
            client.level.addParticle(particle, x + dx * 0.2, y + 0.4 + dy * 0.2, z + dz * 0.2, dx, dy, dz);
        }
        return hideVanilla.get();
    }

    @Override
    public void onWorldRender(WorldRenderContext context) {
        for (Burst burst : bursts) {
            float progress = (System.currentTimeMillis() - burst.created) / (float) life.getInt();
            if (progress > 1f) {
                continue;
            }
            int lineColor = ColorUtil.withAlpha(color.get(), (int) ((1f - progress) * 220));
            Render3D.lines(buffer -> {
                if (mode.is("Кольцо")) {
                    buffer.circle(burst.position.add(0, 0.1, 0), 0.4 + progress * 2.2, lineColor, 2.5f, 72);
                } else {
                    buffer.orbit(burst.position.add(0, 0.9, 0), 0.5 + progress * 1.2, lineColor, 2f, 48,
                            0.6, progress * 3);
                    buffer.orbit(burst.position.add(0, 0.9, 0), 0.4 + progress, lineColor, 2f, 48, -0.8,
                            -progress * 2.4);
                }
            });
        }
    }
}
