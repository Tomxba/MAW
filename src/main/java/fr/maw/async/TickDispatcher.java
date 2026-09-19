package fr.maw.async;

import fr.maw.MawConfig;
import fr.maw.history.ChangeSet;
import fr.maw.objects.SurroundingObjectHandler;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.batch.AbsoluteBlockBatch;
import net.minestom.server.instance.block.Block;

import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * Dispatcher that applies chunk block changes to a Minestom Instance.
 * Supports Time-Budgeting across ticks to ensure zero server TPS drop.
 */
public final class TickDispatcher {

    private final MawConfig config;

    public TickDispatcher(MawConfig config) {
        this.config = Objects.requireNonNull(config, "config cannot be null");
    }

    /**
     * Dispatches the queued chunk changes to the given instance.
     */
    public CompletableFuture<EditResult> dispatch(
            Instance instance,
            ChunkChangeQueue queue,
            Set<Point> boundaryBlocks,
            ChangeSet changeSet,
            boolean updatePhysics,
            boolean manageEntities
    ) {
        CompletableFuture<EditResult> future = new CompletableFuture<>();

        if (queue == null || queue.isEmpty()) {
            future.complete(new EditResult(0, 0, 0));
            return future;
        }

        long startTime = System.currentTimeMillis();
        long totalBlocks = queue.getTotalBlocks();
        int totalChunks = queue.getChunkCount();
        List<ChunkChangeQueue.ChunkEntry> chunkEntries = new ArrayList<>(queue.getChunkEntries());

        int maxChunksPerTick = config.maxChunksPerTick();

        // If maxChunksPerTick <= 0 or chunk count is small, apply in single batch
        if (maxChunksPerTick <= 0 || chunkEntries.size() <= maxChunksPerTick) {
            applyChunkBatch(instance, chunkEntries);

            if (updatePhysics && boundaryBlocks != null && !boundaryBlocks.isEmpty()) {
                SurroundingObjectHandler.handlePostBlockChanges(instance, boundaryBlocks, true);
            }

            long elapsed = System.currentTimeMillis() - startTime;
            future.complete(new EditResult(totalBlocks, totalChunks, Math.max(1, elapsed)));
            return future;
        }

        // Time-budgeted dispatch across multiple ticks
        dispatchProgressively(instance, chunkEntries, 0, maxChunksPerTick, boundaryBlocks, updatePhysics, startTime, totalBlocks, totalChunks, future);
        return future;
    }

    private void dispatchProgressively(
            Instance instance,
            List<ChunkChangeQueue.ChunkEntry> entries,
            int currentIndex,
            int maxChunksPerTick,
            Set<Point> boundaryBlocks,
            boolean updatePhysics,
            long startTime,
            long totalBlocks,
            int totalChunks,
            CompletableFuture<EditResult> future
    ) {
        int nextIndex = Math.min(currentIndex + maxChunksPerTick, entries.size());
        List<ChunkChangeQueue.ChunkEntry> slice = entries.subList(currentIndex, nextIndex);

        applyChunkBatch(instance, slice);

        if (nextIndex < entries.size()) {
            // Schedule next tick
            try {
                MinecraftServer.getSchedulerManager().scheduleNextTick(() -> {
                    dispatchProgressively(instance, entries, nextIndex, maxChunksPerTick, boundaryBlocks, updatePhysics, startTime, totalBlocks, totalChunks, future);
                });
            } catch (Exception e) {
                // If scheduler is unavailable (e.g. unit tests without full server), continue synchronously
                dispatchProgressively(instance, entries, nextIndex, maxChunksPerTick, boundaryBlocks, updatePhysics, startTime, totalBlocks, totalChunks, future);
            }
        } else {
            // Completed all chunks!
            if (updatePhysics && boundaryBlocks != null && !boundaryBlocks.isEmpty()) {
                SurroundingObjectHandler.handlePostBlockChanges(instance, boundaryBlocks, true);
            }

            long elapsed = System.currentTimeMillis() - startTime;
            future.complete(new EditResult(totalBlocks, totalChunks, Math.max(1, elapsed)));
        }
    }

    private void applyChunkBatch(Instance instance, List<ChunkChangeQueue.ChunkEntry> slice) {
        AbsoluteBlockBatch batch = new AbsoluteBlockBatch();
        for (ChunkChangeQueue.ChunkEntry entry : slice) {
            for (Map.Entry<Point, Block> blockEntry : entry.blocks().entrySet()) {
                Point p = blockEntry.getKey();
                batch.setBlock(p.blockX(), p.blockY(), p.blockZ(), blockEntry.getValue());
            }
        }
        batch.apply(instance, null);
    }
}
