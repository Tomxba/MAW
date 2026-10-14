package fr.maw.async;

import java.util.List;

/**
 * Thrown when the world will not load a chunk an operation has changes for.
 * <p>
 * The operation fails, loudly, instead of writing the chunks it could and saying nothing about the others: a
 * WorldEdit that reports success over a change it did not make is worse than one that says it could not.
 */
public final class ChunkNotLoadedException extends RuntimeException {

    /** How many chunk coordinates of the message are shown before it is cut. */
    private static final int SHOWN = 6;

    public ChunkNotLoadedException(List<ChunkCoordinate> chunks, long blocks) {
        super(buildMessage(chunks, blocks));
    }

    private static String buildMessage(List<ChunkCoordinate> chunks, long blocks) {
        StringBuilder where = new StringBuilder();
        int shown = Math.min(chunks.size(), SHOWN);
        for (int i = 0; i < shown; i++) {
            where.append(i == 0 ? "" : ", ")
                    .append('(').append(chunks.get(i).chunkX()).append(", ").append(chunks.get(i).chunkZ()).append(')');
        }
        if (chunks.size() > shown) {
            where.append(", ...");
        }
        return String.format("%,d block(s) were not changed: the world cannot load %d chunk(s) [%s]",
                blocks, chunks.size(), where);
    }
}
