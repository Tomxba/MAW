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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Dispatcher that applies chunk block changes to a Minestom Instance.
 * Supports Time-Budgeting across ticks to ensure zero server TPS drop.
 */
public final class TickDispatcher {

    private final MawConfig config;
    /** How many dispatches are still applying their changes to each instance. */
    private final Map<Instance, AtomicInteger> pending = new ConcurrentHashMap<>();

    public TickDispatcher(MawConfig config) {
        this.config = Objects.requireNonNull(config, "config cannot be null");
    }

    /**
     * Whether changes are still being applied to {@code instance}, possibly over several ticks. While
     * they are, the instance is between two states: it should not be saved.
     */
    public boolean hasPending(Instance instance) {
        if (instance == null) {
            return false;
        }
        AtomicInteger count = pending.get(instance);
        return count != null && count.get() > 0;
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

        // Counted from here until the future completes, whichever way it does: a failure while applying
        // completes it exceptionally, so an instance is never left "busy" for good.
        if (instance != null) {
            pending.computeIfAbsent(instance, key -> new AtomicInteger()).incrementAndGet();
            future.whenComplete((result, failure) -> {
                AtomicInteger count = pending.get(instance);
                if (count != null && count.decrementAndGet() <= 0) {
                    pending.remove(instance, count);
                }
            });
        }

        try {
            long startTime = System.currentTimeMillis();
            long totalBlocks = queue.getTotalBlocks();
            int totalChunks = queue.getChunkCount();
            List<ChunkChangeQueue.ChunkEntry> chunkEntries = new ArrayList<>(queue.getChunkEntries());

            int maxChunksPerTick = config.maxChunksPerTick();

            // If maxChunksPerTick <= 0 or chunk count is small, apply in single batch
            if (maxChunksPerTick <= 0 || chunkEntries.size() <= maxChunksPerTick) {
                applyChunkBatch(instance, chunkEntries).whenComplete((applied, failure) -> {
                    if (failure != null) {
                        future.completeExceptionally(failure);
                        return;
                    }
                    try {
                        if (updatePhysics && boundaryBlocks != null && !boundaryBlocks.isEmpty()) {
                            SurroundingObjectHandler.handlePostBlockChanges(instance, boundaryBlocks, true);
                        }
                        long elapsed = System.currentTimeMillis() - startTime;
                        future.complete(new EditResult(totalBlocks, totalChunks, Math.max(1, elapsed)));
                    } catch (RuntimeException | Error e) {
                        future.completeExceptionally(e);
                    }
                });
                return future;
            }

            // Time-budgeted dispatch across multiple ticks
            dispatchProgressively(instance, chunkEntries, 0, maxChunksPerTick, boundaryBlocks, updatePhysics, startTime, totalBlocks, totalChunks, future);
            return future;
        } catch (RuntimeException | Error failure) {
            future.completeExceptionally(failure);
            throw failure;
        }
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

        // The next slice waits until this one is really written: applying a batch takes place on another
        // thread, so the result is not complete (nor the instance idle) until the batch says it is done.
        applyChunkBatch(instance, slice).whenComplete((applied, failure) -> {
            if (failure != null) {
                // A later tick has nobody to throw to: the failure goes to whoever waits for the result.
                future.completeExceptionally(failure);
                return;
            }
            try {
                if (nextIndex < entries.size()) {
                    // Schedule next tick
                    try {
                        MinecraftServer.getSchedulerManager().scheduleNextTick(() -> {
                            dispatchProgressively(instance, entries, nextIndex, maxChunksPerTick, boundaryBlocks, updatePhysics, startTime, totalBlocks, totalChunks, future);
                        });
                    } catch (Throwable t) {
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
            } catch (RuntimeException | Error e) {
                future.completeExceptionally(e);
            }
        });
    }

    /**
     * A batch that tells when it is done from the thread that finished it. Minestom's own callback waits for
     * the next tick of the instance, which would make the completion of an operation depend on the instance
     * being ticked (it is not, in a test, or on a server that has stopped ticking).
     */
    private static final class DirectBatch extends AbsoluteBlockBatch {
        void applyThenSignal(Instance instance, Runnable done) {
            apply(instance, batch -> done.run(), false);
        }
    }

    /**
     * Writes the blocks of these chunks. Minestom applies a batch on its own threads, so the returned future
     * completes when the batch says it is done (and fails if it cannot start, or does not answer in a minute:
     * an instance must never stay "busy" for good).
     */
    private CompletableFuture<Void> applyChunkBatch(Instance instance, List<ChunkChangeQueue.ChunkEntry> slice) {
        CompletableFuture<Void> applied = new CompletableFuture<>();
        try {
            DirectBatch batch = new DirectBatch();
            boolean any = false;
            for (ChunkChangeQueue.ChunkEntry entry : slice) {
                for (Map.Entry<Point, Block> blockEntry : entry.blocks().entrySet()) {
                    Point p = blockEntry.getKey();
                    batch.setBlock(p.blockX(), p.blockY(), p.blockZ(), blockEntry.getValue());
                    any = true;
                }
            }
            if (!any) {
                applied.complete(null);
                return applied;
            }
            batch.applyThenSignal(instance, () -> applied.complete(null));
        } catch (RuntimeException | Error failure) {
            applied.completeExceptionally(failure);
        }
        return applied.orTimeout(60, TimeUnit.SECONDS);
    }
}
