package su.aurora.ui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import su.aurora.AuroraClient;
import su.aurora.core.Module;
import su.aurora.modules.render.HatsModule;
import su.aurora.util.ColorUtil;
import su.aurora.util.Render2D;

import java.util.List;

/**
 * Экран «Косметика»: сетка шляп с превью, выбор сохраняется в конфиг.
 * Открывается кнопкой в заголовке ClickGUI.
 */
public class CosmeticScreen extends Screen {
    private static final List<String> HATS = List.of(
            "Цилиндр", "Корона", "Кошачьи уши", "Нимб",
            "Колпак", "Рога", "Венок", "Бандана",
            "Наушники", "Рюкзак", "Треуголка", "Маска");

    private static final int ACCENT = 0xFF7C5CFF;
    private static final int BACKGROUND = 0xF012141C;
    private static final int PANEL = 0xFF1A1D26;
    private static final int PANEL_LIGHT = 0xFF232733;
    private static final int TEXT_PRIMARY = 0xFFFFFFFF;
    private static final int TEXT_SECONDARY = 0xFF8A90A6;

    private final Screen parent;
    private int panelX;
    private int panelY;
    private int panelWidth;
    private int panelHeight;
    private int hovered = -1;

    public CosmeticScreen(Screen parent) {
        super(Component.literal("Косметика"));
        this.parent = parent;
    }

    private HatsModule hats() {
        if (AuroraClient.MODULES == null) {
            return null;
        }
        Module module = AuroraClient.MODULES.get("Hats");
        return module instanceof HatsModule hatsModule ? hatsModule : null;
    }

    private void layout() {
        panelWidth = Math.min(520, width - 40);
        int rows = (HATS.size() + 3) / 4;
        panelHeight = Math.min(height - 40, 76 + rows * 96);
        panelX = (width - panelWidth) / 2;
        panelY = (height - panelHeight) / 2;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        layout();
        graphics.fill(0, 0, width, height, 0x88000000);
        Render2D.roundedRect(graphics, panelX, panelY, panelWidth, panelHeight, 12, BACKGROUND);
        Render2D.roundedRect(graphics, panelX, panelY, panelWidth, 34, 12, PANEL);
        graphics.fill(panelX, panelY + 33, panelX + panelWidth, panelY + 34, ColorUtil.withAlpha(ACCENT, 70));

        Render2D.roundedRect(graphics, panelX + 12, panelY + 10, 6, 14, 3, ACCENT);
        Render2D.textShadow(graphics, "КОСМЕТИКА", panelX + 24, panelY + 11, TEXT_PRIMARY);
        Render2D.textRight(graphics, "12 моделей", panelX + panelWidth - 16, panelY + 11, TEXT_SECONDARY);

        HatsModule hats = hats();
        String worn = hats != null ? hats.hatSetting().get() : "—";
        boolean enabled = hats != null && hats.isEnabled();
        if (hats != null && hats.hatSetting() != null) {
            HatsModule module = hats;
            if (this.selectedIndex(module) >= 0) {
                worn = module.hatSetting().get();
            }
        }

        int cellWidth = (panelWidth - 32 - 24) / 4;
        int cellHeight = 88;
        hovered = -1;
        for (int i = 0; i < HATS.size(); i++) {
            int column = i % 4;
            int row = i / 4;
            int x = panelX + 16 + column * (cellWidth + 8);
            int y = panelY + 44 + row * (cellHeight + 8);
            String hat = HATS.get(i);
            boolean active = enabled && hat.equals(worn);
            boolean isHover = Render2D.hovered(mouseX, mouseY, x, y, cellWidth, cellHeight);
            if (isHover) {
                hovered = i;
            }
            Render2D.roundedRect(graphics, x, y, cellWidth, cellHeight, 9,
                    active ? ColorUtil.withAlpha(ACCENT, 46)
                            : isHover ? ColorUtil.withAlpha(PANEL_LIGHT, 210) : PANEL);
            if (active) {
                Render2D.roundedOutline(graphics, x, y, cellWidth, cellHeight, 9, ACCENT, 1.5f);
            }
            drawPreview(graphics, hat, x + cellWidth / 2, y + 30, active ? ACCENT : ColorUtil.withAlpha(ACCENT, 180));
            Render2D.textCentered(graphics, hat, x + cellWidth / 2f, y + cellHeight - 20,
                    active ? ACCENT : TEXT_PRIMARY);
            Render2D.textCentered(graphics, active ? "надето" : "надеть", x + cellWidth / 2f, y + cellHeight - 10,
                    active ? ColorUtil.withAlpha(ACCENT, 190) : ColorUtil.withAlpha(TEXT_SECONDARY, 170));
        }

        Render2D.textCentered(graphics, "ЛКМ — надеть · ПКМ — снять · ESC — назад",
                panelX + panelWidth / 2f, panelY + panelHeight - 14, ColorUtil.withAlpha(TEXT_SECONDARY, 200));
    }

    private int selectedIndex(HatsModule hats) {
        return HATS.indexOf(hats.hatSetting().get());
    }

