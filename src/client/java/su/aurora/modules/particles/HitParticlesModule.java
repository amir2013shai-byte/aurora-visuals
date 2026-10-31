package su.aurora.modules.particles;

import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import su.aurora.core.Category;
import su.aurora.core.Module;
import su.aurora.setting.ModeSetting;
import su.aurora.setting.NumberSetting;

import java.util.HashMap;
import java.util.Map;

/**
 * HitParticles — частицы в момент попадания по сущности.
 */
public class HitParticlesModule extends Module {
    private final ModeSetting mode = addMode("Стиль", "Крит", "Крит", "Сердца", "Искры", "Дым");
    private final NumberSetting amount = addInt("Количество", 14, 3, 60);
    private final NumberSetting range = addNumber("Дистанция", 6, 1, 24, 0.5);

    private final Map<Integer, Float> lastHealth = new HashMap<>();

    public HitParticlesModule() {
        super("HitParticles", "Частицы при попадании по цели", Category.PARTICLES);
        setEnabled(true);
    }

    @Override
    public void onDisable() {
        lastHealth.clear();
    }

    @Override
    public void onTick() {
        Minecraft client = Minecraft.getInstance();
        if (client.level == null || client.player == null) {
            return;
        }
        double rangeSq = range.get() * range.get();
        Map<Integer, Float> seen = new HashMap<>();

        for (Entity entity : client.level.entitiesForRendering()) {
            if (!(entity instanceof LivingEntity living) || living == client.player) {
                continue;
            }
            if (living.distanceToSqr(client.player) > rangeSq) {
                continue;
            }
            int id = living.getId();
            float health = living.getHealth();
            seen.put(id, health);
            Float previous = lastHealth.get(id);
            if (previous != null && health < previous - 0.001f) {
                spawn(living);
            }
        }
        lastHealth.clear();
        lastHealth.putAll(seen);
    }

    private void spawn(LivingEntity entity) {
        Minecraft client = Minecraft.getInstance();
        if (client.level == null) {
            return;
        }
        ParticleOptions particle = switch (mode.get()) {
            case "Сердца" -> ParticleTypes.HEART;
            case "Искры" -> ParticleTypes.ELECTRIC_SPARK;
            case "Дым" -> ParticleTypes.LARGE_SMOKE;
            default -> ParticleTypes.CRIT;
        };
        double x = entity.getX();
        double y = entity.getY() + entity.getBbHeight() * 0.55;
        double z = entity.getZ();
        for (int i = 0; i < amount.getInt(); i++) {
            double dx = (Math.random() - 0.5) * 0.55;
            double dy = (Math.random() - 0.2) * 0.55;
            double dz = (Math.random() - 0.5) * 0.55;
            client.level.addParticle(particle, x + dx * 0.4, y + dy * 0.4, z + dz * 0.4, dx, dy, dz);
        }
    }
}
