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
import su.aurora.modules.render.FootprintsModule;
import su.aurora.modules.render.WaypointsModule;
import su.aurora.modules.render.SeasonsModule;
import su.aurora.modules.render.BlockShadowsModule;
import su.aurora.modules.render.DynamicLightsModule;
import su.aurora.modules.render.BlockPlaceModule;
import su.aurora.modules.render.CrystalColorModule;
import su.aurora.modules.render.AnimationsModule;
import su.aurora.modules.misc.FriendsModule;
import su.aurora.modules.misc.NoPauseModule;
import su.aurora.modules.misc.ItemScrollerModule;
import su.aurora.modules.misc.MainMenuSettingModule;
import su.aurora.modules.player.ZoomModule;
import su.aurora.modules.render.StorageEspModule;
import su.aurora.modules.render.BlockEspModule;
import su.aurora.modules.hud.RadarModule;
import su.aurora.modules.hud.ServerInfoModule;
import su.aurora.modules.hud.StaffListModule;
import su.aurora.modules.hud.ClockModule;
import su.aurora.modules.hud.FpsGraphModule;
import su.aurora.modules.render.NoHurtCamModule;
import su.aurora.modules.render.NoFireOverlayModule;
import su.aurora.modules.render.AntiBlindModule;
import su.aurora.modules.render.NoTotemAnimModule;
import su.aurora.modules.render.ItemPhysicsModule;
import su.aurora.modules.render.BlockOverlayModule;
import su.aurora.modules.render.BreakProgressModule;
import su.aurora.modules.render.TrajectoryModule;
import su.aurora.modules.render.ItemEspModule;
import su.aurora.modules.misc.AntiClearModule;
import su.aurora.modules.misc.ChatMentionModule;
import su.aurora.modules.misc.ChatSuffixModule;
import su.aurora.modules.hud.BetterDebugModule;
import su.aurora.modules.hud.MinimapModule;

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
        register(new FootprintsModule());
        register(new WaypointsModule());
        register(new SeasonsModule());
        register(new BlockShadowsModule());
        register(new DynamicLightsModule());
        register(new BlockPlaceModule());
        register(new CrystalColorModule());
        register(new AnimationsModule());
        register(new FriendsModule());
        register(new NoPauseModule());
        register(new ItemScrollerModule());
        register(new MainMenuSettingModule());
        register(new ZoomModule());
        register(new StorageEspModule());
        register(new BlockEspModule());
        register(new RadarModule());
        register(new ServerInfoModule());
        register(new StaffListModule());
        register(new ClockModule());
        register(new FpsGraphModule());
        register(new NoHurtCamModule());
        register(new NoFireOverlayModule());
        register(new AntiBlindModule());
        register(new NoTotemAnimModule());
        register(new ItemPhysicsModule());
        register(new BlockOverlayModule());
        register(new BreakProgressModule());
        register(new TrajectoryModule());
        register(new ItemEspModule());
        register(new AntiClearModule());
        register(new ChatMentionModule());
        register(new ChatSuffixModule());
        register(new MinimapModule());
        register(new BetterDebugModule());
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
