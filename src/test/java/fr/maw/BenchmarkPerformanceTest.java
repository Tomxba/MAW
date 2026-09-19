package fr.maw;

import fr.maw.async.AsyncEditSession;
import fr.maw.operation.SetOperation;
import fr.maw.pattern.SingleBlockPattern;
import fr.maw.selection.CuboidSelection;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.block.Block;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class BenchmarkPerformanceTest {

    @Test
    public void testHighVolumeOperation() {
        Block.Getter getter = (x, y, z, condition) -> Block.AIR;
        MawConfig config = MawConfig.builder().maxBlocksPerOperation(1_000_000).build();

        // 100 x 50 x 100 = 500,000 blocks
        CuboidSelection selection = new CuboidSelection(new Vec(0, 0, 0), new Vec(99, 49, 99));
        assertEquals(500_000, selection.getVolume());

        AsyncEditSession session = new AsyncEditSession(getter, config.maxBlocksPerOperation(), selection);

        // Warm-up run for JIT compilation
        CuboidSelection warmUp = new CuboidSelection(new Vec(0, 0, 0), new Vec(19, 19, 19)); // 8,000 blocks
        AsyncEditSession warmUpSession = new AsyncEditSession(getter, 100_000, warmUp);
        SetOperation.execute(warmUpSession, warmUp, new SingleBlockPattern(Block.STONE));

        long start = System.nanoTime();
        long count = SetOperation.execute(session, selection, new SingleBlockPattern(Block.STONE));
        long durationMs = (System.nanoTime() - start) / 1_000_000;

        assertEquals(500_000, count);
        assertEquals(500_000, session.getChangedCount());

        // 100x100 area spans (100/16 + 1) * (100/16 + 1) = 7 * 7 = 49 chunks
        assertEquals(49, session.getChangeQueue().getChunkCount());

        System.out.printf("Processed %,d blocks in %d ms (%,.0f blocks/sec)%n",
                count, durationMs, (count * 1000.0) / Math.max(1, durationMs));

        // Ensure performance is well under 2.5 seconds for 500,000 blocks (allowing for cold JVM/varying machine load)
        assertTrue(durationMs < 2500, "500k blocks should be processed in under 2.5 seconds, took: " + durationMs + "ms");
    }
}
