package su.aurora.ui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.lwjgl.glfw.GLFW;
import su.aurora.AuroraClient;
import su.aurora.core.Category;
import su.aurora.core.Module;
import su.aurora.setting.BooleanSetting;
import su.aurora.setting.ColorSetting;
import su.aurora.setting.ModeSetting;
import su.aurora.setting.NumberSetting;
import su.aurora.setting.Setting;
import su.aurora.setting.StringSetting;
import su.aurora.util.ColorUtil;
import su.aurora.util.Render2D;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Меню клиента: категории, модули, настройки, поиск и темы.
 */
public class ClickGuiScreen extends Screen {
    public enum Theme {
        NOVA(0xFF7C5CFF, 0xF0101218, 0xFF161A22, 0xFF2A2F3A, 0xFFFFFFFF, 0xFF9AA4B2),
        AURORA(0xFF00E5FF, 0xF00A1418, 0xFF101E24, 0xFF1E3238, 0xFFFFFFFF, 0xFF8FB3BB),
        MIDNIGHT(0xFF4D7CFE, 0xF00B0D14, 0xFF12141D, 0xFF232838, 0xFFFFFFFF, 0xFF8A93A8),
        SUNSET(0xFFFF8A4C, 0xF0140F12, 0xFF20161A, 0xFF3A2A2E, 0xFFFFFFFF, 0xFFC9A79E),
        MATRIX(0xFF3DFF7A, 0xF0061409, 0xFF0C1F11, 0xFF1B3A24, 0xFFFFFFFF, 0xFF8FBF9C);

        private final int accent;
        private final int background;
        private final int panel;
        private final int panelLight;
        private final int textPrimary;
        private final int textSecondary;

        Theme(int accent, int background, int panel, int panelLight, int textPrimary, int textSecondary) {
            this.accent = accent;
            this.background = background;
            this.panel = panel;
            this.panelLight = panelLight;
            this.textPrimary = textPrimary;
            this.textSecondary = textSecondary;
        }

        public int accent() {
            return accent;
        }

        public int background() {
            return background;
        }

        public int panel() {
            return panel;
        }

        public int panelLight() {
            return panelLight;
        }

        public int textPrimary() {
            return textPrimary;
        }

        public int textSecondary() {
            return textSecondary;
        }

        public Theme next() {
            Theme[] values = values();
            return values[(ordinal() + 1) % values.length];
        }
    }

    private static Theme theme = Theme.NOVA;
    private static float scale = 1.0f;

    private Category selectedCategory = Category.RENDER;
    private Module selectedModule;
    private String search = "";
    private boolean searchFocused;
    private Setting<?> focusedSetting;
    private boolean awaitingKeybind;
    private Setting<?> draggingSlider;
    private Setting<?> draggingColor;

    private float moduleScroll;
    private float settingScroll;

    private int panelX;
    private int panelY;
    private int panelWidth;
    private int panelHeight;

    private static final int HEADER_HEIGHT = 36;
    private static final int SIDEBAR_WIDTH = 112;
    private static final int MODULE_LIST_WIDTH = 188;

    public ClickGuiScreen() {
        super(Component.literal("Aurora Visuals"));
    }

    public static Theme getTheme() {
        return theme;
    }

    public static void setTheme(Theme newTheme) {
        theme = newTheme;
    }

    public static float getScale() {
        return scale;
    }

    public static void setScale(float newScale) {
        scale = Mth.clamp(newScale, 0.75f, 1.35f);
    }

