package fr.maw;

import fr.maw.async.AsyncEditSession;
import fr.maw.async.ChunkChangeQueue;
import fr.maw.async.EditResult;
import fr.maw.async.TickDispatcher;
import fr.maw.history.BlockChange;
import fr.maw.history.ChangeSet;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.ChunkLoader;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.block.Block;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Minestom writes a block batch only into the chunks that are loaded: aimed at a chunk that is not, the batch
 * is dropped with a warning and its callback is never called. An edit over a large area — or the undo of one —
 * therefore used to change nothing at all there, without failing and without saying anything.
 * <p>
 * These tests work on a world whose blocks live outside it, like the file of a map: a chunk that is not in
 * memory is not empty, it is only not loaded.
 */
@DisplayName("Chunks that are not loaded: an edit, its reads and its undo still land")
class ChunkLoadingTest {

    private static boolean initialized;

    @BeforeAll
    static void initMinestom() {
        if (!initialized) {
            MinecraftServer.init();
            initialized = true;
        }
    }

    /** A world whose blocks live outside it (its "disk"): a chunk is built from there when it is loaded. */
    private static final class StoredWorld implements ChunkLoader {

        private final Map<Vec, Block> disk = new ConcurrentHashMap<>();
        private final InstanceContainer instance;

        StoredWorld() {
            instance = MinecraftServer.getInstanceManager().createInstanceContainer();
            instance.setChunkLoader(this);
        }

        @Override
        public Chunk loadChunk(Instance world, int chunkX, int chunkZ) {
            Chunk chunk = world.getChunkSupplier().createChunk(world, chunkX, chunkZ);
            // A loader fills a chunk under its write lock, like the loader of Minestom does.
            chunk.lockWriteLock();
            try {
                for (Map.Entry<Vec, Block> stored : disk.entrySet()) {
                    Vec at = stored.getKey();
                    if ((at.blockX() >> 4) == chunkX && (at.blockZ() >> 4) == chunkZ) {
                        chunk.setBlock(at.blockX() & 15, at.blockY(), at.blockZ() & 15, stored.getValue());
                    }
                }
            } finally {
                chunk.unlockWriteLock();
            }
            return chunk;
        }

        @Override
        public void saveChunk(Chunk chunk) {
            // The world of this test is written by the test itself, through store(...).
        }

        void store(int x, int y, int z, Block block) {
            disk.put(new Vec(x, y, z), block);
        }

        /** What a save of the world would write: the block as it is in memory right now. */
        void storeWhatIsInMemory(int x, int y, int z) {
            store(x, y, z, instance.getBlock(x, y, z));
        }

        /** The map is closed: the chunk leaves memory, and only the disk keeps its blocks. */
        void forget(int chunkX, int chunkZ) {
            Chunk chunk = instance.getChunk(chunkX, chunkZ);
            if (chunk != null) {
                instance.unloadChunk(chunk);
            }
        }
    }

    @Test
    @DisplayName("An edit writes into a chunk that the world has not loaded")
    void should_write_into_a_chunk_that_is_not_loaded() throws Exception {
        StoredWorld world = new StoredWorld();
        TickDispatcher dispatcher = new TickDispatcher(MawConfig.defaultConfig());
        assertNull(world.instance.getChunk(3, 1), "the chunk is not in memory");

        AsyncEditSession session = new AsyncEditSession(world.instance, 1_000, null);
        session.setBlock(3 * 16 + 2, 64, 16 + 5, Block.STONE);

        EditResult result = waitFor(session.commit(dispatcher, false, false), world.instance);

        assertEquals(1, result.blocksChanged());
        assertNotNull(world.instance.getChunk(3, 1), "MAW loaded the chunk to write it");
        assertEquals(Block.STONE, world.instance.getBlock(3 * 16 + 2, 64, 16 + 5));
    }

    @Test
    @DisplayName("An operation reads the real block of a chunk that is not loaded, not air")
    void should_read_a_chunk_that_is_not_loaded() {
        StoredWorld world = new StoredWorld();
        world.store(3 * 16 + 2, 64, 5, Block.STONE);
        assertNull(world.instance.getChunk(3, 0), "the chunk is not in memory");

        AsyncEditSession session = new AsyncEditSession(world.instance, 100, null);

        // A //replace stone dirt (or any mask) has to see the stone to find something to change.
        assertEquals(Block.STONE, session.getBlock(3 * 16 + 2, 64, 5));
    }

    @Test
    @DisplayName("A world that refuses chunks loaded on demand is left alone, and reads as air")
    void should_not_load_a_chunk_in_a_world_that_refuses_it() {
        StoredWorld world = new StoredWorld();
        world.store(3 * 16 + 2, 64, 5, Block.STONE);
        world.instance.enableAutoChunkLoad(false);

        AsyncEditSession session = new AsyncEditSession(world.instance, 100, null);

        assertEquals(Block.AIR, session.getBlock(3 * 16 + 2, 64, 5), "nothing is loaded for a read there");
        assertNull(world.instance.getChunk(3, 0), "the world did not grow");
    }

    @Test
    @DisplayName("The undo of an edit lands even when the chunk left memory since")
    void should_undo_in_a_chunk_that_is_not_loaded() throws Exception {
        StoredWorld world = new StoredWorld();
        TickDispatcher dispatcher = new TickDispatcher(MawConfig.defaultConfig());
        int x = 3 * 16 + 2;
        int y = 64;
        int z = 16 + 5;

        // The edit: dirt where the stored world has stone.
        world.store(x, y, z, Block.STONE);
        AsyncEditSession session = new AsyncEditSession(world.instance, 1_000, null);
        session.setBlock(x, y, z, Block.DIRT);
        waitFor(session.commit(dispatcher, false, false), world.instance);
        ChangeSet edit = session.createChangeSet();
        assertEquals(Block.DIRT, world.instance.getBlock(x, y, z));

        // The map is saved and closed: the chunk leaves memory, the disk keeps what the edit made.
        world.storeWhatIsInMemory(x, y, z);
        world.forget(3, 1);
        assertNull(world.instance.getChunk(3, 1), "the chunk is not in memory anymore");

        // //undo: the changes are already known, so they are written as they are, without reading the world.
        ChunkChangeQueue queue = new ChunkChangeQueue();
        for (BlockChange change : edit.inverse().getChanges()) {
            queue.setBlock(change.x(), change.y(), change.z(), change.newBlock());
        }
        waitFor(dispatcher.dispatch(world.instance, queue, Set.of(), null, false, false), world.instance);

        assertEquals(Block.STONE, world.instance.getBlock(x, y, z), "the undo is in the world");
    }

    /**
     * Waits for an operation, ticking the world while it runs: Minestom reports a written batch on the next tick
     * of its instance, so a test has to tick it, exactly as a server does.
     */
    private static <T> T waitFor(CompletableFuture<T> future, InstanceContainer world) throws Exception {
        for (int i = 0; i < 400 && !future.isDone(); i++) {
            world.tick(System.currentTimeMillis());
            Thread.sleep(5);
        }
        return future.get(5, TimeUnit.SECONDS);
    }
}
