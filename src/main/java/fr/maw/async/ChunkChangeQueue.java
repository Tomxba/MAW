package fr.maw.async;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.block.Block;

import java.util.*;

/**
 * High-performance queue that groups block changes by chunk coordinates.
 */
public final class ChunkChangeQueue {

    public static final class ChunkEntry {
        private final int chunkX;
        private final int chunkZ;
        private final Map<Point, Block> blocks = new HashMap<>();

        public ChunkEntry(int chunkX, int chunkZ) {
            this.chunkX = chunkX;
            this.chunkZ = chunkZ;
        }

        public int chunkX() {
            return chunkX;
        }

        public int chunkZ() {
            return chunkZ;
        }

        public Map<Point, Block> blocks() {
            return blocks;
        }

        public void put(int x, int y, int z, Block block) {
            blocks.put(new Vec(x, y, z), block);
        }

        public int size() {
            return blocks.size();
        }
    }

    private final Map<Long, ChunkEntry> chunks = new HashMap<>();
    private long totalBlocks = 0;

    public synchronized void setBlock(int x, int y, int z, Block block) {
        int chunkX = x >> 4;
        int chunkZ = z >> 4;
        long packed = ChunkCoordinate.pack(chunkX, chunkZ);

        ChunkEntry entry = chunks.computeIfAbsent(packed, k -> new ChunkEntry(chunkX, chunkZ));
        entry.put(x, y, z, block);
        totalBlocks++;
    }

    public synchronized Collection<ChunkEntry> getChunkEntries() {
        return new ArrayList<>(chunks.values());
    }

    public synchronized int getChunkCount() {
        return chunks.size();
    }

    public synchronized long getTotalBlocks() {
        return totalBlocks;
    }

    public synchronized boolean isEmpty() {
        return chunks.isEmpty();
    }

    public synchronized void clear() {
        chunks.clear();
        totalBlocks = 0;
    }
}
