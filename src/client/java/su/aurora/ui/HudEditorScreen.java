package su.aurora.ui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import su.aurora.AuroraClient;
import su.aurora.core.Category;
import su.aurora.core.Module;
import su.aurora.util.ColorUtil;
import su.aurora.util.Render2D;

import java.util.List;

/**
 * Редактор HUD: тянем строку мышью — элемент HUD на экране смещается.
 * Смещения хранятся в {@link HudOffsets} и подгружаются при старте.
 */
public class HudEditorScreen extends Screen {
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
    private int rowHeight = 24;
    private String dragging;
    private int scroll;

    public HudEditorScreen(Screen parent) {
        super(Component.literal("Редактор HUD"));
        this.parent = parent;
        HudOffsets.ensureLoaded();
    }

    private List<Module> modules() {
        if (AuroraClient.MODULES == null) {
            return List.of();
        }
        return AuroraClient.MODULES.inCategory(Category.HUD);
    }

    private void layout() {
        panelWidth = Math.min(360, width - 40);
        panelHeight = Math.min(height - 40, 400);
        panelX = (width - panelWidth) / 2;
        panelY = (height - panelHeight) / 2;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        layout();
        graphics.fill(0, 0, width, height, 0x66000000);
        Render2D.roundedRect(graphics, panelX, panelY, panelWidth, panelHeight, 12, BACKGROUND);
        Render2D.roundedRect(graphics, panelX, panelY, panelWidth, 34, 12, PANEL);
        graphics.fill(panelX, panelY + 33, panelX + panelWidth, panelY + 34, ColorUtil.withAlpha(ACCENT, 70));
        Render2D.roundedRect(graphics, panelX + 12, panelY + 10, 6, 14, 3, ACCENT);
        Render2D.textShadow(graphics, "РЕДАКТОР HUD", panelX + 24, panelY + 11, TEXT_PRIMARY);
        Render2D.textRight(graphics, "ЛКМ — тянуть · ПКМ — сброс", panelX + panelWidth - 14, panelY + 11,
                TEXT_SECONDARY);

        List<Module> list = modules();
        int listTop = panelY + 42;
        int visible = (panelHeight - 70) / rowHeight;
        int end = Math.min(list.size(), scroll + visible);
        for (int i = scroll; i < end; i++) {
            Module module = list.get(i);
            int y = listTop + (i - scroll) * rowHeight;
            float[] offset = HudOffsets.get(module.getName());
            boolean hover = Render2D.hovered(mouseX, mouseY, panelX + 10, y, panelWidth - 20, rowHeight - 4);
            boolean active = module.isEnabled();
            Render2D.roundedRect(graphics, panelX + 10, y, panelWidth - 20, rowHeight - 4, 7,
                    hover ? ColorUtil.withAlpha(PANEL_LIGHT, 220) : PANEL);
            Render2D.roundedRect(graphics, panelX + 14, y + 6, 4, rowHeight - 16, 2,
                    active ? ACCENT : ColorUtil.withAlpha(TEXT_SECONDARY, 120));
            Render2D.textShadow(graphics, module.getName(), panelX + 24, y + 7,
                    active ? TEXT_PRIMARY : ColorUtil.withAlpha(TEXT_PRIMARY, 140));
            String value = String.format(java.util.Locale.ROOT, "%+.0f / %+.0f", offset[0], offset[1]);
            Render2D.textRight(graphics, value, panelX + panelWidth - 16, y + 7,
                    HudOffsets.isMoved(module.getName()) ? ACCENT : ColorUtil.withAlpha(TEXT_SECONDARY, 180));
        }
        if (list.isEmpty()) {
            Render2D.textCentered(graphics, "HUD-модулей нет", panelX + panelWidth / 2f, listTop + 12, TEXT_SECONDARY);
        }
        Render2D.textCentered(graphics, "Смещения сохраняются автоматически · ESC — назад",
                panelX + panelWidth / 2f, panelY + panelHeight - 14, ColorUtil.withAlpha(TEXT_SECONDARY, 200));
    }

    private Module moduleAt(double mouseX, double mouseY) {
        layout();
        List<Module> list = modules();
        int listTop = panelY + 42;
        int visible = (panelHeight - 70) / rowHeight;
        int end = Math.min(list.size(), scroll + visible);
        for (int i = scroll; i < end; i++) {
            int y = listTop + (i - scroll) * rowHeight;
            if (Render2D.hovered(mouseX, mouseY, panelX + 10, y, panelWidth - 20, rowHeight - 4)) {
                return list.get(i);
            }
        }
        return null;
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        Module module = moduleAt(event.x(), event.y());
        if (module != null) {
            if (event.button() == 0) {
                dragging = module.getName();
            } else if (event.button() == 1) {
                HudOffsets.reset(module.getName());
                HudOffsets.save();
            } else if (event.button() == 2) {
                module.toggle();
                AuroraClient.CONFIG.markDirty();
            }
            return true;
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        if (dragging != null) {
            HudOffsets.add(dragging, (float) dragX, (float) dragY);
            return true;
        }
        return super.mouseDragged(event, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (dragging != null) {
            dragging = null;
            HudOffsets.save();
            return true;
        }
        return super.mouseReleased(event);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        layout();
        int visible = (panelHeight - 70) / rowHeight;
        int maxScroll = Math.max(0, modules().size() - visible);
        scroll = (int) Math.max(0, Math.min(maxScroll, scroll - Math.signum(scrollY)));
        return true;
    }

    @Override
    public void onClose() {
        HudOffsets.save();
        Minecraft.getInstance().setScreen(parent);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
