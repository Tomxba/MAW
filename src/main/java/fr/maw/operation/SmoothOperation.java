package fr.maw.operation;

import fr.maw.async.AsyncEditSession;
import fr.maw.selection.Selection;
import net.minestom.server.instance.block.Block;

import java.util.HashMap;
import java.util.Map;

public final class SmoothOperation {

    private SmoothOperation() {}

    public static long execute(AsyncEditSession session, Selection selection, int iterations) {
        int minX = selection.getMinX();
        int maxX = selection.getMaxX();
        int minY = selection.getMinY();
        int maxY = selection.getMaxY();
        int minZ = selection.getMinZ();
        int maxZ = selection.getMaxZ();

        Map<Long, Integer> heightmap = new HashMap<>();
        Map<Long, Block> surfaceBlocks = new HashMap<>();

        // 1. Scan heightmap
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                long key = (((long) x) << 32) | (z & 0xFFFFFFFFL);
                int h = minY;
                Block surf = Block.DIRT;

                for (int y = maxY; y >= minY; y--) {
                    Block b = session.getBlock(x, y, z);
                    if (b != null && !b.isAir()) {
                        h = y;
                        surf = b;
                        break;
                    }
                }
                heightmap.put(key, h);
                surfaceBlocks.put(key, surf);
            }
        }

        // 2. Smooth iterations
        for (int it = 0; it < iterations; it++) {
            Map<Long, Integer> next = new HashMap<>(heightmap);
            for (int x = minX; x <= maxX; x++) {
                for (int z = minZ; z <= maxZ; z++) {
                    int sum = 0;
                    int count = 0;
                    for (int dx = -1; dx <= 1; dx++) {
                        for (int dz = -1; dz <= 1; dz++) {
                            long nKey = (((long) (x + dx)) << 32) | ((z + dz) & 0xFFFFFFFFL);
                            Integer val = heightmap.get(nKey);
                            if (val != null) {
                                sum += val;
                                count++;
                            }
                        }
                    }
                    if (count > 0) {
                        next.put((((long) x) << 32) | (z & 0xFFFFFFFFL), Math.round((float) sum / count));
                    }
                }
            }
            heightmap = next;
        }

        // 3. Write changes
        long changed = 0;
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                long key = (((long) x) << 32) | (z & 0xFFFFFFFFL);
                int targetY = heightmap.getOrDefault(key, minY);
                Block top = surfaceBlocks.getOrDefault(key, Block.GRASS_BLOCK);

                for (int y = minY; y <= maxY; y++) {
                    Block existing = session.getBlock(x, y, z);
                    if (y <= targetY) {
                        if (existing.isAir()) {
                            session.setBlock(x, y, z, y == targetY ? top : Block.DIRT);
                            changed++;
                        }
                    } else {
                        if (!existing.isAir()) {
                            session.setBlock(x, y, z, Block.AIR);
                            changed++;
                        }
                    }
                }
            }
        }

        return changed;
    }
}
