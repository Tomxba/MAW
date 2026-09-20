package fr.maw;

import fr.maw.async.AsyncEditSession;
import fr.maw.operation.ConeOperation;
import fr.maw.operation.EllipsoidOperation;
import fr.maw.operation.PyramidOperation;
import fr.maw.operation.TorusOperation;
import fr.maw.pattern.SingleBlockPattern;
import fr.maw.selection.CuboidSelection;
import fr.maw.testutil.InMemoryWorld;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.block.Block;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Extended Shape Operations Tests (Pyramid, Cone, Torus, Ellipsoid)")
public class ShapesExtendedTest {

    @Test
    @DisplayName("Should generate solid and hollow pyramids")
    public void should_generate_pyramids() {
        InMemoryWorld world = new InMemoryWorld();
        Point origin = new Vec(10, 10, 10);
        CuboidSelection sel = new CuboidSelection(new Vec(0, 0, 0), new Vec(20, 20, 20));

        // Solid pyramid of height 3
        AsyncEditSession session1 = new AsyncEditSession(world, 1000, sel);
        long solidCount = PyramidOperation.execute(session1, origin, new SingleBlockPattern(Block.SANDSTONE), 3, false);
        // y=0: r=2 => 5x5 = 25
        // y=1: r=1 => 3x3 = 9
        // y=2: r=0 => 1x1 = 1
        // Total = 35
        assertEquals(35, solidCount);

        // Hollow pyramid of height 3
        AsyncEditSession session2 = new AsyncEditSession(world, 1000, sel);
        long hollowCount = PyramidOperation.execute(session2, origin, new SingleBlockPattern(Block.SANDSTONE), 3, true);
        // y=0: solid base = 25
        // y=1: outer 8 (center hollowed) = 8
        // y=2: apex = 1
        // Total = 34
        assertTrue(hollowCount < solidCount);
        assertEquals(34, hollowCount);
    }

    @Test
    @DisplayName("Should generate solid and hollow cones")
    public void should_generate_cones() {
        InMemoryWorld world = new InMemoryWorld();
        Point origin = new Vec(10, 10, 10);
        CuboidSelection sel = new CuboidSelection(new Vec(0, 0, 0), new Vec(20, 20, 20));

        AsyncEditSession session1 = new AsyncEditSession(world, 1000, sel);
        long solidCount = ConeOperation.execute(session1, origin, new SingleBlockPattern(Block.PRISMARINE), 4, 5, false);
        assertTrue(solidCount > 0);

        AsyncEditSession session2 = new AsyncEditSession(world, 1000, sel);
        long hollowCount = ConeOperation.execute(session2, origin, new SingleBlockPattern(Block.PRISMARINE), 4, 5, true);
        assertTrue(hollowCount > 0);
        assertTrue(hollowCount <= solidCount);
    }

    @Test
    @DisplayName("Should generate solid and hollow torus")
    public void should_generate_torus() {
        InMemoryWorld world = new InMemoryWorld();
        Point origin = new Vec(20, 20, 20);
        CuboidSelection sel = new CuboidSelection(new Vec(0, 0, 0), new Vec(40, 40, 40));

        AsyncEditSession session1 = new AsyncEditSession(world, 5000, sel);
        long solidCount = TorusOperation.execute(session1, origin, new SingleBlockPattern(Block.GOLD_BLOCK), 5, 2, false);
        assertTrue(solidCount > 0);

        AsyncEditSession session2 = new AsyncEditSession(world, 5000, sel);
        long hollowCount = TorusOperation.execute(session2, origin, new SingleBlockPattern(Block.GOLD_BLOCK), 5, 2, true);
        assertTrue(hollowCount > 0);
        assertTrue(hollowCount < solidCount);
    }

    @Test
    @DisplayName("Should generate solid and hollow ellipsoid")
    public void should_generate_ellipsoid() {
        InMemoryWorld world = new InMemoryWorld();
        Point origin = new Vec(15, 15, 15);
        CuboidSelection sel = new CuboidSelection(new Vec(0, 0, 0), new Vec(30, 30, 30));

        AsyncEditSession session1 = new AsyncEditSession(world, 5000, sel);
        long solidCount = EllipsoidOperation.execute(session1, origin, new SingleBlockPattern(Block.AMETHYST_BLOCK), 4, 2, 3, false);
        assertTrue(solidCount > 0);

        AsyncEditSession session2 = new AsyncEditSession(world, 5000, sel);
        long hollowCount = EllipsoidOperation.execute(session2, origin, new SingleBlockPattern(Block.AMETHYST_BLOCK), 4, 2, 3, true);
        assertTrue(hollowCount > 0);
        assertTrue(hollowCount < solidCount);
    }
}
