package fr.maw;

import fr.maw.async.ChunkChangeQueue;
import fr.maw.async.ChunkCoordinate;
import fr.maw.async.EditResult;
import fr.maw.async.MawAsyncEngine;
import net.minestom.server.instance.block.Block;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Async Queue, Chunk Packing, and Async Engine Tests")
public class AsyncQueueAndChunkTest {

    @Test
    @DisplayName("Should correctly pack and unpack positive and negative chunk coordinates")
    public void should_pack_and_unpack_chunk_coordinates() {
        // Arrange
        int[][] testCoords = {
                {0, 0},
                {10, 20},
                {-1, -1},
                {-15, 30},
                {100, -200},
                {-32768, 32767}
        };

        for (int[] coord : testCoords) {
            int cx = coord[0];
            int cz = coord[1];

            // Act
            long packed = ChunkCoordinate.pack(cx, cz);
            int unpackedX = ChunkCoordinate.unpackX(packed);
            int unpackedZ = ChunkCoordinate.unpackZ(packed);

            // Assert
            assertEquals(cx, unpackedX, "X mismatch for (" + cx + ", " + cz + ")");
            assertEquals(cz, unpackedZ, "Z mismatch for (" + cx + ", " + cz + ")");
        }
    }

    @Test
    @DisplayName("Should derive chunk coordinate correctly from block coordinates")
    public void should_derive_chunk_from_block_coordinates() {
        // Positive coordinates
        ChunkCoordinate c1 = ChunkCoordinate.fromBlock(32, 47);
        assertEquals(2, c1.chunkX());
        assertEquals(2, c1.chunkZ());

        // Negative coordinates (-1 to -16 is chunk -1)
        ChunkCoordinate c2 = ChunkCoordinate.fromBlock(-1, -16);
        assertEquals(-1, c2.chunkX());
        assertEquals(-1, c2.chunkZ());

        // -17 is chunk -2
        ChunkCoordinate c3 = ChunkCoordinate.fromBlock(-17, -1);
        assertEquals(-2, c3.chunkX());
        assertEquals(-1, c3.chunkZ());
    }

    @Test
    @DisplayName("Should group blocks into chunk entries in ChunkChangeQueue")
    public void should_group_blocks_into_chunk_entries() {
        // Arrange
        ChunkChangeQueue queue = new ChunkChangeQueue();
        assertTrue(queue.isEmpty());
        assertEquals(0, queue.getChunkCount());
        assertEquals(0, queue.getTotalBlocks());

        // Act - set 2 blocks in Chunk (0, 0) and 1 block in Chunk (1, 1)
        queue.setBlock(0, 64, 0, Block.STONE);
        queue.setBlock(15, 64, 15, Block.DIRT);
        queue.setBlock(16, 64, 16, Block.GRASS_BLOCK); // chunk (1, 1)

        // Assert
        assertFalse(queue.isEmpty());
        assertEquals(2, queue.getChunkCount());
        assertEquals(3, queue.getTotalBlocks());

        // Verify entries
        var entries = queue.getChunkEntries();
        assertEquals(2, entries.size());

        // Clear
        queue.clear();
        assertTrue(queue.isEmpty());
        assertEquals(0, queue.getChunkCount());
        assertEquals(0, queue.getTotalBlocks());
    }

    @Test
    @DisplayName("Should compute blocks per second accurately in EditResult")
    public void should_compute_blocks_per_second_in_edit_result() {
        // Arrange
        EditResult result = new EditResult(10_000, 4, 100); // 10k blocks in 100ms = 100k/s

        // Assert
        assertEquals(10_000, result.blocksChanged());
        assertEquals(4, result.chunksAffected());
        assertEquals(100, result.elapsedMillis());
        assertEquals(100_000.0, result.getBlocksPerSecond(), 0.01);
        assertTrue(result.toString().contains("10000 blocks changed"));
    }

    @Test
    @DisplayName("Should run tasks asynchronously using Virtual Threads in MawAsyncEngine")
    public void should_run_tasks_asynchronously_on_virtual_threads() throws ExecutionException, InterruptedException {
        // Arrange
        MawAsyncEngine engine = new MawAsyncEngine(2);

        // Act
        CompletableFuture<Boolean> future = engine.supplyAsync(() -> {
            return Thread.currentThread().isVirtual();
        });

        // Assert - Virtual threads are enabled on Java 25
        Boolean isVirtual = future.get();
        assertTrue(isVirtual, "Expected task to run on Java 21+ Virtual Thread");

        engine.shutdown();
    }
}
