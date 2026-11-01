package su.aurora.modules.hud;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import su.aurora.Aurora;
import su.aurora.AuroraClient;
import su.aurora.core.Category;
import su.aurora.core.Module;
import su.aurora.setting.BooleanSetting;
import su.aurora.setting.ColorSetting;
import su.aurora.setting.ModeSetting;
import su.aurora.setting.NumberSetting;
import su.aurora.setting.StringSetting;
import su.aurora.util.ColorUtil;
import su.aurora.util.Render2D;

/**
 * Minimap — настоящая карта блоков вокруг: цвет берётся из MapColor верхнего блока,
 * картинка собирается в NativeImage и обновляется ~2 раза в секунду.
 */
public class MinimapModule extends Module {
    private final ModeSetting position = addMode("Позиция", "Слева сверху", "Слева сверху", "Справа сверху",
            "Слева снизу", "Справа снизу");
    private final NumberSetting size = addNumber("Размер", 96, 48, 200, 8);
    private final NumberSetting scale = addNumber("Блоков на пиксель", 2, 1, 8, 1);
    private final BooleanSetting players = addBoolean("Игроки", true);
    private final BooleanSetting waypoints = addBoolean("Вейпоинты", true);
    private final BooleanSetting frame = addBoolean("Рамка", true);
    private final BooleanSetting shade = addBoolean("Рельеф", true);
    private final ColorSetting accent = addColor("Акцент", 0xFF7C5CFF);

    private NativeImage image;
    private DynamicTexture texture;
    private Identifier textureId;
    private int textureSize;
    private long lastUpdate;
    private double centerX;
    private double centerZ;

    public MinimapModule() {
        super("Minimap", "Карта блоков вокруг игрока", Category.HUD);
    }

    private void ensureTexture(int px) {
        if (image != null && textureSize == px) {
            return;
        }
        Minecraft client = Minecraft.getInstance();
        if (textureId != null) {
            client.getTextureManager().release(textureId);
        }
        image = new NativeImage(NativeImage.Format.RGBA, px, px, false);
        texture = new DynamicTexture(() -> "aurora_minimap", image);
        textureId = Aurora.id("minimap");
        client.getTextureManager().register(textureId, texture);
        textureSize = px;
        lastUpdate = 0;
    }

    private void render(ClientLevel level, double playerX, double playerZ, int px, int blockScale) {
        ensureTexture(px);
        centerX = playerX;
        centerZ = playerZ;
        int half = px / 2;
        int seaLevel = 63;
        for (int py = 0; py < px; py++) {
            for (int pxi = 0; pxi < px; pxi++) {
                int worldX = (int) Math.floor(playerX) + (pxi - half) * blockScale;
                int worldZ = (int) Math.floor(playerZ) + (py - half) * blockScale;
                int topY;
                try {
                    topY = level.getHeight(Heightmap.Types.WORLD_SURFACE, worldX, worldZ);
                } catch (RuntimeException exception) {
                    continue;
                }
                BlockPos pos = new BlockPos(worldX, Math.max(level.getMinY(), topY - 1), worldZ);
                BlockState state = level.getBlockState(pos);
                int color;
                try {
                    color = state.getMapColor(level, pos).col;
                } catch (RuntimeException exception) {
                    color = 0x808080;
                }
                int rgb = color & 0xFFFFFF;
                if (shade.get()) {
                    float factor = 0.72f + Math.min(0.55f, Math.max(-0.3f, (topY - seaLevel) / 90f));
                    int r = (int) Math.min(255, ((rgb >> 16) & 0xFF) * factor);
                    int g = (int) Math.min(255, ((rgb >> 8) & 0xFF) * factor);
                    int b = (int) Math.min(255, (rgb & 0xFF) * factor);
                    rgb = (r << 16) | (g << 8) | b;
                }
                image.setPixel(pxi, py, 0xFF000000 | rgb);
            }
        }
        texture.upload();
    }

