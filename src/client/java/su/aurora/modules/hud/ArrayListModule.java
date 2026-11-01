package su.aurora.modules.hud;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import su.aurora.AuroraClient;
import su.aurora.core.Category;
import su.aurora.core.Module;
import su.aurora.setting.BooleanSetting;
import su.aurora.setting.ColorSetting;
import su.aurora.setting.ModeSetting;
import su.aurora.setting.NumberSetting;
import su.aurora.util.ColorUtil;
import su.aurora.util.Render2D;

import java.util.ArrayList;
import java.util.List;

/**
 * ArrayList — список включённых модулей справа на экране.
 * Размер списка регулируется отдельно: масштаб (мельче/крупнее) и лимит строк.
 */
public class ArrayListModule extends Module {
    private final ModeSetting colorMode = addMode("Цвет", "Радуга", "Радуга", "Акцент", "Белый");
    private final ColorSetting accent = addColor("Акцент", 0xFF7C5CFF);
    private final BooleanSetting showBar = addBoolean("Полоска", true);
    private final BooleanSetting capitalise = addBoolean("Заглавные", false);
    private final NumberSetting scale = addNumber("Масштаб", 1.0, 0.5, 1.6, 0.05);
    private final NumberSetting maxEntries = addNumber("Максимум строк", 20, 3, 60, 1);
    private final BooleanSetting countHidden = addBoolean("Счётчик скрытых", true);

    public ArrayListModule() {
        super("ArrayList", "Список активных модулей справа на экране", Category.HUD);
        setEnabled(true);
    }

    @Override
    public void onHudRender(GuiGraphics graphics, DeltaTracker delta) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null || client.options.hideGui) {
            return;
        }
        List<Module> modules = new ArrayList<>();
        for (Module module : AuroraClient.MODULES.all()) {
            if (module.isEnabled() && module.getCategory() != Category.HUD) {
                modules.add(module);
            }
        }
        modules.sort((a, b) -> Render2D.width(b.getName()) - Render2D.width(a.getName()));

        int limit = maxEntries.getInt();
        int hidden = Math.max(0, modules.size() - limit);
        if (modules.size() > limit) {
            modules = new ArrayList<>(modules.subList(0, limit));
        }

        int screenWidth = client.getWindow().getGuiScaledWidth();
        float s = scale.getFloat();

        // масштабируем относительно правого верхнего угла: элементы «растут» от края
        var pose = graphics.pose();
        pose.pushMatrix();
        pose.translate(screenWidth * (1.0f - s), 6 * (1.0f - s));
        pose.scale(s, s);

        int y = 10;
        for (Module module : modules) {
            String label = capitalise.get() ? module.getName().toUpperCase() : module.getName();
            int width = Render2D.width(label) + 14;
            int x = screenWidth - width - 6;

            int textColor = switch (colorMode.get()) {
                case "Радуга" -> ColorUtil.rainbow(6000L, 0.55f, 1f, 255);
                case "Акцент" -> HudStyleModule.resolve(accent);
                default -> 0xFFFFFFFF;
            };

            Render2D.roundedRect(graphics, x, y, width, 14, 4, 0x7A0C0E14);
            Render2D.textShadow(graphics, label, x + 7, y + 4, textColor);
            if (showBar.get()) {
                Render2D.roundedRect(graphics, x + width - 2, y, 2, 14, 1,
                        ColorUtil.withAlpha(textColor, 220));
            }
            y += 16;
        }

        if (countHidden.get() && hidden > 0) {
            String more = "+" + hidden + " ещё";
            int width = Render2D.width(more) + 14;
            int x = screenWidth - width - 6;
            Render2D.roundedRect(graphics, x, y, width, 14, 4, 0x7A0C0E14);
            Render2D.textShadow(graphics, more, x + 7, y + 4, ColorUtil.withAlpha(0xFFFFFF, 170));
        }

        pose.popMatrix();
    }
}
