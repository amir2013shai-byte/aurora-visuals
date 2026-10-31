package su.aurora.modules.hud;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import su.aurora.core.Category;
import su.aurora.core.Module;
import su.aurora.setting.BooleanSetting;
import su.aurora.setting.ModeSetting;
import su.aurora.util.ColorUtil;
import su.aurora.util.Render2D;

/**
 * ArmorHUD — броня и предмет в руке с полосками прочности.
 */
public class ArmorHudModule extends Module {
    private final ModeSetting position = addMode("Позиция", "Снизу слева", "Снизу слева", "Снизу справа");
    private final BooleanSetting durability = addBoolean("Прочность", true);
    private final BooleanSetting showCount = addBoolean("Количество", true);
    private final BooleanSetting onlyIfEquipped = addBoolean("Только если надето", true);

    public ArmorHudModule() {
        super("ArmorHUD", "Броня и предмет в руке с прочностью", Category.HUD);
    }

    @Override
    public void onHudRender(GuiGraphics graphics, DeltaTracker delta) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null || client.options.hideGui) {
            return;
        }
        EquipmentSlot[] slots = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
        ItemStack[] armor = new ItemStack[4];
        boolean any = false;
        for (int i = 0; i < 4; i++) {
            armor[i] = client.player.getItemBySlot(slots[i]);
            if (!armor[i].isEmpty()) {
                any = true;
            }
        }
        if (onlyIfEquipped.get() && !any) {
            return;
        }

        int screenWidth = client.getWindow().getGuiScaledWidth();
        int screenHeight = client.getWindow().getGuiScaledHeight();
        float size = 18;
        float gap = 2;
        float totalWidth = (size + gap) * 4;
        float x = position.is("Снизу справа") ? screenWidth - totalWidth - 8 : 8;
        float y = screenHeight - 68;

        for (int i = 0; i < 4; i++) {
            float slotX = x + i * (size + gap);
            Render2D.roundedRect(graphics, slotX, y, size, size, 4, 0x8C0C0E14);
            ItemStack stack = armor[i];
            if (stack.isEmpty()) {
                continue;
            }
            graphics.renderItem(stack, (int) slotX + 1, (int) y + 1);
            if (durability.get() && stack.isDamageableItem()) {
                float part = 1f - stack.getDamageValue() / (float) stack.getMaxDamage();
                Render2D.rect(graphics, slotX + 1, y + size - 3, size - 2, 2, 0xB0000000);
                Render2D.rect(graphics, slotX + 1, y + size - 3, (size - 2) * part, 2,
                        ColorUtil.lerp(0xFFFF4D4D, 0xFF7CF7B0, part));
            }
            if (showCount.get() && stack.getCount() > 1) {
                Render2D.textRight(graphics, String.valueOf(stack.getCount()), slotX + size - 1, y + 8, 0xFFFFFFFF);
            }
        }

        ItemStack held = client.player.getMainHandItem();
        float handX = position.is("Снизу справа") ? x + totalWidth + 6 : x + totalWidth + 6;
        Render2D.roundedRect(graphics, handX, y, size, size, 4, 0x8C0C0E14);
        if (!held.isEmpty()) {
            graphics.renderItem(held, (int) handX + 1, (int) y + 1);
            if (durability.get() && held.isDamageableItem()) {
                float part = 1f - held.getDamageValue() / (float) held.getMaxDamage();
                Render2D.rect(graphics, handX + 1, y + size - 3, size - 2, 2, 0xB0000000);
                Render2D.rect(graphics, handX + 1, y + size - 3, (size - 2) * part, 2,
                        ColorUtil.lerp(0xFFFF4D4D, 0xFF7CF7B0, part));
            }
        }
    }
}
