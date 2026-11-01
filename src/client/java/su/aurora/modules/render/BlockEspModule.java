package su.aurora.modules.render;

import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.phys.AABB;
import su.aurora.core.Category;
import su.aurora.core.Module;
import su.aurora.setting.BooleanSetting;
import su.aurora.setting.ColorSetting;
import su.aurora.setting.NumberSetting;
import su.aurora.util.ColorUtil;
import su.aurora.util.Render3D;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * BlockESP — поиск нужных блоков через стены: спавнеры, руды, древние обломки.
 * Секции чанков отсекаются через maybeHas, поэтому скан дешёвый.
 */
public class BlockEspModule extends Module {
    private final NumberSetting chunks = addNumber("Радиус (чанки)", 3, 1, 8, 1);
    private final BooleanSetting spawners = addBoolean("Спавнеры", true);
    private final BooleanSetting diamonds = addBoolean("Алмазы", true);
    private final BooleanSetting debris = addBoolean("Древние обломки", true);
    private final BooleanSetting emeralds = addBoolean("Изумруды", false);
    private final BooleanSetting gold = addBoolean("Золото", false);
    private final NumberSetting limit = addNumber("Максимум блоков", 200, 10, 1000, 10);
    private final BooleanSetting fill = addBoolean("Заливка", true);
    private final ColorSetting accent = addColor("Цвет", 0xFF7C5CFF);

    private final Set<BlockPos> cache = new HashSet<>();
    private int cooldown;

    public BlockEspModule() {
        super("BlockESP", "Подсветка спавнеров, руд и древних обломков", Category.RENDER);
    }

    @Override
    public void onDisable() {
        cache.clear();
    }

    private boolean isTarget(BlockState state) {
        Block block = state.getBlock();
        if (spawners.get() && (block == Blocks.SPAWNER || block == Blocks.TRIAL_SPAWNER)) {
            return true;
        }
        if (diamonds.get() && (block == Blocks.DIAMOND_ORE || block == Blocks.DEEPSLATE_DIAMOND_ORE)) {
            return true;
        }
        if (debris.get() && block == Blocks.ANCIENT_DEBRIS) {
            return true;
        }
        if (emeralds.get() && (block == Blocks.EMERALD_ORE || block == Blocks.DEEPSLATE_EMERALD_ORE)) {
            return true;
        }
        return gold.get() && (block == Blocks.GOLD_ORE || block == Blocks.DEEPSLATE_GOLD_ORE
                || block == Blocks.NETHER_GOLD_ORE);
    }

    private void scan() {
        Minecraft client = Minecraft.getInstance();
        if (client.level == null || client.player == null) {
            return;
        }
        int radius = chunks.getInt();
        int max = limit.getInt();
        int centerX = client.player.chunkPosition().x;
        int centerZ = client.player.chunkPosition().z;
        int minY = client.level.getMinY();
        Set<BlockPos> found = new HashSet<>();

        for (int dx = -radius; dx <= radius && found.size() < max; dx++) {
            for (int dz = -radius; dz <= radius && found.size() < max; dz++) {
                LevelChunk chunk = client.level.getChunk(centerX + dx, centerZ + dz);
                if (chunk == null) {
                    continue;
                }
                LevelChunkSection[] sections = chunk.getSections();
                for (int index = 0; index < sections.length; index++) {
                    LevelChunkSection section = sections[index];
                    if (section == null || section.hasOnlyAir() || !section.maybeHas(this::isTarget)) {
                        continue;
                    }
                    for (int x = 0; x < 16; x++) {
                        for (int y = 0; y < 16; y++) {
                            for (int z = 0; z < 16; z++) {
                                if (!isTarget(section.getBlockState(x, y, z))) {
                                    continue;
                                }
                                BlockPos pos = new BlockPos((centerX + dx) * 16 + x, minY + index * 16 + y,
                                        (centerZ + dz) * 16 + z);
                                found.add(pos);
                                if (found.size() >= max) {
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }
        cache.clear();
        cache.addAll(found);
    }

    @Override
    public void onTick() {
        if (++cooldown < 20) {
            return;
        }
        cooldown = 0;
        scan();
    }

    @Override
    public void onWorldRender(WorldRenderContext context) {
        if (cache.isEmpty()) {
            return;
        }
        int tint = accent.get();
        List<BlockPos> positions = new ArrayList<>(cache);
        for (BlockPos pos : positions) {
            AABB box = new AABB(pos).inflate(0.01);
            Render3D.lines(buffer -> buffer.box(box, tint, 2.0f));
            if (fill.get()) {
                Render3D.quads(buffer -> buffer.box(box, ColorUtil.withAlpha(tint, 40)));
            }
        }
    }

    public int foundCount() {
        return cache.size();
    }
}
