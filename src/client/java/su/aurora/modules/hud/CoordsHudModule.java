package su.aurora.modules.hud;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import su.aurora.core.Category;
import su.aurora.core.Module;
import su.aurora.setting.BooleanSetting;
import su.aurora.setting.ColorSetting;
import su.aurora.setting.ModeSetting;
import su.aurora.util.Render2D;

import java.util.Locale;
import java.util.Optional;

/**
 * CoordsHUD — координаты, биом, измерение и направление взгляда.
 */
public class CoordsHudModule extends Module {
    private final ModeSetting position = addMode("Позиция", "Справа снизу", "Справа снизу", "Слева снизу", "Справа сверху");
    private final BooleanSetting showBiome = addBoolean("Биом", true);
    private final BooleanSetting showDimension = addBoolean("Измерение", true);
    private final BooleanSetting showFacing = addBoolean("Направление", true);
    private final BooleanSetting showNether = addBoolean("Координаты Нижнего мира", true);
    private final ColorSetting accent = addColor("Акцент", 0xFF7C5CFF);

    public CoordsHudModule() {
        super("CoordsHUD", "Координаты, биом и направление", Category.HUD);
    }

    @Override
    public void onHudRender(GuiGraphics graphics, DeltaTracker delta) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null || client.level == null || client.options.hideGui) {
            return;
        }

        double px = client.player.getX();
        double py = client.player.getY();
        double pz = client.player.getZ();
        boolean nether = client.level.dimension().identifier().getPath().contains("nether");

        String coords = String.format(Locale.ROOT, "X %.0f   Y %.0f   Z %.0f", px, py, pz);
        String under = String.format(Locale.ROOT, "Overworld: %.0f, %.0f", nether ? px * 8 : px / 8,
                nether ? pz * 8 : pz / 8);

        String biome = "—";
        String dimension = "—";
        if (showBiome.get()) {
            Optional<Identifier> key = client.level.getBiome(BlockPos.containing(px, py, pz))
                    .unwrapKey()
                    .map(resourceKey -> resourceKey.identifier());
            biome = key.map(id -> pretty(id.getPath())).orElse("—");
        }
        if (showDimension.get()) {
            dimension = pretty(client.level.dimension().identifier().getPath());
        }

        String facing = "";
        if (showFacing.get()) {
            facing = direction(client.player.getYRot());
        }

        int screenWidth = client.getWindow().getGuiScaledWidth();
        int screenHeight = client.getWindow().getGuiScaledHeight();
        float width = 132;
        float height = 34 + (showBiome.get() ? 10 : 0) + (showDimension.get() ? 10 : 0)
                + (nether && showNether.get() ? 10 : 0);
        float x = position.is("Слева снизу") ? 8 : screenWidth - width - 8;
        float y = position.is("Справа сверху") ? 34 : screenHeight - height - 30;

        Render2D.roundedRect(graphics, x, y, width, height, 6, 0x8C0C0E14);
        Render2D.roundedRect(graphics, x, y, 3, height, 3, accent.get());
        Render2D.textShadow(graphics, coords, x + 9, y + 5, 0xFFFFFFFF);

        float row = y + 16;
        if (showFacing.get()) {
            Render2D.textShadow(graphics, facing, x + 9, row, accent.get());
            Render2D.textRight(graphics, String.format(Locale.ROOT, "%.0f°", client.player.getYRot()),
                    x + width - 6, row, 0x99FFFFFF);
            row += 10;
        }
        if (showBiome.get()) {
            Render2D.textShadow(graphics, biome, x + 9, row, 0xB3FFFFFF);
            row += 10;
        }
        if (showDimension.get()) {
            Render2D.textShadow(graphics, dimension, x + 9, row, 0xB3FFFFFF);
            row += 10;
        }
        if (nether && showNether.get()) {
            Render2D.textShadow(graphics, under, x + 9, row, 0x80FFFFFF);
        }
    }

    private String direction(float yaw) {
        float normalized = ((yaw % 360) + 360) % 360;
        if (normalized < 45 || normalized >= 315) {
            return "Юг (Z+)";
        }
        if (normalized < 135) {
            return "Запад (X−)";
        }
        if (normalized < 225) {
            return "Север (Z−)";
        }
        return "Восток (X+)";
    }

    private String pretty(String path) {
        String cleaned = path.replace('_', ' ').replace('/', ' ');
        if (cleaned.isEmpty()) {
            return "—";
        }
        return Character.toUpperCase(cleaned.charAt(0)) + cleaned.substring(1);
    }
}
