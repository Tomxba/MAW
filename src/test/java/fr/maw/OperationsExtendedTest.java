package fr.maw;

import fr.maw.async.AsyncEditSession;
import fr.maw.clipboard.Clipboard;
import fr.maw.operation.*;
import fr.maw.pattern.Mask;
import fr.maw.pattern.Masks;
import fr.maw.pattern.SingleBlockPattern;
import fr.maw.selection.CuboidSelection;
import fr.maw.testutil.InMemoryWorld;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.block.Block;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Comprehensive WorldEdit Operations Tests")
public class OperationsExtendedTest {

    @Test
    @DisplayName("Should fill selection with SetOperation and respect mask")
    public void should_fill_selection_and_respect_mask() {
        // Arrange
        InMemoryWorld world = new InMemoryWorld();
        world.setBlock(0, 0, 0, Block.DIRT);
        world.setBlock(1, 0, 0, Block.STONE);
        world.setBlock(2, 0, 0, Block.DIRT);

        CuboidSelection selection = new CuboidSelection(new Vec(0, 0, 0), new Vec(2, 0, 0)); // 3 blocks
        AsyncEditSession session = new AsyncEditSession(world, 100, selection);
        Mask mask = Masks.ofBlock(Block.DIRT); // Only replace DIRT

        // Act
        long count = SetOperation.execute(session, selection, mask, new SingleBlockPattern(Block.GOLD_BLOCK));

        // Assert
        assertEquals(2, count);
        assertEquals(2, session.getChangedCount());
        var blocks = session.getChangeQueue().getChunkEntries().iterator().next().blocks();
        assertEquals(Block.GOLD_BLOCK, blocks.get(new Vec(0, 0, 0)));
        assertEquals(Block.GOLD_BLOCK, blocks.get(new Vec(2, 0, 0)));
        assertNull(blocks.get(new Vec(1, 0, 0))); // STONE was untouched
    }

    @Test
    @DisplayName("Should create hollow walls without top or bottom using WallsOperation")
    public void should_create_walls_leaving_interior_and_caps_untouched() {
        // Arrange
        InMemoryWorld world = new InMemoryWorld();
        // 3x3x3 cuboid
        CuboidSelection selection = new CuboidSelection(new Vec(0, 0, 0), new Vec(2, 2, 2));
        AsyncEditSession session = new AsyncEditSession(world, 100, selection);

        // Act
        long count = WallsOperation.execute(session, selection, new SingleBlockPattern(Block.BRICKS));

        // Assert - 8 perimeter blocks per layer * 3 layers = 24 blocks
        assertEquals(24, count);
        var blocks = session.getChangeQueue().getChunkEntries().iterator().next().blocks();

        // Center column (1, y, 1) should NOT be set
        for (int y = 0; y <= 2; y++) {
            assertNull(blocks.get(new Vec(1, y, 1)), "Center at y=" + y + " should be untouched");
        }
        // Corner (0, y, 0) should be set
        assertEquals(Block.BRICKS, blocks.get(new Vec(0, 0, 0)));
    }

    @Test
    @DisplayName("Should cover all 6 faces using FacesOperation leaving center hollow")
    public void should_cover_all_six_faces_leaving_interior_hollow() {
        // Arrange
        InMemoryWorld world = new InMemoryWorld();
        // 3x3x3 cuboid: volume 27, center is 1x1x1 (1 block). Outer faces = 26 blocks.
        CuboidSelection selection = new CuboidSelection(new Vec(0, 0, 0), new Vec(2, 2, 2));
        AsyncEditSession session = new AsyncEditSession(world, 100, selection);

        // Act
        long count = FacesOperation.execute(session, selection, new SingleBlockPattern(Block.OBSIDIAN));

        // Assert
        assertEquals(26, count);
        var blocks = session.getChangeQueue().getChunkEntries().iterator().next().blocks();

        // Exactly the center block (1, 1, 1) is empty
        assertNull(blocks.get(new Vec(1, 1, 1)));
        // Top and bottom center (1, 0, 1) and (1, 2, 1) ARE set in faces
        assertEquals(Block.OBSIDIAN, blocks.get(new Vec(1, 0, 1)));
        assertEquals(Block.OBSIDIAN, blocks.get(new Vec(1, 2, 1)));
    }

    @Test
    @DisplayName("Should hollow out solid cube using HollowOperation")
    public void should_hollow_out_solid_cube() {
        // Arrange - 3x3x3 of solid stone
        InMemoryWorld world = new InMemoryWorld(Block.STONE);
        CuboidSelection selection = new CuboidSelection(new Vec(0, 0, 0), new Vec(2, 2, 2));
        AsyncEditSession session = new AsyncEditSession(world, 100, selection);

        // Act - Hollow with thickness 1 and AIR inside
        long count = HollowOperation.execute(session, selection, 1, new SingleBlockPattern(Block.AIR));

        // Assert - exactly 1 interior block (1, 1, 1) hollowed out
        assertEquals(1, count);
        var blocks = session.getChangeQueue().getChunkEntries().iterator().next().blocks();
        assertEquals(Block.AIR, blocks.get(new Vec(1, 1, 1)));
        assertNull(blocks.get(new Vec(0, 0, 0))); // perimeter was untouched
    }

