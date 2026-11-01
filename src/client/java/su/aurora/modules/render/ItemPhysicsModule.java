package su.aurora.modules.render;

import su.aurora.core.Category;
import su.aurora.core.Module;
import su.aurora.setting.NumberSetting;

/**
 * ItemPhysics — предметы на земле лежат плоско, как в ItemPhysics.
 * Работает через миксин в ItemEntityRenderer#submit (поворот матрицы).
 */
public class ItemPhysicsModule extends Module {
    private final NumberSetting rotation = addNumber("Поворот", 90.0, 0.0, 180.0, 5.0);
    private final NumberSetting lift = addNumber("Подъём", 0.08, 0.0, 0.5, 0.01);
    private final NumberSetting scale = addNumber("Масштаб", 1.0, 0.4, 1.6, 0.05);

    public ItemPhysicsModule() {
        super("ItemPhysics", "Предметы лежат плоско на земле", Category.RENDER);
    }

    public float rotation() {
        return rotation.getFloat();
    }

    public float lift() {
        return lift.getFloat();
    }

    public float scale() {
        return scale.getFloat();
    }
}