    /** Мини-превью модели шляпы: несколько фигур, без текстур. */
    private void drawPreview(GuiGraphics graphics, String hat, int cx, int cy, int accent) {
        int base = 0xFF2C3142;
        switch (hat) {
            case "Цилиндр" -> {
                graphics.fill(cx - 16, cy + 8, cx + 16, cy + 11, base);
                graphics.fill(cx - 9, cy - 14, cx + 9, cy + 8, base);
                graphics.fill(cx - 9, cy + 2, cx + 9, cy + 4, accent);
            }
            case "Корона" -> {
                graphics.fill(cx - 14, cy + 4, cx + 14, cy + 10, base);
                for (int i = -1; i <= 1; i++) {
                    graphics.fill(cx + i * 11 - 2, cy - 8, cx + i * 11 + 2, cy + 4, accent);
                }
            }
            case "Кошачьи уши" -> {
                graphics.fill(cx - 14, cy + 4, cx + 14, cy + 9, base);
                graphics.fill(cx - 14, cy - 8, cx - 6, cy + 4, accent);
                graphics.fill(cx + 6, cy - 8, cx + 14, cy + 4, accent);
            }
            case "Нимб" -> {
                for (int i = 0; i < 14; i++) {
                    double angle = Math.PI * 2 * i / 14.0;
                    int px = cx + (int) (Math.cos(angle) * 15);
                    int py = cy + (int) (Math.sin(angle) * 6);
                    graphics.fill(px - 1, py - 1, px + 2, py + 2, accent);
                }
            }
            case "Колпак" -> {
                graphics.fill(cx - 13, cy + 6, cx + 13, cy + 10, base);
                for (int i = 0; i < 10; i++) {
                    graphics.fill(cx - 13 + i * 2, cy + 6 - i * 2, cx + 13 - i * 2, cy + 8 - i * 2, accent);
                }
            }
            case "Рога" -> {
                graphics.fill(cx - 12, cy + 2, cx + 12, cy + 8, base);
                graphics.fill(cx - 16, cy - 8, cx - 10, cy + 3, accent);
                graphics.fill(cx + 10, cy - 8, cx + 16, cy + 3, accent);
            }
            case "Венок" -> {
                for (int i = 0; i < 10; i++) {
                    double angle = Math.PI * 2 * i / 10.0;
                    int px = cx + (int) (Math.cos(angle) * 14);
                    int py = cy + (int) (Math.sin(angle) * 7);
                    graphics.fill(px - 2, py - 2, px + 3, py + 3, i % 2 == 0 ? accent : 0xFFFF9EC4);
                }
            }
            case "Бандана" -> {
                graphics.fill(cx - 15, cy - 2, cx + 15, cy + 4, accent);
                graphics.fill(cx - 3, cy + 4, cx + 3, cy + 14, base);
            }
            case "Наушники" -> {
                graphics.fill(cx - 15, cy - 6, cx + 15, cy - 2, accent);
                graphics.fill(cx - 18, cy - 4, cx - 12, cy + 8, base);
                graphics.fill(cx + 12, cy - 4, cx + 18, cy + 8, base);
            }
            case "Рюкзак" -> {
                graphics.fill(cx - 11, cy - 6, cx + 11, cy + 12, base);
                graphics.fill(cx - 7, cy - 1, cx + 7, cy + 3, accent);
                graphics.fill(cx - 2, cy + 3, cx + 2, cy + 8, accent);
            }
            case "Треуголка" -> {
                graphics.fill(cx - 17, cy + 2, cx + 17, cy + 6, base);
                graphics.fill(cx - 12, cy - 7, cx + 12, cy + 2, accent);
                graphics.fill(cx - 14, cy - 3, cx - 8, cy + 2, accent);
                graphics.fill(cx + 8, cy - 3, cx + 14, cy + 2, accent);
            }
            default -> {
                graphics.fill(cx - 13, cy - 8, cx + 13, cy + 10, base);
                graphics.fill(cx - 8, cy - 2, cx - 4, cy + 1, accent);
                graphics.fill(cx + 4, cy - 2, cx + 8, cy + 1, accent);
                graphics.fill(cx - 6, cy + 5, cx + 6, cy + 7, accent);
            }
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        layout();
        int mouseX = (int) event.x();
        int mouseY = (int) event.y();
        int button = event.button();
        int cellWidth = (panelWidth - 32 - 24) / 4;
        int cellHeight = 88;
        HatsModule hats = hats();
        if (hats == null) {
            return super.mouseClicked(event, doubleClick);
        }
        for (int i = 0; i < HATS.size(); i++) {
            int column = i % 4;
            int row = i / 4;
            int x = panelX + 16 + column * (cellWidth + 8);
            int y = panelY + 44 + row * (cellHeight + 8);
            if (!Render2D.hovered(mouseX, mouseY, x, y, cellWidth, cellHeight)) {
                continue;
            }
            if (button == 0) {
                hats.hatSetting().set(HATS.get(i));
                hats.setEnabled(true);
            } else if (button == 1) {
                hats.setEnabled(false);
            }
            AuroraClient.CONFIG.markDirty();
            return true;
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public void onClose() {
        Minecraft.getInstance().setScreen(parent);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
