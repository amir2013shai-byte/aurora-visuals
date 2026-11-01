package su.aurora.mixin;

import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import su.aurora.AuroraClient;
import su.aurora.core.Module;
import su.aurora.modules.render.AntiBlindModule;

/**
 * AntiBlind: обнуляет визуальную «плотность» слепоты, темноты и тошноты.
 */
@Mixin(LivingEntity.class)
public class EffectBlendMixin {
    @Inject(method = "getEffectBlendFactor", at = @At("RETURN"), cancellable = true)
    private void aurora$antiBlind(Holder<MobEffect> effect, float partialTick, CallbackInfoReturnable<Float> cir) {
        if (AuroraClient.MODULES == null || !((Object) this instanceof net.minecraft.client.player.LocalPlayer)) {
            return;
        }
        Module module = AuroraClient.MODULES.get("AntiBlind");
        if (!(module instanceof AntiBlindModule antiBlind) || !antiBlind.isEnabled()) {
            return;
        }
        if ((antiBlind.blindness() && effect.is(MobEffects.BLINDNESS))
                || (antiBlind.darkness() && effect.is(MobEffects.DARKNESS))
                || (antiBlind.nausea() && effect.is(MobEffects.NAUSEA))) {
            cir.setReturnValue(0.0f);
        }
    }
}