    public static void open() {
        Minecraft.getInstance().setScreen(new ClickGuiScreen());
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private void layout() {
        panelWidth = (int) Mth.clamp(this.width * 0.78f, 520, 760);
        panelHeight = (int) Mth.clamp(this.height * 0.78f, 320, 470);
        panelX = (this.width - panelWidth) / 2;
        panelY = (this.height - panelHeight) / 2;
    }

    private List<Module> visibleModules() {
        List<Module> result = new ArrayList<>();
        String query = search.toLowerCase(Locale.ROOT).trim();
        for (Module module : AuroraClient.MODULES.all()) {
            if (!query.isEmpty()) {
                if (module.getName().toLowerCase(Locale.ROOT).contains(query)) {
                    result.add(module);
                }
                continue;
            }
            if (module.getCategory() == selectedCategory) {
                result.add(module);
            }
        }
        return result;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        layout();
        Theme theme = getTheme();

        graphics.fill(0, 0, this.width, this.height, 0x66000000);

        // Основная панель
        Render2D.roundedRect(graphics, panelX, panelY, panelWidth, panelHeight, 12, theme.background());
        Render2D.roundedRect(graphics, panelX, panelY, panelWidth, HEADER_HEIGHT, 12, theme.panel());
        graphics.fill(panelX, panelY + HEADER_HEIGHT - 1, panelX + panelWidth, panelY + HEADER_HEIGHT,
                ColorUtil.withAlpha(theme.accent(), 70));

        // Заголовок
        Render2D.roundedRect(graphics, panelX + 12, panelY + 11, 6, 14, 3, theme.accent());
        Render2D.textShadow(graphics, "AURORA", panelX + 24, panelY + 12, theme.textPrimary());
        Render2D.textShadow(graphics, "VISUALS", panelX + 24 + Render2D.width("AURORA ") + 2, panelY + 12,
                theme.accent());

        // Кнопка «Косметика»
        int cosmeticsX = panelX + panelWidth - 294;
        int cosmeticsY = panelY + 9;
        boolean cosmeticsHover = Render2D.hovered(mouseX, mouseY, cosmeticsX, cosmeticsY, 80, 18);
        Render2D.roundedRect(graphics, cosmeticsX, cosmeticsY, 80, 18, 8,
                ColorUtil.withAlpha(cosmeticsHover ? theme.accent() : theme.panelLight(), cosmeticsHover ? 90 : 190));
        Render2D.textCentered(graphics, "КОСМЕТИКА", cosmeticsX + 40, cosmeticsY + 5,
                cosmeticsHover ? theme.accent() : theme.textPrimary());

        // Поиск
        int searchWidth = 132;
        int searchX = panelX + panelWidth - searchWidth - 74;
        Render2D.roundedRect(graphics, searchX, panelY + 9, searchWidth, 18, 8,
                ColorUtil.withAlpha(theme.panelLight(), 190));
        String searchText = search.isEmpty() && !searchFocused ? "Поиск..." : search;
        Render2D.textShadow(graphics, searchText, searchX + 8, panelY + 14,
                search.isEmpty() ? ColorUtil.withAlpha(theme.textSecondary(), 170) : theme.textPrimary());
        if (searchFocused && (System.currentTimeMillis() / 500) % 2 == 0) {
            Render2D.rect(graphics, searchX + 8 + Render2D.width(search), panelY + 12, 1, 12, theme.accent());
        }

        // Кнопка темы
        Render2D.roundedRect(graphics, panelX + panelWidth - 68, panelY + 9, 34, 18, 8,
                ColorUtil.withAlpha(theme.panelLight(), 190));
        Render2D.textCentered(graphics, theme.name().substring(0, 3), panelX + panelWidth - 51, panelY + 14,
                theme.accent());

        // Кнопка закрытия
        Render2D.roundedRect(graphics, panelX + panelWidth - 28, panelY + 9, 18, 18, 8,
                ColorUtil.withAlpha(theme.panelLight(), 190));
        Render2D.textCentered(graphics, "×", panelX + panelWidth - 19, panelY + 12,
                Render2D.hovered(mouseX, mouseY, panelX + panelWidth - 28, panelY + 9, 18, 18)
                        ? theme.accent() : theme.textPrimary());

        // Сайдбар категорий
        int sidebarX = panelX + 10;
        int sidebarY = panelY + HEADER_HEIGHT + 10;
        int categoryHeight = 24;
        for (Category category : Category.values()) {
            boolean active = category == selectedCategory && search.isEmpty();
            boolean hover = Render2D.hovered(mouseX, mouseY, sidebarX, sidebarY, SIDEBAR_WIDTH - 12, categoryHeight);
            Render2D.roundedRect(graphics, sidebarX, sidebarY, SIDEBAR_WIDTH - 12, categoryHeight, 7,
                    active ? ColorUtil.withAlpha(theme.accent(), 46)
                            : hover ? ColorUtil.withAlpha(theme.panelLight(), 160) : 0x00000000);
            if (active) {
                Render2D.roundedRect(graphics, sidebarX + 2, sidebarY + 6, 3, categoryHeight - 12, 2, theme.accent());
            }
            Render2D.textShadow(graphics, category.getDisplayName(), sidebarX + 12, sidebarY + 5,
                    active ? theme.textPrimary() : theme.textSecondary());
            Render2D.textShadow(graphics, category.getRussianName(), sidebarX + 12, sidebarY + 13,
                    ColorUtil.withAlpha(active ? theme.accent() : theme.textSecondary(), 150));
            int count = AuroraClient.MODULES.inCategory(category).size();
            if (count > 0) {
                Render2D.textRight(graphics, String.valueOf(count), sidebarX + SIDEBAR_WIDTH - 22,
                        sidebarY + 8, theme.accent());
            }
            sidebarY += categoryHeight + 4;
        }

        // Нижняя подпись
        Render2D.textShadow(graphics, "v" + su.aurora.Aurora.VERSION + " · 1.21.11",
                sidebarX + 4, panelY + panelHeight - 18,
                ColorUtil.withAlpha(theme.textSecondary(), 170));

        // Список модулей
        int listX = panelX + SIDEBAR_WIDTH;
        int listY = panelY + HEADER_HEIGHT + 10;
        int listWidth = MODULE_LIST_WIDTH;
        int listHeight = panelHeight - HEADER_HEIGHT - 24;
        Render2D.roundedRect(graphics, listX, listY, listWidth, listHeight, 9, theme.panel());

        List<Module> modules = visibleModules();
        Render2D.scissor(graphics, listX, listY + 4, listWidth, listHeight - 8);
        int rowY = listY + 6 - (int) moduleScroll;
        for (Module module : modules) {
            boolean hover = Render2D.hovered(mouseX, mouseY, listX + 4, rowY, listWidth - 8, 22)
                    && mouseY > listY && mouseY < listY + listHeight;
            boolean selected = module == selectedModule;
            if (hover || selected) {
                Render2D.roundedRect(graphics, listX + 4, rowY, listWidth - 8, 22, 6,
                        selected ? ColorUtil.withAlpha(theme.accent(), 40)
                                : ColorUtil.withAlpha(theme.panelLight(), 150));
            }
            Render2D.textShadow(graphics, module.getName(), listX + 12, rowY + 7,
                    module.isEnabled() ? theme.accent() : theme.textSecondary());
            // переключатель
            int toggleX = listX + listWidth - 32;
            Render2D.roundedRect(graphics, toggleX, rowY + 5, 22, 12, 6,
                    module.isEnabled() ? ColorUtil.withAlpha(theme.accent(), 210) : 0x50FFFFFF);
            float knobX = module.isEnabled() ? toggleX + 12 : toggleX + 2;
            Render2D.roundedRect(graphics, knobX, rowY + 7, 8, 8, 4, 0xFFFFFFFF);
            rowY += 24;
        }
        Render2D.unscissor(graphics);

        // Панель настроек
        int settingsX = listX + listWidth + 10;
        int settingsWidth = panelX + panelWidth - settingsX - 10;
        int settingsY = listY;
        int settingsHeight = listHeight;
        Render2D.roundedRect(graphics, settingsX, settingsY, settingsWidth, settingsHeight, 9, theme.panel());

        if (selectedModule == null) {
            Render2D.textCentered(graphics, "Выбери модуль слева", settingsX + settingsWidth / 2f,
                    settingsY + settingsHeight / 2f - 4, ColorUtil.withAlpha(theme.textSecondary(), 200));
            return;
        }

        Render2D.textShadow(graphics, selectedModule.getName(), settingsX + 12, settingsY + 10, theme.textPrimary());
        Render2D.textShadow(graphics, selectedModule.getDescription(), settingsX + 12, settingsY + 22,
                ColorUtil.withAlpha(theme.textSecondary(), 200));
        graphics.fill(settingsX + 12, settingsY + 36, settingsX + settingsWidth - 12, settingsY + 37,
                ColorUtil.withAlpha(theme.panelLight(), 200));

        // Бинд
        String keyText = awaitingKeybind ? "Нажми клавишу..."
                : "Бинд: " + selectedModule.getKeyName();
        Render2D.roundedRect(graphics, settingsX + 12, settingsY + 42, settingsWidth - 24, 16, 6,
                ColorUtil.withAlpha(theme.panelLight(), 170));
        Render2D.textShadow(graphics, keyText, settingsX + 18, settingsY + 46,
                awaitingKeybind ? theme.accent() : theme.textSecondary());

        // Настройки
        float row = settingsY + 64 - settingScroll;
        Render2D.scissor(graphics, settingsX, settingsY + 58, settingsWidth, settingsHeight - 64);
        for (Setting<?> setting : selectedModule.getSettings()) {
            row = drawSetting(graphics, setting, settingsX + 12, row, settingsWidth - 24, mouseX, mouseY);
            row += 6;
        }
        Render2D.unscissor(graphics);
    }

    private float drawSetting(GuiGraphics graphics, Setting<?> setting, float x, float y, float width,
                              int mouseX, int mouseY) {
        Theme theme = getTheme();
        if (setting instanceof BooleanSetting booleanSetting) {
            Render2D.textShadow(graphics, booleanSetting.getName(), x, y + 3, theme.textSecondary());
            int toggleX = (int) (x + width - 26);
            Render2D.roundedRect(graphics, toggleX, y + 1, 26, 13, 6,
                    booleanSetting.get() ? ColorUtil.withAlpha(theme.accent(), 210) : 0x50FFFFFF);
            Render2D.roundedRect(graphics, toggleX + (booleanSetting.get() ? 14 : 3), y + 3, 9, 9, 4, 0xFFFFFFFF);
            return y + 18;
        }
        if (setting instanceof NumberSetting numberSetting) {
            Render2D.textShadow(graphics, numberSetting.getName(), x, y, theme.textSecondary());
            Render2D.textRight(graphics, numberSetting.displayValue(), x + width, y, theme.accent());
            float barY = y + 12;
            Render2D.roundedRect(graphics, x, barY, width, 5, 2, ColorUtil.withAlpha(0xFFFFFFFF, 40));
            float progress = numberSetting.getProgress();
            Render2D.roundedRect(graphics, x, barY, Math.max(2, width * progress), 5, 2, theme.accent());
            Render2D.roundedRect(graphics, x + width * progress - 2.5f, barY - 3, 5, 11, 2, 0xFFFFFFFF);
            return y + 24;
        }
        if (setting instanceof ModeSetting modeSetting) {
            Render2D.textShadow(graphics, modeSetting.getName(), x, y, theme.textSecondary());
            int buttonY = (int) (y + 11);
            int buttonWidth = (int) ((width - 4) / Math.max(1, modeSetting.getModes().size()));
            int index = 0;
            for (String mode : modeSetting.getModes()) {
                boolean active = modeSetting.get().equals(mode);
                Render2D.roundedRect(graphics, x + index * (buttonWidth + 2), buttonY, buttonWidth, 14, 5,
                        active ? ColorUtil.withAlpha(theme.accent(), 190)
                                : ColorUtil.withAlpha(theme.panelLight(), 180));
                Render2D.textCentered(graphics, mode, x + index * (buttonWidth + 2) + buttonWidth / 2f,
                        buttonY + 3, active ? 0xFF10121A : theme.textSecondary());
                index++;
            }
            return y + 30;
        }
        if (setting instanceof ColorSetting colorSetting) {
            Render2D.textShadow(graphics, colorSetting.getName(), x, y, theme.textSecondary());
            Render2D.textRight(graphics, colorSetting.displayValue(), x + width, y, theme.textSecondary());
            float barY = y + 12;
            for (int i = 0; i < (int) width; i++) {
                int color = java.awt.Color.HSBtoRGB(i / width, 0.75f, 1.0f);
                Render2D.rect(graphics, x + i, barY, 1, 6, color | 0xFF000000);
            }
            Render2D.roundedRect(graphics, x + width * colorSetting.getHue() - 2, barY - 2, 4, 10, 2, 0xFFFFFFFF);
            Render2D.roundedRect(graphics, x, barY + 10, 16, 10, 3, colorSetting.get());
            return y + 26;
        }
        if (setting instanceof StringSetting stringSetting) {
            Render2D.textShadow(graphics, stringSetting.getName(), x, y, theme.textSecondary());
            Render2D.roundedRect(graphics, x, y + 11, width, 16, 5,
                    focusedSetting == setting ? ColorUtil.withAlpha(theme.accent(), 60)
                            : ColorUtil.withAlpha(theme.panelLight(), 170));
            Render2D.textShadow(graphics, stringSetting.get(), x + 6, y + 15,
                    focusedSetting == setting ? theme.textPrimary() : theme.textSecondary());
            if (focusedSetting == setting && (System.currentTimeMillis() / 500) % 2 == 0) {
                Render2D.rect(graphics, x + 6 + Render2D.width(stringSetting.get()), y + 13, 1, 11,
                        theme.accent());
            }
            return y + 32;
        }
        return y + 18;
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        layout();
        int mouseX = (int) event.x();
        int mouseY = (int) event.y();
        Theme theme = getTheme();
        searchFocused = false;

        // закрытие
        if (Render2D.hovered(mouseX, mouseY, panelX + panelWidth - 28, panelY + 9, 18, 18)) {
            onClose();
            return true;
        }
        // смена темы
        if (Render2D.hovered(mouseX, mouseY, panelX + panelWidth - 68, panelY + 9, 34, 18)) {
            setTheme(theme.next());
            AuroraClient.CONFIG.markDirty();
            return true;
        }
        // открытие экрана косметики
        if (Render2D.hovered(mouseX, mouseY, panelX + panelWidth - 294, panelY + 9, 80, 18)) {
            Minecraft.getInstance().setScreen(new CosmeticScreen(this));
            return true;
        }
        // поиск
        int searchWidth = 132;
        int searchX = panelX + panelWidth - searchWidth - 74;
        if (Render2D.hovered(mouseX, mouseY, searchX, panelY + 9, searchWidth, 18)) {
            searchFocused = true;
            focusedSetting = null;
            return true;
        }

        // категории
        int sidebarX = panelX + 10;
        int sidebarY = panelY + HEADER_HEIGHT + 10;
        for (Category category : Category.values()) {
            if (Render2D.hovered(mouseX, mouseY, sidebarX, sidebarY, SIDEBAR_WIDTH - 12, 24)) {
                selectedCategory = category;
                search = "";
                moduleScroll = 0;
                settingScroll = 0;
                return true;
            }
            sidebarY += 28;
        }

        // модули
        int listX = panelX + SIDEBAR_WIDTH;
        int listY = panelY + HEADER_HEIGHT + 10;
        int listHeight = panelHeight - HEADER_HEIGHT - 24;
        if (mouseX >= listX && mouseX <= listX + MODULE_LIST_WIDTH && mouseY >= listY && mouseY <= listY + listHeight) {
            int rowY = listY + 6 - (int) moduleScroll;
            for (Module module : visibleModules()) {
                if (mouseY >= rowY && mouseY <= rowY + 22) {
                    if (mouseX >= listX + MODULE_LIST_WIDTH - 36) {
                        module.toggle();
                        AuroraClient.CONFIG.markDirty();
                    } else {
                        selectedModule = module;
                        settingScroll = 0;
                    }
                    return true;
                }
                rowY += 24;
            }
            return true;
        }

        // бинд
        if (selectedModule != null) {
            int settingsX = listX + MODULE_LIST_WIDTH + 10;
            int settingsWidth = panelX + panelWidth - settingsX - 10;
            if (Render2D.hovered(mouseX, mouseY, settingsX + 12, listY + 42, settingsWidth - 24, 16)) {
                awaitingKeybind = true;
                return true;
            }

            float row = listY + 64 - settingScroll;
            for (Setting<?> setting : selectedModule.getSettings()) {
                float height = setting instanceof NumberSetting ? 24
                        : setting instanceof ModeSetting ? 30
                        : setting instanceof ColorSetting ? 26
                        : setting instanceof StringSetting ? 32 : 18;
                if (mouseY >= row && mouseY <= row + height && mouseX >= settingsX + 12
                        && mouseX <= settingsX + settingsWidth - 12) {
                    handleSettingClick(setting, mouseX, row, settingsX + 12, settingsWidth - 24);
                    return true;
                }
                row += height + 6;
            }
        }
        return super.mouseClicked(event, doubleClick);
    }

    private void handleSettingClick(Setting<?> setting, int mouseX, float row, float x, float width) {
        focusedSetting = null;
        if (setting instanceof BooleanSetting booleanSetting) {
            booleanSetting.toggle();
            AuroraClient.CONFIG.markDirty();
        } else if (setting instanceof NumberSetting numberSetting) {
            draggingSlider = numberSetting;
            numberSetting.setProgress((mouseX - x) / width);
            AuroraClient.CONFIG.markDirty();
        } else if (setting instanceof ModeSetting modeSetting) {
            int count = modeSetting.getModes().size();
            int buttonWidth = (int) ((width - 4) / Math.max(1, count));
            int index = Mth.clamp((int) ((mouseX - x) / Math.max(1, buttonWidth + 2)), 0, count - 1);
            modeSetting.set(modeSetting.getModes().get(index));
            AuroraClient.CONFIG.markDirty();
        } else if (setting instanceof ColorSetting colorSetting) {
            draggingColor = colorSetting;
            colorSetting.setHue(Mth.clamp((mouseX - x) / width, 0f, 1f));
            AuroraClient.CONFIG.markDirty();
        } else if (setting instanceof StringSetting) {
            focusedSetting = setting;
        }
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        layout();
        int settingsX = panelX + SIDEBAR_WIDTH + MODULE_LIST_WIDTH + 10;
        int settingsWidth = panelX + panelWidth - settingsX - 10;
        float x = settingsX + 12;
        float width = settingsWidth - 24;
        if (draggingSlider instanceof NumberSetting numberSetting) {
            numberSetting.setProgress((float) ((event.x() - x) / width));
            AuroraClient.CONFIG.markDirty();
            return true;
        }
        if (draggingColor instanceof ColorSetting colorSetting) {
            colorSetting.setHue(Mth.clamp((float) ((event.x() - x) / width), 0f, 1f));
            AuroraClient.CONFIG.markDirty();
            return true;
        }
        return super.mouseDragged(event, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        draggingSlider = null;
        draggingColor = null;
        return super.mouseReleased(event);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        layout();
        int listX = panelX + SIDEBAR_WIDTH;
        int listY = panelY + HEADER_HEIGHT + 10;
        int listHeight = panelHeight - HEADER_HEIGHT - 24;
        if (mouseX >= listX && mouseX <= listX + MODULE_LIST_WIDTH && mouseY >= listY
                && mouseY <= listY + listHeight) {
            moduleScroll = Mth.clamp((float) (moduleScroll - scrollY * 18), 0,
                    Math.max(0, visibleModules().size() * 24 - listHeight + 12));
            return true;
        }
        if (selectedModule != null) {
            float content = selectedModule.getSettings().size() * 32f;
            settingScroll = Mth.clamp((float) (settingScroll - scrollY * 18), 0,
                    Math.max(0, content - listHeight + 80));
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (awaitingKeybind && selectedModule != null) {
            int key = event.key();
            if (key != GLFW.GLFW_KEY_ESCAPE) {
                selectedModule.setKey(key == GLFW.GLFW_KEY_DELETE ? -1 : key);
                AuroraClient.CONFIG.markDirty();
            }
            awaitingKeybind = false;
            return true;
        }
        if (focusedSetting instanceof StringSetting stringSetting) {
            if (event.key() == GLFW.GLFW_KEY_BACKSPACE) {
                String value = stringSetting.get();
                if (!value.isEmpty()) {
                    stringSetting.set(value.substring(0, value.length() - 1));
                    AuroraClient.CONFIG.markDirty();
                }
                return true;
            }
            if (event.key() == GLFW.GLFW_KEY_ENTER) {
                focusedSetting = null;
                return true;
            }
        }
        if (searchFocused && event.key() == GLFW.GLFW_KEY_BACKSPACE) {
            if (!search.isEmpty()) {
                search = search.substring(0, search.length() - 1);
            }
            return true;
        }
        if (event.key() == GLFW.GLFW_KEY_ESCAPE) {
            onClose();
            return true;
        }
        return super.keyPressed(event);
    }

    @Override
    public boolean charTyped(CharacterEvent event) {
        char character = (char) event.codepoint();
        if (!Character.isISOControl(character)) {
            if (focusedSetting instanceof StringSetting stringSetting
                    && stringSetting.get().length() < stringSetting.getMaxLength()) {
                stringSetting.set(stringSetting.get() + character);
                AuroraClient.CONFIG.markDirty();
                return true;
            }
            if (searchFocused && search.length() < 24) {
                search += character;
                return true;
            }
        }
        return super.charTyped(event);
    }

    @Override
    public void removed() {
        AuroraClient.CONFIG.save();
        super.removed();
    }
}
