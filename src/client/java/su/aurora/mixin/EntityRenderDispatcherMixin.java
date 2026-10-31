package su.aurora.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import su.aurora.AuroraClient;
import su.aurora.core.Module;
import su.aurora.modules.misc.OptimizationModule;

/**
 * Optimization: не рисует сущности, которые полностью скрыты стенами.
 */
@Mixin(EntityRenderDispatcher.class)
public class EntityRenderDispatcherMixin {
    @Inject(method = "shouldRender", at = @At("HEAD"), cancellable = true)
    private <E extends Entity> void aurora$shouldRender(E entity, Frustum frustum, double camX, double camY,
                                                        double camZ, CallbackInfoReturnable<Boolean> cir) {
        if (AuroraClient.MODULES == null) {
            return;
        }
        Minecraft client = Minecraft.getInstance();
        if (client.level == null) {
            return;
        }
        Module module = AuroraClient.MODULES.get("Optimization");
        if (!(module instanceof OptimizationModule optimization) || !optimization.cullingEnabled()) {
            return;
        }
        if (entity instanceof Player && optimization.skipPlayers()) {
            return;
        }
        if (entity.distanceToSqr(camX, camY, camZ) > optimization.maxDistance() * optimization.maxDistance()) {
            cir.setReturnValue(false);
            return;
        }
        Vec3 from = new Vec3(camX, camY, camZ);
        Vec3 to = entity.getBoundingBox().getCenter();
        if (from.distanceToSqr(to) < 4.0) {
            return;
        }
        HitResult hit = client.level.clip(new ClipContext(from, to, ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE, entity));
        if (hit.getType() == HitResult.Type.BLOCK) {
            cir.setReturnValue(false);
        }
    }
}
