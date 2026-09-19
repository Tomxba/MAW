package fr.maw;

import fr.maw.async.AsyncEditSession;
import fr.maw.operation.*;
import fr.maw.pattern.Mask;
import fr.maw.pattern.Masks;
import fr.maw.pattern.SingleBlockPattern;
import fr.maw.selection.CuboidSelection;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.block.Block;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class OperationsUnitTest {

    @Test
    public void testSetOperationCount() {
        Block.Getter getter = (x, y, z, condition) -> Block.AIR;
        MawConfig config = MawConfig.defaultConfig();
        CuboidSelection selection = new CuboidSelection(new Vec(0, 0, 0), new Vec(2, 2, 2)); // 27 blocks
        AsyncEditSession session = new AsyncEditSession(getter, config.maxBlocksPerOperation(), selection);

        long count = SetOperation.execute(session, selection, new SingleBlockPattern(Block.STONE));
        assertEquals(27, count);
        assertEquals(27, session.getChangedCount());
        assertEquals(1, session.getChangeQueue().getChunkCount());
    }

    @Test
    public void testReplaceOperation() {
        Block.Getter getter = (x, y, z, condition) -> (x % 2 == 0) ? Block.STONE : Block.DIRT;
        MawConfig config = MawConfig.defaultConfig();
        CuboidSelection selection = new CuboidSelection(new Vec(0, 0, 0), new Vec(2, 2, 2)); // 27 blocks
        AsyncEditSession session = new AsyncEditSession(getter, config.maxBlocksPerOperation(), selection);

        Mask stoneMask = Masks.ofBlock(Block.STONE);
        long count = ReplaceOperation.execute(session, selection, stoneMask, new SingleBlockPattern(Block.GOLD_BLOCK));
        // x in [0, 1, 2]: x=0 (9 blocks), x=2 (9 blocks), total 18 blocks
        assertEquals(18, count);
        assertEquals(18, session.getChangedCount());
    }

    @Test
    public void testWallsOperationCount() {
        Block.Getter getter = (x, y, z, condition) -> Block.AIR;
        MawConfig config = MawConfig.defaultConfig();
        // 3x3x3 cuboid:
        // y=0: 8 perimeter blocks
        // y=1: 8 perimeter blocks
        // y=2: 8 perimeter blocks
        // total = 24 blocks
        CuboidSelection selection = new CuboidSelection(new Vec(0, 0, 0), new Vec(2, 2, 2));
        AsyncEditSession session = new AsyncEditSession(getter, config.maxBlocksPerOperation(), selection);

        long count = WallsOperation.execute(session, selection, new SingleBlockPattern(Block.STONE));
        assertEquals(24, count);
    }

    @Test
    public void testFacesOperationCount() {
        Block.Getter getter = (x, y, z, condition) -> Block.AIR;
        MawConfig config = MawConfig.defaultConfig();
        // 3x3x3 box has 27 - 1 (center) = 26 exterior blocks
        CuboidSelection selection = new CuboidSelection(new Vec(0, 0, 0), new Vec(2, 2, 2));
        AsyncEditSession session = new AsyncEditSession(getter, config.maxBlocksPerOperation(), selection);

        long count = FacesOperation.execute(session, selection, new SingleBlockPattern(Block.STONE));
        assertEquals(26, count);
    }

    @Test
    public void testHollowOperationCount() {
        Block.Getter getter = (x, y, z, condition) -> Block.STONE;
        MawConfig config = MawConfig.defaultConfig();
        // 3x3x3 solid block: hollowing with thickness 1 should replace the 1 center block with air
        CuboidSelection selection = new CuboidSelection(new Vec(0, 0, 0), new Vec(2, 2, 2));
        AsyncEditSession session = new AsyncEditSession(getter, config.maxBlocksPerOperation(), selection);

        long count = HollowOperation.execute(session, selection, 1, new SingleBlockPattern(Block.AIR));
        assertEquals(1, count);
    }

    @Test
    public void testSphereOperation() {
        Block.Getter getter = (x, y, z, condition) -> Block.AIR;
        MawConfig config = MawConfig.defaultConfig();
        AsyncEditSession session = new AsyncEditSession(getter, config.maxBlocksPerOperation());

        long count = SphereOperation.execute(session, new Vec(0, 0, 0), 2, 2, 2, false, new SingleBlockPattern(Block.STONE));
        assertTrue(count > 0, "Sphere should contain blocks");
    }

    @Test
    public void testCylinderOperation() {
        Block.Getter getter = (x, y, z, condition) -> Block.AIR;
        MawConfig config = MawConfig.defaultConfig();
        AsyncEditSession session = new AsyncEditSession(getter, config.maxBlocksPerOperation());

        long count = CylinderOperation.execute(session, new Vec(0, 0, 0), 2, 2, 3, false, new SingleBlockPattern(Block.STONE));
        assertTrue(count > 0, "Cylinder should contain blocks");
    }
}
