package su.aurora.modules.render;

import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import su.aurora.core.Category;
import su.aurora.core.Module;
import su.aurora.setting.BooleanSetting;
import su.aurora.setting.ColorSetting;
import su.aurora.setting.NumberSetting;
import su.aurora.util.ColorUtil;
import su.aurora.util.Render3D;

import java.util.HashMap;
import java.util.Map;

/**
 * StorageESP — подсветка сундуков, бочек, шалкеров, печей, воронок и спавнеров через стены.
 * Сканируются блок-сущности загруженных чанков: это дешевле, чем перебор блоков.
 */
public class StorageEspModule extends Module {
    private final NumberSetting chunks = addNumber("Радиус (чанки)", 4, 1, 10, 1);
    private final BooleanSetting containers = addBoolean("Контейнеры", true);
    private final BooleanSetting spawners = addBoolean("Спавнеры", true);
    private final BooleanSetting machines = addBoolean("Печи и механизмы", true);
    private final BooleanSetting beacons = addBoolean("Маяки", true);
    private final BooleanSetting fill = addBoolean("Заливка", true);
    private final BooleanSetting labels = addBoolean("Подписи", false);
    private final ColorSetting color = addColor("Цвет", 0xFFFFA53A);

    public StorageEspModule() {
        super("StorageESP", "Подсветка сундуков, печей и спавнеров через стены", Category.RENDER);
    }

    private boolean isContainer(BlockEntityType<?> type) {
        return type == BlockEntityType.CHEST || type == BlockEntityType.TRAPPED_CHEST
                || type == BlockEntityType.ENDER_CHEST || type == BlockEntityType.BARREL
                || type == BlockEntityType.SHULKER_BOX;
    }

    private boolean isSpawner(BlockEntityType<?> type) {
        return type == BlockEntityType.MOB_SPAWNER || type == BlockEntityType.TRIAL_SPAWNER;
    }

    private boolean isMachine(BlockEntityType<?> type) {
        return type == BlockEntityType.FURNACE || type == BlockEntityType.BLAST_FURNACE
                || type == BlockEntityType.SMOKER || type == BlockEntityType.HOPPER
                || type == BlockEntityType.DISPENSER || type == BlockEntityType.DROPPER
                || type == BlockEntityType.BREWING_STAND;
    }

    @Override
    public void onWorldRender(WorldRenderContext context) {
        Minecraft client = Minecraft.getInstance();
        if (client.level == null || client.player == null) {
            return;
        }
        int radius = chunks.getInt();
        int centerX = client.player.chunkPosition().x;
        int centerZ = client.player.chunkPosition().z;
        double maxDistance = radius * 16.0;

        Map<BlockPos, Integer> found = new HashMap<>();
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                LevelChunk chunk = client.level.getChunk(centerX + dx, centerZ + dz);
                if (chunk == null) {
                    continue;
                }
                for (Map.Entry<BlockPos, BlockEntity> entry : chunk.getBlockEntities().entrySet()) {
                    BlockEntityType<?> type = entry.getValue().getType();
                    int typeColor;
                    if (containers.get() && isContainer(type)) {
                        typeColor = color.get();
                    } else if (spawners.get() && isSpawner(type)) {
                        typeColor = 0xFFFF4D4D;
                    } else if (machines.get() && isMachine(type)) {
                        typeColor = 0xFF9AA4B2;
                    } else if (beacons.get() && type == BlockEntityType.BEACON) {
                        typeColor = 0xFF7CF7F0;
                    } else {
                        continue;
                    }
                    BlockPos pos = entry.getKey();
                    if (pos.distToCenterSqr(client.player.position()) > maxDistance * maxDistance) {
                        continue;
                    }
                    found.put(pos.immutable(), typeColor);
                }
            }
        }
        if (found.isEmpty()) {
            return;
        }

        for (Map.Entry<BlockPos, Integer> entry : found.entrySet()) {
            BlockPos pos = entry.getKey();
            int tint = entry.getValue();
            AABB box = new AABB(pos).inflate(0.02);
            Render3D.lines(buffer -> buffer.box(box, tint, 2.0f));
            if (fill.get()) {
                Render3D.quads(buffer -> buffer.box(box, ColorUtil.withAlpha(tint, 28)));
            }
            if (labels.get()) {
                String text = pos.getX() + " " + pos.getY() + " " + pos.getZ();
                Vec3 labelPos = new Vec3(pos.getX() + 0.5, pos.getY() + 1.2, pos.getZ() + 0.5);
                Render3D.lines(buffer -> buffer.cross(labelPos, 0.12, tint, 1.4f));
                if (client.player.distanceToSqr(labelPos) < 24 * 24) {
                    Render3D.quads(buffer -> buffer.disc(labelPos, 0.06, ColorUtil.withAlpha(tint, 200), 8));
                }
                if (false) {
                    su.aurora.Aurora.LOG.debug(text);
                }
            }
        }
    }
}
