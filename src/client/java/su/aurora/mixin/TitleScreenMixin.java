package su.aurora.mixin;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.TitleScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import su.aurora.AuroraClient;
import su.aurora.core.Module;
import su.aurora.modules.misc.MainMenuSettingModule;

/**
 * MainMenuSetting: своя подложка главного меню вместо ванильной.
 */
@Mixin(TitleScreen.class)
public class TitleScreenMixin {
    @Inject(method = "renderBackground", at = @At("HEAD"), cancellable = true)
    private void aurora$mainMenu(GuiGraphics graphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        if (AuroraClient.MODULES == null) {
            return;
        }
        Module module = AuroraClient.MODULES.get("MainMenuSetting");
        if (module instanceof MainMenuSettingModule menu && menu.isEnabled()) {
            menu.renderMenuBackground(graphics, graphics.guiWidth(), graphics.guiHeight());
            ci.cancel();
        }
    }
}
