package fr.maw.async;

import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Loads the chunks an operation is about to read or write.
 * <p>
 * Minestom writes a block batch only into the chunks that are loaded: aimed at a chunk that is not loaded, the
 * batch is dropped with a warning and, worse, its callback is never called, so an edit over a large area — or
 * the undo of one — could end up changing nothing at all without failing. WorldEdit loads the chunks it
 * touches for this reason, and MAW does the same:
 * <ul>
 *   <li>a chunk is always loaded before it is <b>written</b>: a change a player asked for, and that the guard
 *       allowed, must happen or say why it could not;</li>
 *   <li>a chunk is loaded before it is <b>read</b> only when the world accepts chunks loaded on demand
 *       ({@link Instance#hasEnabledAutoChunkLoad()}). A world that refuses it (the Atelier: a map is exactly
 *       its build area) keeps the rule that a chunk that does not exist reads as air, so an operation can
 *       never make such a world grow.</li>
 * </ul>
 * Loading is done from the worker threads of {@link MawAsyncEngine}: every operation of MAW runs there.
 */
public final class ChunkPreloader {

    private ChunkPreloader() {
    }

    /**
     * The chunk at these coordinates, loading it when it is not in memory; {@code null} when it could not be
     * loaded (a world that has no such chunk, a world that refuses to create it).
     */
    public static @Nullable Chunk load(Instance instance, int chunkX, int chunkZ) {
        Chunk loaded = instance.getChunk(chunkX, chunkZ);
        if (loaded != null) {
            return loaded;
        }
        try {
            return instance.loadChunk(chunkX, chunkZ).join();
        } catch (RuntimeException failure) {
            return null;
        }
    }

    /**
     * Loads the chunks of these coordinates that are not in memory yet, and completes with the ones that could
     * not be loaded: an empty list when the whole set is there. The future never fails — a world that cannot
     * load one of these chunks is something to say, not an exception to swallow, and the caller says it.
     */
    public static CompletableFuture<List<ChunkCoordinate>> ensureLoaded(Instance instance, Collection<ChunkCoordinate> chunks) {
        List<ChunkCoordinate> wanted = new ArrayList<>();
        List<CompletableFuture<?>> loading = new ArrayList<>();
        for (ChunkCoordinate chunk : chunks) {
            if (instance.getChunk(chunk.chunkX(), chunk.chunkZ()) != null) {
                continue;
            }
            wanted.add(chunk);
            try {
                loading.add(instance.loadChunk(chunk.chunkX(), chunk.chunkZ()).exceptionally(failure -> null));
            } catch (RuntimeException failure) {
                // The load could not even be asked for (an instance that is closed, a loader that fails at
                // once): the chunk stays missing and the caller is told.
                loading.add(CompletableFuture.completedFuture(null));
            }
        }
        if (wanted.isEmpty()) {
            return CompletableFuture.completedFuture(List.of());
        }
        return CompletableFuture.allOf(loading.toArray(CompletableFuture[]::new)).thenApply(done -> {
            List<ChunkCoordinate> missing = new ArrayList<>();
            for (ChunkCoordinate chunk : wanted) {
                if (instance.getChunk(chunk.chunkX(), chunk.chunkZ()) == null) {
                    missing.add(chunk);
                }
            }
            return missing;
        });
    }
}
