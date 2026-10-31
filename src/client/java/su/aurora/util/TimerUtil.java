package su.aurora.util;

import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;

public final class TimerUtil {
    private long last = System.currentTimeMillis();

    public boolean hasReached(long milliseconds) {
        if (System.currentTimeMillis() - last >= milliseconds) {
            last = System.currentTimeMillis();
            return true;
        }
        return false;
    }

    public boolean passed(long milliseconds) {
        return System.currentTimeMillis() - last >= milliseconds;
    }

    public void reset() {
        last = System.currentTimeMillis();
    }

    public long elapsed() {
        return System.currentTimeMillis() - last;
    }

    public static float partialTick() {
        Minecraft client = Minecraft.getInstance();
        return client.getDeltaTracker().getGameTimeDeltaPartialTick(true);
    }

    public static Vec3 camera() {
        return Minecraft.getInstance().gameRenderer.getMainCamera().position();
    }

    private TimerUtil() {
    }

    public static TimerUtil create() {
        return new TimerUtil();
    }

    /** Простая интерполяция значений для анимаций меню и HUD. */
    public static float animate(float current, float target, float speed) {
        return current + (target - current) * speed;
    }
}
