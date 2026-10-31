package su.aurora.modules.render;

import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.minecraft.client.Minecraft;
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
import java.util.Random;

/**
 * Seasons — сезонный визуал вокруг игрока: лепестки весной, светлячки летом,
 * листья осенью, снег зимой. Частицы свои, ванильные не трогаются.
 */
public class SeasonsModule extends Module {
    private final ModeSetting season = addMode("Сезон", "Авто", "Авто", "Весна", "Лето", "Осень", "Зима");
    private final NumberSetting amount = addNumber("Количество", 60, 5, 300, 5);
    private final NumberSetting radius = addNumber("Радиус", 12, 3, 40, 1);
    private final NumberSetting speed = addNumber("Скорость", 1.0, 0.2, 3.0, 0.1);
    private final NumberSetting size = addNumber("Размер", 0.07, 0.02, 0.3, 0.01);
    private final BooleanSetting glow = addBoolean("Свечение", true);
    private final ColorSetting color = addColor("Цвет", 0xFFFFFFFF);

    private final List<Flake> flakes = new ArrayList<>();
    private final Random random = new Random();

    private record Flake(Vec3 pos, Vec3 velocity, int color, float life, long created) {
    }

    public SeasonsModule() {
        super("Seasons", "Сезонный визуал: лепестки, светлячки, листья, снег", Category.RENDER);
    }

    @Override
    public void onDisable() {
        flakes.clear();
    }

    private String activeSeason() {
        if (!season.is("Авто")) {
            return season.get();
        }
        Minecraft client = Minecraft.getInstance();
        if (client.level != null && client.player != null) {
            String path = client.level.getBiome(client.player.blockPosition()).unwrapKey()
                    .map(key -> key.identifier().getPath()).orElse("").toLowerCase(java.util.Locale.ROOT);
            if (path.contains("snow") || path.contains("frozen") || path.contains("ice") || path.contains("grove")) {
                return "Зима";
            }
            if (path.contains("desert") || path.contains("badlands") || path.contains("savanna")) {
                return "Лето";
            }
            if (path.contains("forest") || path.contains("jungle") || path.contains("taiga")) {
                return "Осень";
            }
        }
        return "Весна";
    }

    private int colorFor(String seasonName) {
        return switch (seasonName) {
            case "Весна" -> 0xFFFF9EC4;
            case "Лето" -> 0xFFFFE98A;
            case "Осень" -> 0xFFFFA85C;
            default -> 0xFFFFFFFF;
        };
    }

    @Override
    public void onTick() {
        Minecraft client = Minecraft.getInstance();
        if (client.level == null || client.player == null) {
            return;
        }
        String seasonName = activeSeason();
        int tint = color.get();
        int baseColor = (tint & 0x00FFFFFF) == 0x00FFFFFF ? colorFor(seasonName) : tint;
        Vec3 center = client.player.position();
        double r = radius.get();
        int target = amount.getInt();

        while (flakes.size() < target) {
            double angle = random.nextDouble() * Math.PI * 2;
            double dist = random.nextDouble() * r;
            Vec3 pos = center.add(Math.cos(angle) * dist, random.nextDouble() * 6 + 1, Math.sin(angle) * dist);
            Vec3 velocity = switch (seasonName) {
                case "Зима" -> new Vec3(0.005 - random.nextDouble() * 0.01, -0.035, 0.005 - random.nextDouble() * 0.01);
                case "Осень" -> new Vec3(0.02 - random.nextDouble() * 0.04, -0.025, 0.02 - random.nextDouble() * 0.04);
                case "Лето" -> new Vec3(0.01 - random.nextDouble() * 0.02, 0.012, 0.01 - random.nextDouble() * 0.02);
                default -> new Vec3(0.015 - random.nextDouble() * 0.03, -0.02, 0.015 - random.nextDouble() * 0.03);
            };
            float life = seasonName.equals("Зима") ? 8f : 6f;
            flakes.add(new Flake(pos, velocity.scale(speed.get()), baseColor, life, System.currentTimeMillis()));
        }

        long lifeMs = 8000;
        for (Iterator<Flake> it = flakes.iterator(); it.hasNext(); ) {
            Flake flake = it.next();
            boolean tooFar = flake.pos().distanceToSqr(center) > (r + 6) * (r + 6);
            boolean expired = System.currentTimeMillis() - flake.created() > lifeMs;
            if (tooFar || expired) {
                it.remove();
            }
        }
        // обновляем позиции
        List<Flake> updated = new ArrayList<>(flakes.size());
        for (Flake flake : flakes) {
            Vec3 next = flake.pos().add(flake.velocity());
            if (client.level.getBlockState(net.minecraft.core.BlockPos.containing(next)).isAir()) {
                updated.add(new Flake(next, flake.velocity(), flake.color(), flake.life(), flake.created()));
            }
        }
        flakes.clear();
        flakes.addAll(updated);
    }

    @Override
    public void onWorldRender(WorldRenderContext context) {
        if (flakes.isEmpty()) {
            return;
        }
        float s = size.getFloat();
        boolean additive = glow.get();
        for (Flake flake : flakes) {
            Vec3 pos = flake.pos();
            int tint = ColorUtil.withAlpha(flake.color(), additive ? 190 : 150);
            Render3D.quads(buffer -> {
                Vec3 a = pos.add(-s, 0, -s);
                Vec3 b = pos.add(s, 0, -s);
                Vec3 c = pos.add(s, 0, s);
                Vec3 d = pos.add(-s, 0, s);
                buffer.quad(a, b, c, d, tint);
                Vec3 t = pos.add(0, s * 1.5, 0);
                buffer.quad(a.add(0, s * 1.5, 0), b.add(0, s * 1.5, 0), t, t, ColorUtil.withAlpha(tint, tint >>> 24));
            });
        }
    }
}
