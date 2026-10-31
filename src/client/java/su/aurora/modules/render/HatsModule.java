package su.aurora.modules.render;

import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import su.aurora.core.Category;
import su.aurora.core.Module;
import su.aurora.setting.BooleanSetting;
import su.aurora.setting.ColorSetting;
import su.aurora.setting.ModeSetting;
import su.aurora.setting.NumberSetting;
import su.aurora.util.ColorUtil;
import su.aurora.util.Render3D;

/**
 * Hats — косметические шляпы (12 моделей), как в магазине Never Visuals, только локально и бесплатно.
 */
public class HatsModule extends Module {
    private final ModeSetting hat = addMode("Шляпа", "Цилиндр",
            "Цилиндр", "Корона", "Кошачьи уши", "Нимб", "Колпак", "Рога",
            "Венок", "Бандана", "Наушники", "Рюкзак", "Треуголка", "Маска");
    private final ColorSetting color = addColor("Основной цвет", 0xFF1A1D26);
    private final ColorSetting accent = addColor("Акцент", 0xFF7C5CFF);
    private final NumberSetting size = addNumber("Размер", 1.0, 0.5, 1.8, 0.05);
    private final BooleanSetting allPlayers = addBoolean("Все игроки", false);
    private final BooleanSetting glow = addBoolean("Свечение", true);

    public HatsModule() {
        super("Hats", "Косметические шляпы: цилиндр, корона, уши, нимб и другие", Category.RENDER);
    }

    public ModeSetting hatSetting() {
        return hat;
    }

    public BooleanSetting allPlayersSetting() {
        return allPlayers;
    }

    @Override
    public void onWorldRender(WorldRenderContext context) {
        Minecraft client = Minecraft.getInstance();
        if (client.level == null || client.player == null) {
            return;
        }
        float partial = Render3D.tickDelta();
        if (allPlayers.get()) {
            for (Player player : client.level.players()) {
                renderHat(player, partial);
            }
        } else {
            renderHat(client.player, partial);
        }
    }

