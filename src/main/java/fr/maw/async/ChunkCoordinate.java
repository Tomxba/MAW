package fr.maw.async;

/**
 * Compact representation of chunk coordinates with fast 64-bit packing.
 */
public record ChunkCoordinate(int chunkX, int chunkZ) {

    public static long pack(int chunkX, int chunkZ) {
        return ((long) chunkX << 32) | (chunkZ & 0xFFFFFFFFL);
    }

    public static int unpackX(long packed) {
        return (int) (packed >> 32);
    }

    public static int unpackZ(long packed) {
        return (int) packed;
    }

    public long pack() {
        return pack(chunkX, chunkZ);
    }

    public static ChunkCoordinate fromBlock(int blockX, int blockZ) {
        return new ChunkCoordinate(blockX >> 4, blockZ >> 4);
    }

    public static long packFromBlock(int blockX, int blockZ) {
        return pack(blockX >> 4, blockZ >> 4);
    }
}
