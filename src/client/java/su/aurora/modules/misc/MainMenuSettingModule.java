package su.aurora.modules.misc;

import net.minecraft.client.gui.GuiGraphics;
import su.aurora.core.Category;
import su.aurora.core.Module;
import su.aurora.setting.BooleanSetting;
import su.aurora.setting.ColorSetting;
import su.aurora.setting.NumberSetting;
import su.aurora.util.ColorUtil;
import su.aurora.util.Render2D;

import java.util.Random;

/**
 * MainMenuSetting — своё оформление главного меню: градиент, частицы, вуаль и подпись.
 * Работает через TitleScreenMixin: подменяет renderBackground у ванильного TitleScreen,
 * поэтому все кнопки и мир на фоне остаются на месте.
 */
public class MainMenuSettingModule extends Module {
    private final ColorSetting top = addColor("Цвет сверху", 0xFF10131C);
    private final ColorSetting bottom = addColor("Цвет снизу", 0xFF2A1533);
    private final NumberSetting veil = addNumber("Затемнение", 120, 0, 240, 10);
    private final BooleanSetting particles = addBoolean("Частицы", true);
    private final BooleanSetting gradient = addBoolean("Градиент", true);
    private final BooleanSetting watermark = addBoolean("Подпись клиента", true);
    private final ColorSetting accent = addColor("Акцент", 0xFF7C5CFF);

    private final Random random = new Random();

    public MainMenuSettingModule() {
        super("MainMenuSetting", "Своё оформление главного меню", Category.RENDER);
    }

    public void renderMenuBackground(GuiGraphics graphics, int width, int height) {
        int alphaVeil = veil.getInt();
        if (gradient.get()) {
            Render2D.verticalGradient(graphics, 0, 0, width, height,
                    ColorUtil.withAlpha(top.get(), 255 - alphaVeil),
                    ColorUtil.withAlpha(bottom.get(), 255 - alphaVeil));
        } else {
            graphics.fill(0, 0, width, height, ColorUtil.withAlpha(top.get(), 255 - alphaVeil));
        }

        if (particles.get()) {
            float time = System.currentTimeMillis() / 1000f;
            for (int i = 0; i < 48; i++) {
                float seed = i * 97.13f;
                float speed = 12f + (i % 7) * 3f;
                float x = (seed * 37.7f + (float) Math.sin(time * 0.35f + i) * 26f) % width;
                float y = height - ((seed * 13.3f + time * speed) % (height + 40));
                int size = 1 + (i % 3);
                int color = ColorUtil.withAlpha(accent.get(), 40 + (i % 5) * 14);
                graphics.fill((int) x, (int) y, (int) x + size, (int) y + size, color);
            }
        }

        if (watermark.get()) {
            String text = "AURORA VISUALS  •  " + su.aurora.Aurora.VERSION;
            Render2D.textCentered(graphics, text, width / 2f, height - 22, ColorUtil.withAlpha(accent.get(), 200));
        }
    }

    public int accentColor() {
        return accent.get();
    }
}
