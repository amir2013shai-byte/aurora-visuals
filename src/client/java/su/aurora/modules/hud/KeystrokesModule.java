package su.aurora.modules.hud;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import su.aurora.core.Category;
import su.aurora.core.Module;
import su.aurora.setting.BooleanSetting;
import su.aurora.setting.ColorSetting;
import su.aurora.setting.ModeSetting;
import su.aurora.setting.NumberSetting;
import su.aurora.util.ColorUtil;
import su.aurora.util.Render2D;

/**
 * Keystrokes — нажимаемые клавиши (WASD, пробел, ЛКМ/ПКМ) и CPS.
 */
public class KeystrokesModule extends Module {
    private final ModeSetting style = addMode("Стиль", "WASD + мышь", "WASD", "WASD + мышь", "Только WASD");
    private final NumberSetting size = addNumber("Размер", 16, 10, 26, 1);
    private final ColorSetting accent = addColor("Акцент", 0xFF7C5CFF);
    private final BooleanSetting showCps = addBoolean("Показывать CPS", true);
    private final BooleanSetting showSpace = addBoolean("Пробел", true);

    private boolean wasAttack;
    private boolean wasUse;
    private int leftClicks;
    private int rightClicks;
    private int leftCps;
    private int rightCps;

    public KeystrokesModule() {
        super("Keystrokes", "Клавиши WASD, мышь и CPS на экране", Category.HUD);
    }

    @Override
    public void onTick() {
        Minecraft client = Minecraft.getInstance();
        boolean attack = client.options.keyAttack.isDown();
        boolean use = client.options.keyUse.isDown();
        if (attack && !wasAttack) {
            leftClicks++;
        }
        if (use && !wasUse) {
            rightClicks++;
        }
        wasAttack = attack;
        wasUse = use;

        // CPS обновляем раз в секунду
        if (client.level != null && client.level.getGameTime() % 20 == 0) {
            leftCps = leftClicks;
            rightCps = rightClicks;
            leftClicks = 0;
            rightClicks = 0;
        }
    }

    @Override
    public void onHudRender(GuiGraphics graphics, DeltaTracker delta) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null || client.options.hideGui) {
            return;
        }
        float box = size.getFloat();
        float gap = 3;
        float x = 8;
        float y = client.getWindow().getGuiScaledHeight() - (box * 3 + gap * 2) - 76;
        int accentColor = HudStyleModule.resolve(accent);
        int bg = 0x8C0C0E14;
        int text = 0xFFFFFFFF;

        boolean mouse = !style.is("WASD");

        // W
        drawKey(graphics, x + box + gap, y, box, client.options.keyUp.isDown(), "W", bg, accentColor, text);
        // A S D
        drawKey(graphics, x, y + box + gap, box, client.options.keyLeft.isDown(), "A", bg, accentColor, text);
        drawKey(graphics, x + box + gap, y + box + gap, box, client.options.keyDown.isDown(), "S", bg, accentColor, text);
        drawKey(graphics, x + (box + gap) * 2, y + box + gap, box, client.options.keyRight.isDown(), "D", bg, accentColor, text);

        float bottom = y + (box + gap) * 2;
        if (showSpace.get()) {
            float spaceWidth = box * 3 + gap * 2;
            Render2D.roundedRect(graphics, x, bottom, spaceWidth, box * 0.55f, 4,
                    client.options.keyJump.isDown() ? ColorUtil.withAlpha(accentColor, 200) : bg);
            Render2D.textCentered(graphics, "SPACE", x + spaceWidth / 2f, bottom + box * 0.13f,
                    client.options.keyJump.isDown() ? 0xFF10121A : text);
            bottom += box * 0.55f + gap;
        }

        if (mouse) {
            float mouseWidth = (box * 3 + gap * 2 - gap) / 2f;
            drawMouse(graphics, x, bottom, mouseWidth, box * 0.75f, client.options.keyAttack.isDown(), "LMB",
                    showCps.get() ? String.valueOf(leftCps) : null, bg, accentColor, text);
            drawMouse(graphics, x + mouseWidth + gap, bottom, mouseWidth, box * 0.75f,
                    client.options.keyUse.isDown(), "RMB", showCps.get() ? String.valueOf(rightCps) : null,
                    bg, accentColor, text);
        }
    }

    private void drawKey(GuiGraphics graphics, float x, float y, float size, boolean pressed, String label,
                         int bg, int accent, int text) {
        Render2D.roundedRect(graphics, x, y, size, size, 4, pressed ? ColorUtil.withAlpha(accent, 200) : bg);
        Render2D.textCentered(graphics, label, x + size / 2f, y + size / 2f - 4,
                pressed ? 0xFF10121A : text);
    }

    private void drawMouse(GuiGraphics graphics, float x, float y, float width, float height, boolean pressed,
                           String label, String cps, int bg, int accent, int text) {
        Render2D.roundedRect(graphics, x, y, width, height, 4, pressed ? ColorUtil.withAlpha(accent, 200) : bg);
        Render2D.textCentered(graphics, label, x + width / 2f, y + 2,
                pressed ? 0xFF10121A : text);
        if (cps != null) {
            Render2D.textCentered(graphics, cps, x + width / 2f, y + height - 8,
                    pressed ? 0xFF10121A : ColorUtil.withAlpha(text, 180));
        }
    }
}
