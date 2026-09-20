package fr.maw;

import fr.maw.async.AsyncEditSession;
import fr.maw.operation.*;
import fr.maw.pattern.SingleBlockPattern;
import fr.maw.selection.CuboidSelection;
import fr.maw.testutil.InMemoryWorld;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.block.Block;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Extended Region Operations Tests (Move, Stack, Naturalize, Overlay, Line, Center, Fall, Forest, Flora)")
public class RegionOpsExtendedTest {

    @Test
    @DisplayName("Should move blocks in direction and clear original positions")
    public void should_move_blocks_and_clear_origin() {
        InMemoryWorld world = new InMemoryWorld();
        world.setBlock(1, 1, 1, Block.STONE);
        world.setBlock(1, 2, 1, Block.DIRT);

        CuboidSelection sel = new CuboidSelection(new Vec(1, 1, 1), new Vec(1, 2, 1));
        AsyncEditSession session = new AsyncEditSession(world, 1000, sel);

        long moved = MoveOperation.execute(session, sel, 5, fr.maw.clipboard.Transform.Direction.EAST, true);

        assertEquals(2, moved);
        var chunk = session.getChangeQueue().getChunkEntries().iterator().next().blocks();
        // New positions set
        assertEquals(Block.STONE, chunk.get(new Vec(6, 1, 1)));
        assertEquals(Block.DIRT, chunk.get(new Vec(6, 2, 1)));
        // Old positions cleared to air
        assertEquals(Block.AIR, chunk.get(new Vec(1, 1, 1)));
        assertEquals(Block.AIR, chunk.get(new Vec(1, 2, 1)));
    }

    @Test
    @DisplayName("Should stack selection multiple times in direction")
    public void should_stack_blocks_repeatedly() {
        InMemoryWorld world = new InMemoryWorld();
        world.setBlock(0, 0, 0, Block.DIAMOND_BLOCK);

        CuboidSelection sel = new CuboidSelection(new Vec(0, 0, 0), new Vec(0, 0, 0));
        AsyncEditSession session = new AsyncEditSession(world, 1000, sel);

        long stacked = StackOperation.execute(session, sel, 3, fr.maw.clipboard.Transform.Direction.EAST);

        assertEquals(3, stacked);
        var chunk = session.getChangeQueue().getChunkEntries().iterator().next().blocks();
        assertEquals(Block.DIAMOND_BLOCK, chunk.get(new Vec(1, 0, 0)));
        assertEquals(Block.DIAMOND_BLOCK, chunk.get(new Vec(2, 0, 0)));
        assertEquals(Block.DIAMOND_BLOCK, chunk.get(new Vec(3, 0, 0)));
    }

    @Test
    @DisplayName("Should draw a 3D Bresenham line between two points")
    public void should_draw_line_between_two_points() {
        InMemoryWorld world = new InMemoryWorld();
        Point p1 = new Vec(0, 0, 0);
        Point p2 = new Vec(3, 0, 0);

        CuboidSelection sel = new CuboidSelection(p1, p2);
        AsyncEditSession session = new AsyncEditSession(world, 1000, sel);

        long count = LineOperation.execute(session, p1, p2, new SingleBlockPattern(Block.OBSIDIAN));

        assertEquals(4, count); // (0,0,0), (1,0,0), (2,0,0), (3,0,0)
        var chunk = session.getChangeQueue().getChunkEntries().iterator().next().blocks();
        assertEquals(Block.OBSIDIAN, chunk.get(new Vec(0, 0, 0)));
        assertEquals(Block.OBSIDIAN, chunk.get(new Vec(1, 0, 0)));
        assertEquals(Block.OBSIDIAN, chunk.get(new Vec(2, 0, 0)));
        assertEquals(Block.OBSIDIAN, chunk.get(new Vec(3, 0, 0)));
    }

    @Test
    @DisplayName("Should find and set the center block of selection")
    public void should_set_center_block() {
        InMemoryWorld world = new InMemoryWorld();
        // 3x3x3 cuboid from 0 to 2
        CuboidSelection sel = new CuboidSelection(new Vec(0, 0, 0), new Vec(2, 2, 2));
        AsyncEditSession session = new AsyncEditSession(world, 1000, sel);

        long count = CenterOperation.execute(session, sel, new SingleBlockPattern(Block.BEACON));

        assertEquals(1, count);
        var chunk = session.getChangeQueue().getChunkEntries().iterator().next().blocks();
        assertEquals(Block.BEACON, chunk.get(new Vec(1, 1, 1)));
    }

