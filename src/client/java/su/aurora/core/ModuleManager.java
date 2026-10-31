package su.aurora.core;

import su.aurora.modules.hud.ArrayListModule;
import su.aurora.modules.hud.ArmorHudModule;
import su.aurora.modules.hud.CoordsHudModule;
import su.aurora.modules.hud.CrosshairModule;
import su.aurora.modules.hud.KeystrokesModule;
import su.aurora.modules.hud.PlayerListModule;
import su.aurora.modules.hud.PotionHudModule;
import su.aurora.modules.hud.DynamicIslandModule;
import su.aurora.modules.hud.TargetHudModule;
import su.aurora.modules.hud.WatermarkModule;
import su.aurora.modules.misc.BindCommandModule;
import su.aurora.modules.misc.CustomFogModule;
import su.aurora.modules.misc.FullbrightModule;
import su.aurora.modules.misc.OptimizationModule;
import su.aurora.modules.misc.ScreenTweaksModule;
import su.aurora.modules.misc.WeatherModule;
import su.aurora.modules.particles.HitParticlesModule;
import su.aurora.modules.particles.JumpCirclesModule;
import su.aurora.modules.particles.KillEffectModule;
import su.aurora.modules.particles.TotemParticlesModule;
import su.aurora.modules.player.HitSoundsModule;
import su.aurora.modules.player.SlotLockModule;
import su.aurora.modules.player.TargetActionsModule;
import su.aurora.modules.render.AuraModule;
import su.aurora.modules.render.CapeModule;
import su.aurora.modules.render.HatsModule;
import su.aurora.modules.render.NameTagsModule;
import su.aurora.modules.render.HitColorModule;
import su.aurora.modules.render.MotionBlurModule;
import su.aurora.modules.render.ItemHighlightModule;
import su.aurora.modules.render.SkeletonEspModule;
import su.aurora.modules.render.TracersModule;
import su.aurora.modules.render.TargetEspModule;
import su.aurora.modules.render.TrailsModule;
import su.aurora.modules.render.WingsModule;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ModuleManager {
    private final List<Module> modules = new ArrayList<>();
    private final Map<String, Module> byName = new LinkedHashMap<>();

    public void init() {
        // Render
        register(new TargetEspModule());
        register(new TrailsModule());
        register(new CapeModule());
        register(new WingsModule());
        register(new ItemHighlightModule());
        register(new AuraModule());
        register(new TracersModule());
        register(new SkeletonEspModule());
        register(new HatsModule());
        register(new MotionBlurModule());
        register(new HitColorModule());
        register(new NameTagsModule());
        // Particles
        register(new JumpCirclesModule());
        register(new TotemParticlesModule());
        register(new HitParticlesModule());
        register(new KillEffectModule());
        // Interface
        register(new WatermarkModule());
        register(new ArrayListModule());
        register(new TargetHudModule());
        register(new DynamicIslandModule());
        register(new CrosshairModule());
        register(new KeystrokesModule());
        register(new PotionHudModule());
        register(new ArmorHudModule());
        register(new PlayerListModule());
        register(new CoordsHudModule());
        // Player
        register(new SlotLockModule());
        register(new TargetActionsModule());
        register(new HitSoundsModule());
        register(new BindCommandModule());
        // Misc
        register(new CustomFogModule());
        register(new WeatherModule());
        register(new FullbrightModule());
        register(new OptimizationModule());
        register(new ScreenTweaksModule());
    }

    public void register(Module module) {
        modules.add(module);
        byName.put(module.getName().toLowerCase(), module);
    }

    public List<Module> all() {
        return modules;
    }

    public Module get(String name) {
        return byName.get(name.toLowerCase());
    }

    public List<Module> inCategory(Category category) {
        List<Module> result = new ArrayList<>();
        for (Module module : modules) {
            if (module.getCategory() == category && module.isEnabled()) {
                result.add(module);
            }
        }
        return result;
    }
}
