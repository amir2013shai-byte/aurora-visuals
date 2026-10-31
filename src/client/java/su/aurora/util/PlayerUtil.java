package su.aurora.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public final class PlayerUtil {
    private PlayerUtil() {
    }

    public static Minecraft mc() {
        return Minecraft.getInstance();
    }

    public static float health(LivingEntity entity) {
        return entity.getHealth() + entity.getAbsorptionAmount();
    }

    public static float healthPercent(LivingEntity entity) {
        float max = entity.getMaxHealth();
        return max <= 0 ? 0 : Math.min(1f, health(entity) / max);
    }

    /** Цель под прицелом: сущность, на которую смотрит игрок, либо ближайший игрок. */
    public static LivingEntity crosshairTarget(double range) {
        Minecraft client = mc();
        if (client.player == null || client.level == null) {
            return null;
        }
        Vec3 eyes = client.player.getEyePosition();
        Vec3 look = client.player.getLookAngle().scale(range);
        Vec3 end = eyes.add(look);
        LivingEntity best = null;
        double bestDistance = Double.MAX_VALUE;
        for (Entity entity : client.level.entitiesForRendering()) {
            if (!(entity instanceof LivingEntity living) || living == client.player || !living.isAlive()) {
                continue;
            }
            if (living.isInvisible()) {
                continue;
            }
            if (living.getBoundingBox().inflate(0.35).clip(eyes, end).isEmpty()) {
                continue;
            }
            double distance = living.distanceToSqr(client.player);
            if (distance < bestDistance) {
                bestDistance = distance;
                best = living;
            }
        }
        return best;
    }

    public static LivingEntity nearestPlayer(double range) {
        Minecraft client = mc();
        if (client.player == null || client.level == null) {
            return null;
        }
        LivingEntity best = null;
        double bestDistance = range * range;
        for (Player player : client.level.players()) {
            if (player == client.player || !player.isAlive() || player.isInvisible()) {
                continue;
            }
            double distance = player.distanceToSqr(client.player);
            if (distance < bestDistance) {
                bestDistance = distance;
                best = player;
            }
        }
        return best;
    }

    public static String targetName(LivingEntity entity) {
        if (entity == null) {
            return "—";
        }
        return entity.getName().getString();
    }

    public static int ping() {
        Minecraft client = mc();
        if (client.player == null || client.getConnection() == null) {
            return 0;
        }
        PlayerInfo info = client.getConnection().getPlayerInfo(client.player.getUUID());
        return info == null ? 0 : info.getLatency();
    }

    public static int fps() {
        return mc().getFps();
    }

    public static Vec3 playerPosition() {
        Player player = mc().player;
        return player == null ? Vec3.ZERO : player.position();
    }
}
