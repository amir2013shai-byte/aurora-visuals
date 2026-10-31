package su.aurora.mixin;

import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.core.particles.ParticleOptions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import su.aurora.AuroraClient;
import su.aurora.core.Module;
import su.aurora.modules.particles.TotemParticlesModule;

/**
 * TotemParticles: перехватывает ванильные частицы тотема и заменяет своими.
 */
@Mixin(ParticleEngine.class)
public class ParticleEngineMixin {
    @Inject(method = "createParticle", at = @At("HEAD"), cancellable = true)
    private void aurora$createParticle(ParticleOptions options, double x, double y, double z,
                                       double velocityX, double velocityY, double velocityZ,
                                       CallbackInfoReturnable<Particle> cir) {
        if (AuroraClient.MODULES == null) {
            return;
        }
        Module module = AuroraClient.MODULES.get("TotemParticles");
        if (module instanceof TotemParticlesModule totem && totem.isEnabled()
                && totem.onParticle(options, x, y, z)) {
            cir.setReturnValue(null);
        }
    }
}
