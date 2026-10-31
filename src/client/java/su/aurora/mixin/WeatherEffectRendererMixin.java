package su.aurora.mixin;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.WeatherEffectRenderer;
import net.minecraft.client.renderer.state.WeatherRenderState;
import net.minecraft.server.level.ParticleStatus;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import su.aurora.AuroraClient;
import su.aurora.core.Module;
import su.aurora.modules.misc.WeatherModule;

/**
 * Weather: убирает дождь и снег на клиенте.
 */
@Mixin(WeatherEffectRenderer.class)
public class WeatherEffectRendererMixin {
    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void aurora$render(MultiBufferSource consumers, Vec3 cameraPos, WeatherRenderState state,
                               CallbackInfo ci) {
        if (aurora$shouldHide()) {
            ci.cancel();
        }
    }

    @Inject(method = "tickRainParticles", at = @At("HEAD"), cancellable = true)
    private void aurora$tickRainParticles(ClientLevel level, net.minecraft.client.Camera camera, int ticks,
                                          ParticleStatus status, int renderDistance, CallbackInfo ci) {
        if (aurora$shouldHide()) {
            ci.cancel();
        }
    }

    private static boolean aurora$shouldHide() {
        if (AuroraClient.MODULES == null) {
            return false;
        }
        Module module = AuroraClient.MODULES.get("Weather");
        return module instanceof WeatherModule weather && weather.isEnabled()
                && (weather.hideRain() || weather.hideSnow());
    }
}
