package fr.maw.async;

/**
 * Result statistics of an applied WorldEdit operation.
 */
public record EditResult(long blocksChanged, int chunksAffected, long elapsedMillis) {

    public double getBlocksPerSecond() {
        if (elapsedMillis <= 0) return blocksChanged * 1000.0;
        return (blocksChanged * 1000.0) / elapsedMillis;
    }

    @Override
    public String toString() {
        return String.format("%d blocks changed in %d chunks (%.2f ms, %,.0f blocks/sec)",
                blocksChanged, chunksAffected, (double) elapsedMillis, getBlocksPerSecond());
    }
}
