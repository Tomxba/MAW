package fr.maw.tool.brush;

import fr.maw.MawConfig;
import fr.maw.async.AsyncEditSession;
import fr.maw.async.MawAsyncEngine;
import fr.maw.async.TickDispatcher;
import fr.maw.command.CommandHelper;
import fr.maw.pattern.Mask;
import fr.maw.selection.CuboidSelection;
import fr.maw.session.PlayerSession;
import fr.maw.session.SessionManager;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;

import java.util.HashMap;
import java.util.Map;

public class SmoothBrush extends Brush {

    private int iterations;

    public SmoothBrush(
            MawConfig config,
            SessionManager sessionManager,
            MawAsyncEngine asyncEngine,
            TickDispatcher dispatcher,
            int radius,
            int iterations
    ) {
        super(config, sessionManager, asyncEngine, dispatcher, null, radius, false);
        this.iterations = Math.max(1, Math.min(iterations, 10));
    }

    public int getIterations() {
        return iterations;
    }

    public void setIterations(int iterations) {
        this.iterations = Math.max(1, Math.min(iterations, 10));
    }

    @Override
    protected void applyBrush(Player player, Instance instance, Point targetBlock, BlockFace face) {
        PlayerSession session = sessionManager.getSession(player);
        int cx = targetBlock.blockX();
        int cy = targetBlock.blockY();
        int cz = targetBlock.blockZ();

        int minY = Math.max(-64, cy - radius * 2);
        int maxY = Math.min(320, cy + radius * 2);

        Point min = new Vec(cx - radius, minY, cz - radius);
        Point max = new Vec(cx + radius, maxY, cz + radius);
        CuboidSelection selection = new CuboidSelection(min, max);

        Mask brushMask = this.mask != null ? this.mask : session.getBrushMask();
        Mask globalMask = session.getGlobalMask();

        asyncEngine.runAsync(() -> {
            try {
                AsyncEditSession editSession = CommandHelper.newSession(
                        config, player, instance, config.maxBlocksPerOperation(), selection
                );

                // 1. Build heightmap
                Map<Long, Integer> heightmap = new HashMap<>();
                Map<Long, Block> topBlocks = new HashMap<>();

                for (int x = cx - radius; x <= cx + radius; x++) {
                    for (int z = cz - radius; z <= cz + radius; z++) {
                        long key = (((long) x) << 32) | (z & 0xFFFFFFFFL);
                        int highestY = minY;
                        Block surfaceBlock = Block.DIRT;

                        for (int y = maxY; y >= minY; y--) {
                            Block b = editSession.getBlock(x, y, z);
                            if (b != null && !b.air()) {
                                highestY = y;
                                surfaceBlock = b;
                                break;
                            }
                        }
                        heightmap.put(key, highestY);
                        topBlocks.put(key, surfaceBlock);
                    }
                }

                // 2. Perform smoothing iterations
                double rSq = (radius + 0.5) * (radius + 0.5);
                for (int it = 0; it < iterations; it++) {
                    Map<Long, Integer> nextMap = new HashMap<>(heightmap);
                    for (int x = cx - radius; x <= cx + radius; x++) {
                        for (int z = cz - radius; z <= cz + radius; z++) {
                            double distSq = (x - cx) * (x - cx) + (z - cz) * (z - cz);
                            if (distSq > rSq) continue;

                            int sum = 0;
                            int count = 0;
                            for (int dx = -1; dx <= 1; dx++) {
                                for (int dz = -1; dz <= 1; dz++) {
                                    long nKey = (((long) (x + dx)) << 32) | ((z + dz) & 0xFFFFFFFFL);
                                    Integer h = heightmap.get(nKey);
                                    if (h != null) {
                                        sum += h;
                                        count++;
                                    }
                                }
                            }
                            if (count > 0) {
                                nextMap.put((((long) x) << 32) | (z & 0xFFFFFFFFL), Math.round((float) sum / count));
                            }
                        }
                    }
                    heightmap = nextMap;
                }

                // 3. Apply smoothed heightmap
                for (int x = cx - radius; x <= cx + radius; x++) {
                    for (int z = cz - radius; z <= cz + radius; z++) {
                        double distSq = (x - cx) * (x - cx) + (z - cz) * (z - cz);
                        if (distSq > rSq) continue;

                        long key = (((long) x) << 32) | (z & 0xFFFFFFFFL);
                        int targetY = heightmap.getOrDefault(key, cy);
                        Block topBlock = topBlocks.getOrDefault(key, Block.GRASS_BLOCK);

                        // Fill up to targetY or clear above targetY
                        for (int y = minY; y <= maxY; y++) {
                            Block existing = editSession.getBlock(x, y, z);
                            if (brushMask != null && !brushMask.test(x, y, z, existing)) continue;
                            if (globalMask != null && !globalMask.test(x, y, z, existing)) continue;

                            if (y <= targetY) {
                                if (existing.air()) {
                                    editSession.setBlock(x, y, z, y == targetY ? topBlock : Block.DIRT);
                                }
                            } else {
                                if (!existing.air()) {
                                    editSession.setBlock(x, y, z, Block.AIR);
                                }
                            }
                        }
                    }
                }

                editSession.commit(dispatcher, session.isUpdatePhysics(), session.isManageEntities()).thenAccept(result -> {
                    session.getHistoryManager().record(editSession.createChangeSet());
                });
            } catch (Exception e) {
                CommandHelper.sendError(player, "Smooth brush error: " + e.getMessage());
            }
        });
    }
}
