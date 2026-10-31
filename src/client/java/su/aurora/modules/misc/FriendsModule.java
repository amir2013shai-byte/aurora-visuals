package su.aurora.modules.misc;

import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import su.aurora.core.Category;
import su.aurora.core.Module;
import su.aurora.setting.BooleanSetting;
import su.aurora.setting.ColorSetting;
import su.aurora.setting.NumberSetting;
import su.aurora.setting.StringSetting;
import su.aurora.util.ColorUtil;
import su.aurora.util.Render3D;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * Friends — список друзей: зелёная рамка и подпись, плюс статический хелпер
 * isFriend() для остальных модулей (Tracers, NameTags и т.д.).
 */
public class FriendsModule extends Module {
    private final StringSetting names = addString("Друзья", "");
    private final ColorSetting color = addColor("Цвет", 0xFF7CF7B0);
    private final BooleanSetting box = addBoolean("Рамка", true);
    private final BooleanSetting fill = addBoolean("Заливка", false);
    private final BooleanSetting marker = addBoolean("Метка над головой", true);
    private final NumberSetting range = addNumber("Дистанция", 64, 8, 256, 4);

    private static String friendList = "";

    public FriendsModule() {
        super("Friends", "Список друзей: отдельная подсветка и метка", Category.MISC);
        friendList = "";
    }

    /** Ник в списке друзей (без учёта регистра). */
    public static boolean isFriend(String name) {
        if (name == null || friendList.isBlank()) {
            return false;
        }
        String target = name.toLowerCase(Locale.ROOT);
        for (String friend : friendList.split("[,;\\s]+")) {
            if (!friend.isBlank() && friend.toLowerCase(Locale.ROOT).equals(target)) {
                return true;
            }
        }
        return false;
    }

    private Set<String> names() {
        Set<String> result = new HashSet<>();
        String raw = names.get();
        if (raw == null) {
            return result;
        }
        for (String friend : raw.split("[,;\\s]+")) {
            if (!friend.isBlank()) {
                result.add(friend.toLowerCase(Locale.ROOT));
            }
        }
        return result;
    }

    @Override
    public void onTick() {
        friendList = names.get() == null ? "" : names.get();
    }

    @Override
    public void onWorldRender(WorldRenderContext context) {
        Set<String> list = names();
        if (list.isEmpty()) {
            return;
        }
        Minecraft client = Minecraft.getInstance();
        if (client.level == null || client.player == null) {
            return;
        }
        float partial = Render3D.tickDelta();
        double rangeSq = range.get() * range.get();
        int tint = color.get();

        for (Entity entity : client.level.entitiesForRendering()) {
            if (!(entity instanceof Player player) || player == client.player) {
                continue;
            }
            if (!list.contains(player.getGameProfile().name().toLowerCase(Locale.ROOT))) {
                continue;
            }
            if (player.distanceToSqr(client.player) > rangeSq) {
                continue;
            }
            AABB bb = player.getBoundingBox().inflate(0.1)
                    .move(player.getX(partial) - player.getX(), player.getY(partial) - player.getY(),
                            player.getZ(partial) - player.getZ());
            if (box.get()) {
                Render3D.lines(buffer -> buffer.box(bb, tint, 2.0f));
            }
            if (fill.get()) {
                Render3D.quads(buffer -> buffer.box(bb, ColorUtil.withAlpha(tint, 30)));
            }
            if (marker.get()) {
                double y = bb.maxY + 0.35;
                Render3D.lines(buffer -> {
                    buffer.diamond(new net.minecraft.world.phys.Vec3(player.getX(partial), y, player.getZ(partial)),
                            0.18, tint, 1.8f);
                });
            }
        }
    }
}
