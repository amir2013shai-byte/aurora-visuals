package su.aurora.modules.player;

import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import su.aurora.core.Category;
import su.aurora.core.Module;
import su.aurora.setting.BooleanSetting;
import su.aurora.setting.ModeSetting;
import su.aurora.setting.NumberSetting;

import java.util.HashMap;
import java.util.Map;

/**
 * HitSounds — звук попадания по цели (клиентский, сервер о нём не знает).
 * Работает так же, как HitParticles: следит за уменьшением здоровья сущности.
 */
public class HitSoundsModule extends Module {
    private final ModeSetting sound = addMode("Звук", "Pling",
            "Pling", "Опыт", "Колокол", "Анвил", "Щелчок");
    private final NumberSetting volume = addNumber("Громкость", 0.8, 0.1, 1.0, 0.05);
    private final NumberSetting pitch = addNumber("Тон", 1.2, 0.5, 2.0, 0.05);
    private final NumberSetting range = addNumber("Дистанция", 6, 1, 24, 0.5);
    private final BooleanSetting onlyPlayers = addBoolean("Только по игрокам", false);

    private final Map<Integer, Float> lastHealth = new HashMap<>();

    public HitSoundsModule() {
        super("HitSounds", "Звук при попадании по цели", Category.PLAYER);
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
            if (onlyPlayers.get() && !(living instanceof net.minecraft.world.entity.player.Player)) {
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
                play(living);
            }
        }
        lastHealth.clear();
        lastHealth.putAll(seen);
    }

    private void play(LivingEntity entity) {
        Minecraft client = Minecraft.getInstance();
        if (client.level == null) {
            return;
        }
        SoundEvent event = switch (sound.get()) {
            case "Опыт" -> SoundEvents.EXPERIENCE_ORB_PICKUP;
            case "Колокол" -> SoundEvents.NOTE_BLOCK_BELL.value();
            case "Анвил" -> SoundEvents.ANVIL_LAND;
            case "Щелчок" -> SoundEvents.UI_BUTTON_CLICK.value();
            default -> SoundEvents.NOTE_BLOCK_PLING.value();
        };
        client.level.playLocalSound(entity.getX(), entity.getY() + entity.getBbHeight() * 0.6, entity.getZ(),
                event, SoundSource.PLAYERS, volume.getFloat(), pitch.getFloat(), false);
    }
}
