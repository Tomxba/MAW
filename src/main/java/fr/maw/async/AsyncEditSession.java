package fr.maw.async;

import fr.maw.history.BlockChange;
import fr.maw.history.ChangeSet;
import fr.maw.selection.Selection;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;

import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * High performance editing session inspired by FastAsyncWorldEdit (FAWE).
 * Records block changes, enforces limits, sorts blocks by chunk and prepares ChangeSets.
 */
public final class AsyncEditSession {

    public static class MaxBlocksExceededException extends RuntimeException {
        public MaxBlocksExceededException(String message) {
            super(message);
        }
    }

    private final Instance instance;
    private final Block.Getter blockGetter;
    private final int maxBlocks;
    private final ChunkChangeQueue changeQueue = new ChunkChangeQueue();
    private final List<BlockChange> recordedChanges = new ArrayList<>();
    private final Set<Point> boundaryBlocks = new HashSet<>();
    private final Selection bounds;

    private long changedCount = 0;
    private boolean closed = false;

    public AsyncEditSession(Block.Getter blockGetter, int maxBlocks, Selection bounds) {
        this.blockGetter = Objects.requireNonNull(blockGetter, "blockGetter cannot be null");
        this.instance = (blockGetter instanceof Instance inst) ? inst : null;
        this.maxBlocks = maxBlocks > 0 ? maxBlocks : Integer.MAX_VALUE;
        this.bounds = bounds;
    }

    public AsyncEditSession(Instance instance, int maxBlocks, Selection bounds) {
        this((Block.Getter) instance, maxBlocks, bounds);
    }

    public AsyncEditSession(Block.Getter blockGetter, int maxBlocks) {
        this(blockGetter, maxBlocks, null);
    }

    public AsyncEditSession(Instance instance, int maxBlocks) {
        this((Block.Getter) instance, maxBlocks, null);
    }

    public Instance getInstance() {
        return instance;
    }

    public Block getBlock(int x, int y, int z) {
        return blockGetter.getBlock(x, y, z);
    }

    public Block getBlock(Point p) {
        return getBlock(p.blockX(), p.blockY(), p.blockZ());
    }

    /**
     * Sets a block at the given position. Records previous state for Undo.
     */
    public synchronized void setBlock(int x, int y, int z, Block newBlock) {
        if (closed) {
            throw new IllegalStateException("EditSession is already closed");
        }

        if (changedCount >= maxBlocks) {
            throw new MaxBlocksExceededException("Block limit of " + maxBlocks + " exceeded!");
        }

        Block previous = getBlock(x, y, z);
        if (previous == null) {
            previous = Block.AIR;
        }
        if (previous.equals(newBlock)) {
            // No change needed
            return;
        }

        changeQueue.setBlock(x, y, z, newBlock);
        recordedChanges.add(new BlockChange(x, y, z, previous, newBlock));
        changedCount++;

        // Track boundary blocks if selection bounds are known
        if (bounds != null && isBoundary(x, y, z)) {
            boundaryBlocks.add(new Vec(x, y, z));
        }
    }

    public void setBlock(Point p, Block newBlock) {
        setBlock(p.blockX(), p.blockY(), p.blockZ(), newBlock);
    }

    private boolean isBoundary(int x, int y, int z) {
        return x == bounds.getMinX() || x == bounds.getMaxX() ||
               y == bounds.getMinY() || y == bounds.getMaxY() ||
               z == bounds.getMinZ() || z == bounds.getMaxZ();
    }

    public synchronized ChangeSet createChangeSet() {
        return new ChangeSet(recordedChanges);
    }

    public synchronized ChunkChangeQueue getChangeQueue() {
        return changeQueue;
    }

    public synchronized Set<Point> getBoundaryBlocks() {
        return boundaryBlocks;
    }

    public synchronized long getChangedCount() {
        return changedCount;
    }

    public synchronized void close() {
        closed = true;
    }

    /**
     * Commits all buffered chunk changes to the instance through the TickDispatcher.
     */
    public CompletableFuture<EditResult> commit(TickDispatcher dispatcher, boolean updatePhysics, boolean manageEntities) {
        close();
        return dispatcher.dispatch(instance, changeQueue, boundaryBlocks, createChangeSet(), updatePhysics, manageEntities);
    }
}