    @Override
    public void onHudRender(GuiGraphics graphics, DeltaTracker delta) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null || client.level == null || client.options.hideGui) {
            return;
        }
        int px = size.getInt();
        int blockScale = Math.max(1, scale.getInt());
        long now = System.currentTimeMillis();
        if (now - lastUpdate > 500) {
            lastUpdate = now;
            render(client.level, client.player.getX(), client.player.getZ(), px, blockScale);
        }
        if (textureId == null) {
            return;
        }

        int screenWidth = client.getWindow().getGuiScaledWidth();
        int screenHeight = client.getWindow().getGuiScaledHeight();
        int x = switch (position.get()) {
            case "Справа сверху", "Справа снизу" -> screenWidth - px - 8;
            default -> 8;
        };
        int y = switch (position.get()) {
            case "Слева снизу", "Справа снизу" -> screenHeight - px - 8;
            default -> 8;
        };

        graphics.blit(RenderPipelines.GUI_TEXTURED, textureId, x, y, 0f, 0f, px, px, px, px);
        if (frame.get()) {
            Render2D.roundedOutline(graphics, x - 1, y - 1, px + 2, px + 2, 4,
                    ColorUtil.withAlpha(accent.get(), 160), 1.0f);
        }

        double half = (px - 1) / 2.0;
        double scaleFactor = blockScale;
        if (players.get()) {
            for (Entity entity : client.level.entitiesForRendering()) {
                if (!(entity instanceof Player other) || other == client.player) {
                    continue;
                }
                double dx = (other.getX() - centerX) / scaleFactor;
                double dz = (other.getZ() - centerZ) / scaleFactor;
                if (Math.abs(dx) > half || Math.abs(dz) > half) {
                    continue;
                }
                int dotX = x + (int) Math.round(px / 2.0 + dx);
                int dotY = y + (int) Math.round(px / 2.0 + dz);
                graphics.fill(dotX - 1, dotY - 1, dotX + 2, dotY + 2, 0xFFFF5C7C);
            }
        }

        if (waypoints.get() && AuroraClient.MODULES != null) {
            Module module = AuroraClient.MODULES.get("Waypoints");
            if (module != null && module.getSetting("Точки") instanceof StringSetting points) {
                String raw = points.get();
                if (raw != null && !raw.isBlank()) {
                    for (String part : raw.split(";")) {
                        String[] split = part.trim().split(":");
                        if (split.length < 4) {
                            continue;
                        }
                        try {
                            double wx = Double.parseDouble(split[1]);
                            double wz = Double.parseDouble(split[3]);
                            double dx = (wx - centerX) / scaleFactor;
                            double dz = (wz - centerZ) / scaleFactor;
                            if (Math.abs(dx) > half || Math.abs(dz) > half) {
                                continue;
                            }
                            int dotX = x + (int) Math.round(px / 2.0 + dx);
                            int dotY = y + (int) Math.round(px / 2.0 + dz);
                            graphics.fill(dotX - 2, dotY - 2, dotX + 3, dotY + 3, accent.get());
                        } catch (NumberFormatException ignored) {
                        }
                    }
                }
            }
        }

        // стрелка игрока в центре
        int centerPx = x + px / 2;
        int centerPy = y + px / 2;
        double yaw = Math.toRadians(client.player.getYRot());
        int arrowX = centerPx + (int) (-Math.sin(yaw) * 5);
        int arrowY = centerPy + (int) (Math.cos(yaw) * 5);
        graphics.fill(centerPx - 1, centerPy - 1, centerPx + 2, centerPy + 2, 0xFFFFFFFF);
        graphics.fill(arrowX - 1, arrowY - 1, arrowX + 2, arrowY + 2, accent.get());
    }

    @Override
    public void onDisable() {
        if (textureId != null) {
            Minecraft.getInstance().getTextureManager().release(textureId);
            textureId = null;
            texture = null;
            image = null;
            textureSize = 0;
        }
    }
}
