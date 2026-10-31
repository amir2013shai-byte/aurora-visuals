package su.aurora.mixin;

import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/**
 * Доступ к приватному setPostEffect — единственный штатный способ включить
 * произвольный пост-эффект в 1.21.11 (публичного setter'а нет).
 */
@Mixin(GameRenderer.class)
public interface GameRendererAccessor {
    @Invoker("setPostEffect")
    void aurora$setPostEffect(Identifier id);
}
