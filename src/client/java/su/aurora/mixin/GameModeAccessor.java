package su.aurora.mixin;

import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Доступ к приватным полям ломания блока для BreakProgress.
 */
@Mixin(MultiPlayerGameMode.class)
public interface GameModeAccessor {
    @Accessor("destroyBlockPos")
    BlockPos aurora$destroyBlockPos();

    @Accessor("destroyProgress")
    float aurora$destroyProgress();

    @Accessor("isDestroying")
    boolean aurora$isDestroying();
}