    private void renderHat(Player player, float partial) {
        double x = player.getX(partial);
        double y = player.getY(partial) + player.getBbHeight() + 0.02;
        double z = player.getZ(partial);
        Vec3 base = new Vec3(x, y, z);
        float yaw = (float) Math.toRadians(player.getYRot());
        Vec3 right = new Vec3(Math.cos(yaw), 0, Math.sin(yaw));
        Vec3 forward = new Vec3(-Math.sin(yaw), 0, Math.cos(yaw));

        double s = size.get();
        int main = color.get();
        int tint = accent.get();
        float time = System.currentTimeMillis() / 1000f;

        switch (hat.get()) {
            case "Цилиндр" -> {
                Render3D.quads(buffer -> {
                    buffer.ring(base, 0.30 * s, 0.40 * s, ColorUtil.withAlpha(main, 235), 28);
                    buffer.disc(base.add(0, 0.02, 0), 0.30 * s, ColorUtil.withAlpha(main, 235), 28);
                    buffer.disc(base.add(0, 0.42 * s, 0), 0.30 * s, ColorUtil.withAlpha(main, 235), 28);
                });
                Render3D.lines(buffer -> {
                    buffer.circle(base.add(0, 0.02, 0), 0.30 * s, tint, 1.8f, 32);
                    buffer.circle(base.add(0, 0.42 * s, 0), 0.30 * s, tint, 1.8f, 32);
                    buffer.circle(base.add(0, 0.08 * s, 0), 0.30 * s, main, 1.4f, 32);
                    for (int i = 0; i < 8; i++) {
                        double angle = Math.PI * 2 * i / 8.0;
                        double px = base.x + Math.cos(angle) * 0.30 * s;
                        double pz = base.z + Math.sin(angle) * 0.30 * s;
                        buffer.line(px, base.y + 0.02, pz, px, base.y + 0.42 * s, pz, main, 1.2f);
                    }
                });
            }
            case "Корона" -> Render3D.lines(buffer -> {
                double radius = 0.30 * s;
                buffer.circle(base.add(0, 0.06 * s, 0), radius, tint, 2.0f, 32);
                int spikes = 5;
                for (int i = 0; i < spikes; i++) {
                    double angle = Math.PI * 2 * i / spikes - Math.PI / 2;
                    Vec3 bottom = base.add(Math.cos(angle) * radius, 0.06 * s, Math.sin(angle) * radius);
                    Vec3 top = base.add(Math.cos(angle) * radius * 0.55, 0.34 * s, Math.sin(angle) * radius * 0.55);
                    buffer.line(bottom, top, tint, 2.2f);
                    Vec3 next = base.add(Math.cos(angle + Math.PI * 2 / spikes) * radius, 0.06 * s,
                            Math.sin(angle + Math.PI * 2 / spikes) * radius);
                    buffer.line(top, next, ColorUtil.scaleAlpha(tint, 0.6f), 1.2f);
                }
            });
            case "Кошачьи уши" -> Render3D.lines(buffer -> {
                for (int side = -1; side <= 1; side += 2) {
                    Vec3 root = base.add(right.scale(side * 0.17 * s));
                    Vec3 tip = root.add(right.scale(side * 0.05 * s)).add(0, 0.26 * s, 0).subtract(forward.scale(0.02));
                    Vec3 back = root.add(right.scale(side * 0.14 * s)).subtract(forward.scale(0.08));
                    buffer.line(root, tip, tint, 2.2f);
                    buffer.line(root.add(forward.scale(0.08)), tip, tint, 2.2f);
                    buffer.line(tip, back, ColorUtil.scaleAlpha(tint, 0.7f), 1.4f);
                    buffer.line(back, root, ColorUtil.scaleAlpha(tint, 0.7f), 1.4f);
                }
            });
            case "Нимб" -> {
                double bob = Math.sin(time * 1.8) * 0.035;
                Vec3 halo = base.add(0, 0.30 * s + bob, 0);
                Render3D.lines(buffer -> {
                    buffer.circle(halo, 0.26 * s, tint, 2.6f, 40);
                    if (glow.get()) {
                        buffer.circle(halo.add(0, 0.02, 0), 0.30 * s, ColorUtil.scaleAlpha(tint, 0.45f), 1.4f, 40);
                    }
                });
                if (glow.get()) {
                    Render3D.quads(buffer -> buffer.ring(halo, 0.22 * s, 0.30 * s, ColorUtil.withAlpha(tint, 60), 36));
                }
            }
            case "Колпак" -> Render3D.lines(buffer -> {
                Vec3 apex = base.add(Math.sin(time) * 0.03, 0.52 * s, Math.cos(time) * 0.03);
                buffer.circle(base, 0.28 * s, main, 2.0f, 28);
                for (int i = 0; i < 10; i++) {
                    double angle = Math.PI * 2 * i / 10.0;
                    Vec3 rim = base.add(Math.cos(angle) * 0.28 * s, 0, Math.sin(angle) * 0.28 * s);
                    buffer.line(rim, apex, ColorUtil.scaleAlpha(tint, 0.85f), 1.6f);
                }
                buffer.circle(apex, 0.06 * s, tint, 2.0f, 12);
            });
            case "Рога" -> Render3D.lines(buffer -> {
                for (int side = -1; side <= 1; side += 2) {
                    Vec3 root = base.add(right.scale(side * 0.16 * s));
                    Vec3 mid = root.add(right.scale(side * 0.06 * s)).add(0, 0.16 * s, 0);
                    Vec3 tip = mid.add(right.scale(side * 0.10 * s)).add(0, 0.16 * s, 0);
                    buffer.line(root, mid, tint, 2.6f);
                    buffer.line(mid, tip, ColorUtil.scaleAlpha(tint, 0.8f), 2.0f);
                }
            });
            case "Венок" -> {
                Render3D.lines(buffer -> {
                    double radius = 0.29 * s;
                    buffer.circle(base.add(0, 0.05 * s, 0), radius, main, 1.8f, 32);
                    int flowers = 8;
                    for (int i = 0; i < flowers; i++) {
                        double angle = Math.PI * 2 * i / flowers;
                        Vec3 center = base.add(Math.cos(angle) * radius, 0.07 * s, Math.sin(angle) * radius);
                        int flowerColor = (i % 2 == 0) ? tint : 0xFFFF9EC4;
                        buffer.diamond(center, 0.045 * s, flowerColor, 1.6f);
                    }
                });
            }
            case "Бандана" -> {
                Render3D.lines(buffer -> {
                    buffer.circle(base.add(0, 0.10 * s, 0), 0.29 * s, main, 2.4f, 32);
                    buffer.circle(base.add(0, 0.02 * s, 0), 0.29 * s, main, 2.4f, 32);
                    for (int i = 0; i < 6; i++) {
                        double angle = Math.PI * 2 * i / 6.0;
                        double px = base.x + Math.cos(angle) * 0.29 * s;
                        double pz = base.z + Math.sin(angle) * 0.29 * s;
                        buffer.line(px, base.y + 0.02, pz, px, base.y + 0.10 * s, pz, main, 1.2f);
                    }
                    Vec3 knot = base.subtract(forward.scale(0.28 * s)).add(0, 0.06 * s, 0);
                    buffer.line(knot, knot.add(0, -0.18 * s, 0).add(right.scale(0.06 * s)), tint, 2.0f);
                    buffer.line(knot, knot.add(0, -0.14 * s, 0).subtract(right.scale(0.06 * s)), tint, 2.0f);
                });
            }
            case "Наушники" -> {
                Render3D.lines(buffer -> {
                    int segments = 20;
                    Vec3 previous = null;
                    for (int i = 0; i <= segments; i++) {
                        double angle = Math.PI * i / segments;
                        Vec3 point = base.add(right.scale(Math.cos(angle) * 0.30 * s)).add(0, Math.sin(angle) * 0.26 * s, 0);
                        if (previous != null) {
                            buffer.line(previous, point, tint, 2.6f);
                        }
                        previous = point;
                    }
                });
                Render3D.quads(buffer -> {
                    for (int side = -1; side <= 1; side += 2) {
                        Vec3 ear = base.add(right.scale(side * 0.29 * s)).add(0, -0.10 * s, 0);
                        buffer.box(new AABB(ear.x - 0.06 * s, ear.y - 0.07 * s, ear.z - 0.06 * s,
                                ear.x + 0.06 * s, ear.y + 0.07 * s, ear.z + 0.06 * s),
                                ColorUtil.withAlpha(main, 220));
                    }
                });
            }
            case "Рюкзак" -> {
                Vec3 pack = base.subtract(forward.scale(0.30 * s)).add(0, -0.75 * s, 0);
                Render3D.quads(buffer -> buffer.box(new AABB(pack.x - 0.22 * s, pack.y - 0.28 * s, pack.z - 0.16 * s,
                        pack.x + 0.22 * s, pack.y + 0.28 * s, pack.z + 0.16 * s), ColorUtil.withAlpha(main, 225)));
                Render3D.lines(buffer -> {
                    buffer.box(new AABB(pack.x - 0.22 * s, pack.y - 0.28 * s, pack.z - 0.16 * s,
                            pack.x + 0.22 * s, pack.y + 0.28 * s, pack.z + 0.16 * s), tint, 1.8f);
                    buffer.line(pack.x, pack.y - 0.10 * s, pack.z + 0.16 * s,
                            pack.x, pack.y + 0.16 * s, pack.z + 0.16 * s, tint, 2.2f);
                });
            }
            case "Треуголка" -> Render3D.lines(buffer -> {
                buffer.circle(base.add(0, 0.04 * s, 0), 0.34 * s, main, 2.0f, 28);
                Vec3 back = base.subtract(forward.scale(0.30 * s)).add(0, 0.26 * s, 0);
                Vec3 leftPoint = base.add(right.scale(-0.32 * s)).add(0, 0.18 * s, 0);
                Vec3 rightPoint = base.add(right.scale(0.32 * s)).add(0, 0.18 * s, 0);
                Vec3 front = base.add(forward.scale(0.26 * s)).add(0, 0.16 * s, 0);
                buffer.line(leftPoint, back, tint, 2.4f);
                buffer.line(back, rightPoint, tint, 2.4f);
                buffer.line(leftPoint, front, ColorUtil.scaleAlpha(tint, 0.7f), 1.6f);
                buffer.line(front, rightPoint, ColorUtil.scaleAlpha(tint, 0.7f), 1.6f);
                buffer.line(front, back, ColorUtil.scaleAlpha(tint, 0.5f), 1.2f);
            });
            default -> {
                Vec3 mask = base.add(forward.scale(0.13 * s)).add(0, -0.22 * s, 0);
                Render3D.quads(buffer -> buffer.plane(
                        mask.add(right.scale(-0.20 * s)).add(0, 0.12 * s, 0),
                        right.scale(0.40 * s), new Vec3(0, -0.24 * s, 0), ColorUtil.withAlpha(main, 200)));
                Render3D.lines(buffer -> {
                    Vec3 tl = mask.add(right.scale(-0.20 * s)).add(0, 0.12 * s, 0);
                    Vec3 tr = mask.add(right.scale(0.20 * s)).add(0, 0.12 * s, 0);
                    Vec3 bl = mask.add(right.scale(-0.20 * s)).add(0, -0.12 * s, 0);
                    Vec3 br = mask.add(right.scale(0.20 * s)).add(0, -0.12 * s, 0);
                    buffer.line(tl, tr, tint, 1.8f);
                    buffer.line(tr, br, tint, 1.8f);
                    buffer.line(br, bl, tint, 1.8f);
                    buffer.line(bl, tl, tint, 1.8f);
                    buffer.circle(mask.add(right.scale(-0.08 * s)).add(0, 0.03 * s, 0), 0.035 * s, tint, 1.6f, 10);
                    buffer.circle(mask.add(right.scale(0.08 * s)).add(0, 0.03 * s, 0), 0.035 * s, tint, 1.6f, 10);
                });
            }
        }
    }
}
