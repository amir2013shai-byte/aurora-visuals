package su.aurora.modules.particles;

import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import su.aurora.AuroraClient;
import su.aurora.core.Category;
import su.aurora.core.Module;
import su.aurora.setting.BooleanSetting;
import su.aurora.setting.ColorSetting;
import su.aurora.setting.ModeSetting;
import su.aurora.setting.NumberSetting;
import su.aurora.util.ColorUtil;
import su.aurora.util.Render3D;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * KillEffect — эффект на убийство: вспышка, кольца, частицы и уведомление.
 */
public class KillEffectModule extends Module {
    private final ModeSetting mode = addMode("Стиль", "Взрыв", "Взрыв", "Молния", "Кольца", "Души");
    private final NumberSetting life = addInt("Длительность", 1200, 400, 3000);
    private final BooleanSetting notification = addBoolean("Уведомление", true);
    private final ColorSetting color = addColor("Цвет", 0xFFFF5C5C);

    private static class Effect {
        final Vec3 position;
        final long created;
        final String name;

        Effect(Vec3 position, String name) {
            this.position = position;
            this.name = name;
            this.created = System.currentTimeMillis();
        }
    }

    private final List<Effect> effects = new ArrayList<>();
    private final Map<Integer, LivingEntity> tracked = new HashMap<>();

    public KillEffectModule() {
        super("KillEffect", "Красивый эффект при убийстве цели", Category.PARTICLES);
        setEnabled(true);
    }

    @Override
    public void onDisable() {
        effects.clear();
        tracked.clear();
    }

    @Override
    public void onTick() {
        Minecraft client = Minecraft.getInstance();
        if (client.level == null || client.player == null) {
            return;
        }

        // сущность «умерла» только если она была жива и исчезла/умерла, а не просто вышла из зоны стриминга
        var iterator = tracked.entrySet().iterator();
        while (iterator.hasNext()) {
            var entry = iterator.next();
            LivingEntity entity = entry.getValue();
            if (entity == null || entity.isRemoved() || entity.isDeadOrDying()) {
                trigger(entity == null ? "Цель" : entity.getName().getString());
                iterator.remove();
            }
        }

        for (Entity entity : client.level.entitiesForRendering()) {
            if (!(entity instanceof LivingEntity living) || living == client.player) {
                continue;
            }
            if (living.distanceToSqr(client.player) > 40 * 40) {
                continue;
            }
            tracked.put(living.getId(), living);
        }

        Iterator<Effect> effectIterator = effects.iterator();
        while (effectIterator.hasNext()) {
            if (System.currentTimeMillis() - effectIterator.next().created > life.getInt()) {
                effectIterator.remove();
            }
        }
    }

    private void trigger(String name) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null || client.level == null) {
            return;
        }
        Vec3 position = new Vec3(client.player.getX(), client.player.getY(), client.player.getZ());
        // эффект появляется у последней известной позиции цели — берём позицию игрока как якорь
        effects.add(new Effect(position, name));

        ParticleOptions particle = switch (mode.get()) {
            case "Молния" -> ParticleTypes.ELECTRIC_SPARK;
            case "Души" -> ParticleTypes.SOUL_FIRE_FLAME;
            case "Кольца" -> ParticleTypes.END_ROD;
            default -> ParticleTypes.EXPLOSION;
        };
        for (int i = 0; i < 40; i++) {
            double angle = Math.random() * Math.PI * 2;
            double speed = 0.2 + Math.random() * 0.5;
            client.level.addParticle(particle,
                    position.x + Math.cos(angle) * 0.4,
                    position.y + 0.4 + Math.random() * 1.2,
                    position.z + Math.sin(angle) * 0.4,
                    Math.cos(angle) * speed, 0.12 + Math.random() * 0.2, Math.sin(angle) * speed);
        }

        if (notification.get() && AuroraClient.NOTIFICATIONS != null) {
            AuroraClient.NOTIFICATIONS.push("Kill", name + " повержен", color.get(), 3000L);
        }
    }

    @Override
    public void onWorldRender(WorldRenderContext context) {
        for (Effect effect : effects) {
            float progress = (System.currentTimeMillis() - effect.created) / (float) life.getInt();
            if (progress > 1f) {
                continue;
            }
            int lineColor = ColorUtil.withAlpha(color.get(), (int) ((1f - progress) * 230));
            Render3D.lines(buffer -> {
                if (mode.is("Молния")) {
                    double height = 4.0;
                    Vec3 previous = effect.position;
                    for (int i = 1; i <= 12; i++) {
                        double t = i / 12.0;
                        Vec3 point = effect.position.add((Math.random() - 0.5) * 0.5, height * t,
                                (Math.random() - 0.5) * 0.5);
                        buffer.line(previous, point, lineColor, 2.5f);
                        previous = point;
                    }
                    buffer.circle(effect.position.add(0, 0.05, 0), 0.6 + progress * 1.4, lineColor, 2f, 56);
                } else {
                    for (int i = 0; i < 3; i++) {
                        double radius = 0.3 + progress * (1.4 + i * 0.5);
                        buffer.circle(effect.position.add(0, 0.05 + i * 0.55, 0), radius,
                                ColorUtil.scaleAlpha(lineColor, 1f - i * 0.25f), 2.5f, 64);
                    }
                }
            });
        }
    }
}
