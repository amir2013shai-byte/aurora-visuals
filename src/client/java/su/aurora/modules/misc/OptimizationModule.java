package su.aurora.modules.misc;

import su.aurora.core.Category;
import su.aurora.core.Module;
import su.aurora.setting.BooleanSetting;
import su.aurora.setting.NumberSetting;

/**
 * Optimization — то, что за стенами, не рисуется.
 * Отсечение сущностей выполняется в миксине EntityRenderDispatcher#shouldRender.
 */
public class OptimizationModule extends Module {
    private final BooleanSetting entityCulling = addBoolean("Отсечение сущностей", true);
    private final NumberSetting maxDistance = addNumber("Лимит дистанции", 64, 16, 128, 1);
    private final BooleanSetting skipPlayers = addBoolean("Не отсекать игроков", true);
    private final BooleanSetting hideNameTags = addBoolean("Скрывать ники за стенами", false);

    public OptimizationModule() {
        super("Optimization", "Не рисует то, что скрыто за стенами — FPS выше", Category.MISC);
        setEnabled(true);
    }

    public boolean cullingEnabled() {
        return isEnabled() && entityCulling.get();
    }

    public double maxDistance() {
        return maxDistance.get();
    }

    public boolean skipPlayers() {
        return skipPlayers.get();
    }

    public boolean hideNameTags() {
        return isEnabled() && hideNameTags.get();
    }
}