    @Test
    @DisplayName("Should generate solid vs hollow sphere correctly")
    public void should_generate_solid_and_hollow_sphere() {
        // Arrange
        InMemoryWorld world = new InMemoryWorld();
        Point center = new Vec(0, 0, 0);

        // Solid sphere of radius 3
        AsyncEditSession solidSession = new AsyncEditSession(world, 500);
        long solidCount = SphereOperation.execute(solidSession, center, 3, 3, 3, false, new SingleBlockPattern(Block.STONE));

        // Hollow sphere of radius 3
        AsyncEditSession hollowSession = new AsyncEditSession(world, 500);
        long hollowCount = SphereOperation.execute(hollowSession, center, 3, 3, 3, true, new SingleBlockPattern(Block.STONE));

        // Assert - solid sphere has more blocks than hollow sphere
        assertTrue(solidCount > 0, "Solid sphere must have blocks");
        assertTrue(hollowCount > 0, "Hollow sphere must have blocks");
        assertTrue(solidCount > hollowCount, "Solid sphere count (" + solidCount + ") must exceed hollow count (" + hollowCount + ")");

        // Center must be present in solid sphere, but absent in hollow sphere
        var solidBlocks = solidSession.getChangeQueue().getChunkEntries().iterator().next().blocks();
        var hollowBlocks = hollowSession.getChangeQueue().getChunkEntries().iterator().next().blocks();

        assertNotNull(solidBlocks.get(new Vec(0, 0, 0)), "Center must be filled in solid sphere");
        assertNull(hollowBlocks.get(new Vec(0, 0, 0)), "Center must be hollow in hollow sphere");
    }

    @Test
    @DisplayName("Should generate solid vs hollow cylinder correctly")
    public void should_generate_solid_and_hollow_cylinder() {
        // Arrange
        InMemoryWorld world = new InMemoryWorld();
        Point baseCenter = new Vec(0, 0, 0);

        // Solid cylinder radius 3, height 5
        AsyncEditSession solidSession = new AsyncEditSession(world, 500);
        long solidCount = CylinderOperation.execute(solidSession, baseCenter, 3, 3, 5, false, new SingleBlockPattern(Block.STONE));

        // Hollow cylinder radius 3, height 5
        AsyncEditSession hollowSession = new AsyncEditSession(world, 500);
        long hollowCount = CylinderOperation.execute(hollowSession, baseCenter, 3, 3, 5, true, new SingleBlockPattern(Block.STONE));

        // Assert
        assertTrue(solidCount > hollowCount, "Solid cylinder must have more blocks than hollow cylinder");

        var solidBlocks = solidSession.getChangeQueue().getChunkEntries().iterator().next().blocks();
        var hollowBlocks = hollowSession.getChangeQueue().getChunkEntries().iterator().next().blocks();

        assertNotNull(solidBlocks.get(new Vec(0, 2, 0)), "Center axis must be filled in solid cylinder");
        assertNull(hollowBlocks.get(new Vec(0, 2, 0)), "Center axis must be hollow in hollow cylinder");
    }

    @Test
    @DisplayName("Should copy from world and paste with relative offset")
    public void should_copy_and_paste_with_relative_offset() {
        // Arrange
        InMemoryWorld world = new InMemoryWorld();
        world.setBlock(10, 5, 10, Block.GOLD_BLOCK);
        world.setBlock(11, 5, 10, Block.DIAMOND_BLOCK);

        CuboidSelection selection = new CuboidSelection(new Vec(10, 5, 10), new Vec(11, 5, 10));
        Point copyOrigin = new Vec(10, 5, 10);

        // Act - Copy
        Clipboard clipboard = CopyOperation.execute(world, selection, copyOrigin, false);
        assertEquals(2, clipboard.getBlockCount());

        // Act - Paste at (20, 10, 20)
        AsyncEditSession pasteSession = new AsyncEditSession(world, 100);
        Point pasteTarget = new Vec(20, 10, 20);
        long pasted = PasteOperation.execute(pasteSession, clipboard, pasteTarget, false, false);

        // Assert
        assertEquals(2, pasted);
        var blocks = pasteSession.getChangeQueue().getChunkEntries().iterator().next().blocks();
        assertEquals(Block.GOLD_BLOCK, blocks.get(new Vec(20, 10, 20)));
        assertEquals(Block.DIAMOND_BLOCK, blocks.get(new Vec(21, 10, 20)));
    }

    @Test
    @DisplayName("Should ignore air blocks when pasting with ignoreAir flag")
    public void should_ignore_air_blocks_when_pasting_with_ignore_air() {
        // Arrange
        Clipboard clipboard = new Clipboard(
                java.util.Map.of(
                        new Vec(0, 0, 0), Block.STONE,
                        new Vec(1, 0, 0), Block.AIR
                ),
                Vec.ZERO,
                new Vec(2, 1, 1)
        );

        InMemoryWorld world = new InMemoryWorld();
        AsyncEditSession pasteSession = new AsyncEditSession(world, 100);

        // Act - ignoreAir = true
        long pasted = PasteOperation.execute(pasteSession, clipboard, new Vec(0, 0, 0), true, false);

        // Assert
        assertEquals(1, pasted);
        var blocks = pasteSession.getChangeQueue().getChunkEntries().iterator().next().blocks();
        assertEquals(1, blocks.size());
        assertEquals(Block.STONE, blocks.get(new Vec(0, 0, 0)));
        assertNull(blocks.get(new Vec(1, 0, 0)));
    }
}
