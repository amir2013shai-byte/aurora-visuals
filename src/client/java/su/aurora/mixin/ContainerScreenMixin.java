package su.aurora.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import su.aurora.AuroraClient;
import su.aurora.core.Module;
import su.aurora.modules.misc.ItemScrollerModule;

/**
 * ItemScroller: колесо над слотом переносит содержимое (shift-клик).
 */
@Mixin(AbstractContainerScreen.class)
public abstract class ContainerScreenMixin {
    @Shadow
    protected AbstractContainerMenu menu;

    @Shadow
    protected Slot hoveredSlot;

    @Inject(method = "mouseScrolled", at = @At("HEAD"), cancellable = true)
    private void aurora$scroller(double mouseX, double mouseY, double scrollX, double scrollY,
                                 CallbackInfoReturnable<Boolean> cir) {
        if (AuroraClient.MODULES == null || scrollY == 0) {
            return;
        }
        Module module = AuroraClient.MODULES.get("ItemScroller");
        if (!(module instanceof ItemScrollerModule scroller) || !scroller.isEnabled()) {
            return;
        }
        if (hoveredSlot == null || !hoveredSlot.hasItem()) {
            return;
        }
        if (scroller.shiftOnly() && !Minecraft.getInstance().options.keyShift.isDown()) {
            return;
        }
        Minecraft client = Minecraft.getInstance();
        if (client.gameMode == null || client.player == null) {
            return;
        }
        client.gameMode.handleInventoryMouseClick(menu.containerId, hoveredSlot.index, 0,
                ClickType.QUICK_MOVE, client.player);
        cir.setReturnValue(true);
    }
}
