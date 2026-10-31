package su.aurora.modules.hud;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import su.aurora.AuroraClient;
import su.aurora.core.Category;
import su.aurora.core.Module;
import su.aurora.core.Notifications;
import su.aurora.setting.BooleanSetting;
import su.aurora.setting.ColorSetting;
import su.aurora.util.ColorUtil;
import su.aurora.util.Render2D;

/**
 * Dynamic Island — капсула сверху экрана: уведомления, музыка и истекающие эффекты.
 */
public class DynamicIslandModule extends Module {
    private final BooleanSetting notifications = addBoolean("Уведомления", true);
    private final BooleanSetting music = addBoolean("Музыка", true);
    private final BooleanSetting effects = addBoolean("Эффекты", true);
    private final ColorSetting accent = addColor("Акцент", 0xFF7C5CFF);

    private float currentWidth = 120f;

    public DynamicIslandModule() {
        super("DynamicIsland", "Капсула сверху: музыка, эффекты и уведомления", Category.HUD);
        setEnabled(true);
    }

    @Override
    public void onHudRender(GuiGraphics graphics, DeltaTracker delta) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null || client.options.hideGui) {
            return;
        }

        String title = null;
        String text = null;
        String icon = "◆";
        int tint = accent.get();

        if (notifications.get() && AuroraClient.NOTIFICATIONS != null) {
            Notifications.Entry entry = AuroraClient.NOTIFICATIONS.latest();
            if (entry != null) {
                title = entry.title;
                text = entry.text;
                tint = entry.accent;
                icon = "✦";
            }
        }

        if (title == null && music.get()) {
            String key = client.getMusicManager().getCurrentMusicTranslationKey();
            if (key != null && !key.isBlank()) {
                title = "Сейчас играет";
                text = Component.translatable(key).getString();
                icon = "♪";
                tint = 0xFF5CE1E6;
            }
        }

        if (title == null && effects.get()) {
            MobEffectInstance soonest = null;
            for (MobEffectInstance instance : client.player.getActiveEffects()) {
                if (instance.getDuration() < 20 * 30 && instance.getDuration() > 0) {
                    if (soonest == null || instance.getDuration() < soonest.getDuration()) {
                        soonest = instance;
                    }
                }
            }
            if (soonest != null) {
                Holder<net.minecraft.world.effect.MobEffect> holder = soonest.getEffect();
                title = holder.value().getDisplayName().getString();
                text = "истекает через " + (soonest.getDuration() / 20) + " с";
                icon = "!";
                tint = 0xFFFFB74D;
            }
        }

        int screenWidth = client.getWindow().getGuiScaledWidth();
        if (title == null) {
            // пустая мини-капсула с биндом меню
            currentWidth = Render2D.animate(currentWidth, 74f, 0.2f);
            float x = (screenWidth - currentWidth) / 2f;
            Render2D.roundedRect(graphics, x, 4, currentWidth, 16, 8, 0x8C0C0E14);
            Render2D.textCentered(graphics, "AURORA · RSHIFT", screenWidth / 2f, 8, 0xFF6C7686);
            return;
        }

        String content = title + "  " + text;
        float targetWidth = Math.max(120f, Render2D.width(content) + 46f);
        currentWidth = Render2D.animate(currentWidth, targetWidth, 0.25f);
        float x = (screenWidth - currentWidth) / 2f;

        Render2D.roundedRect(graphics, x, 4, currentWidth, 20, 10, 0xC70C0E14);
        Render2D.roundedRect(graphics, x + 6, 10, 8, 8, 4, tint);
        Render2D.textShadow(graphics, icon, x + 17, 10, tint);
        Render2D.textShadow(graphics, title, x + 30, 10, 0xFFFFFFFF);
        Render2D.textShadow(graphics, text, x + 34 + Render2D.width(title), 10,
                ColorUtil.withAlpha(0xFFFFFFFF, 170));
    }
}