    @Test
    @DisplayName("Should naturalize surface to grass, sub-surface to dirt, deep to stone")
    public void should_naturalize_soil_layers() {
        InMemoryWorld world = new InMemoryWorld();
        // Create a column of sandstone from y=0 to y=5
        for (int y = 0; y <= 5; y++) {
            world.setBlock(0, y, 0, Block.SANDSTONE);
        }

        CuboidSelection sel = new CuboidSelection(new Vec(0, 0, 0), new Vec(0, 5, 0));
        AsyncEditSession session = new AsyncEditSession(world, 1000, sel);

        long count = NaturalizeOperation.execute(session, sel);

        assertEquals(6, count);
        var chunk = session.getChangeQueue().getChunkEntries().iterator().next().blocks();
        assertEquals(Block.GRASS_BLOCK, chunk.get(new Vec(0, 5, 0))); // top
        assertEquals(Block.DIRT, chunk.get(new Vec(0, 4, 0)));        // layer 1
        assertEquals(Block.DIRT, chunk.get(new Vec(0, 3, 0)));        // layer 2
        assertEquals(Block.DIRT, chunk.get(new Vec(0, 2, 0)));        // layer 3
        assertEquals(Block.STONE, chunk.get(new Vec(0, 1, 0)));       // deep
        assertEquals(Block.STONE, chunk.get(new Vec(0, 0, 0)));       // deep
    }

    @Test
    @DisplayName("Should overlay pattern above existing blocks")
    public void should_overlay_pattern_above_surface() {
        InMemoryWorld world = new InMemoryWorld();
        world.setBlock(0, 0, 0, Block.GRASS_BLOCK);
        world.setBlock(1, 0, 0, Block.GRASS_BLOCK);

        CuboidSelection sel = new CuboidSelection(new Vec(0, 0, 0), new Vec(1, 1, 0));
        AsyncEditSession session = new AsyncEditSession(world, 1000, sel);

        long count = OverlayOperation.execute(session, sel, new SingleBlockPattern(Block.SNOW));

        assertEquals(2, count);
        var chunk = session.getChangeQueue().getChunkEntries().iterator().next().blocks();
        assertEquals(Block.SNOW, chunk.get(new Vec(0, 1, 0)));
        assertEquals(Block.SNOW, chunk.get(new Vec(1, 1, 0)));
    }

    @Test
    @DisplayName("Should drop floating blocks downwards with FallOperation")
    public void should_drop_floating_blocks() {
        InMemoryWorld world = new InMemoryWorld();
        world.setBlock(0, 5, 0, Block.SAND);
        world.setBlock(0, 0, 0, Block.BEDROCK);

        CuboidSelection sel = new CuboidSelection(new Vec(0, 0, 0), new Vec(0, 5, 0));
        AsyncEditSession session = new AsyncEditSession(world, 1000, sel);

        long count = FallOperation.execute(session, sel);

        assertTrue(count > 0);
        var chunk = session.getChangeQueue().getChunkEntries().iterator().next().blocks();
        assertEquals(Block.SAND, chunk.get(new Vec(0, 1, 0))); // dropped to y=1 above bedrock
        assertEquals(Block.AIR, chunk.get(new Vec(0, 5, 0)));  // original position cleared
    }

    @Test
    @DisplayName("Should generate flora on grass block surface")
    public void should_generate_flora_on_surface() {
        InMemoryWorld world = new InMemoryWorld();
        world.setBlock(0, 0, 0, Block.GRASS_BLOCK);

        CuboidSelection sel = new CuboidSelection(new Vec(0, 0, 0), new Vec(0, 1, 0));
        AsyncEditSession session = new AsyncEditSession(world, 1000, sel);

        long count = FloraOperation.execute(session, sel, 1.0);

        assertEquals(1, count);
        var chunk = session.getChangeQueue().getChunkEntries().iterator().next().blocks();
        assertNotNull(chunk.get(new Vec(0, 1, 0)));
    }
}
