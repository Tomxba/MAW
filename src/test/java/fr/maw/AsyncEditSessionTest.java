package fr.maw;

import fr.maw.async.AsyncEditSession;
import fr.maw.history.ChangeSet;
import fr.maw.selection.CuboidSelection;
import fr.maw.testutil.InMemoryWorld;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.block.Block;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("AsyncEditSession Unit Tests")
public class AsyncEditSessionTest {

    @Test
    @DisplayName("Should enforce block limit and throw MaxBlocksExceededException")
    public void should_throw_exception_when_block_limit_exceeded() {
        // Arrange
        InMemoryWorld world = new InMemoryWorld(Block.AIR);
        CuboidSelection selection = new CuboidSelection(new Vec(0, 0, 0), new Vec(5, 5, 5));
        AsyncEditSession session = new AsyncEditSession(world, 2, selection); // Limit of 2 blocks

        // Act & Assert
        session.setBlock(0, 0, 0, Block.STONE);
        session.setBlock(1, 0, 0, Block.STONE);

        // 3rd block exceeds limit
        assertThrows(AsyncEditSession.MaxBlocksExceededException.class, () -> {
            session.setBlock(2, 0, 0, Block.STONE);
        });
    }

    @Test
    @DisplayName("Should throw IllegalStateException when setting block on closed session")
    public void should_throw_exception_when_session_is_closed() {
        // Arrange
        InMemoryWorld world = new InMemoryWorld(Block.AIR);
        AsyncEditSession session = new AsyncEditSession(world, 100);
        session.close();

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> {
            session.setBlock(0, 0, 0, Block.STONE);
        });
    }

    @Test
    @DisplayName("Should skip recording change when block is already the target block")
    public void should_skip_recording_when_block_is_already_target() {
        // Arrange
        InMemoryWorld world = new InMemoryWorld(Block.STONE); // Default is STONE
        AsyncEditSession session = new AsyncEditSession(world, 100);

        // Act - setting STONE on STONE
        session.setBlock(0, 0, 0, Block.STONE);

        // Assert
        assertEquals(0, session.getChangedCount());
        assertTrue(session.getChangeQueue().isEmpty());
    }

    @Test
    @DisplayName("Should record BlockChange and create valid ChangeSet")
    public void should_record_changes_and_create_valid_changeset() {
        // Arrange
        InMemoryWorld world = new InMemoryWorld(Block.AIR);
        AsyncEditSession session = new AsyncEditSession(world, 100);

        // Act
        session.setBlock(0, 10, 0, Block.STONE);
        session.setBlock(0, 11, 0, Block.OAK_LOG);

        // Assert
        assertEquals(2, session.getChangedCount());
        ChangeSet changeSet = session.createChangeSet();
        assertEquals(2, changeSet.size());

        // First change
        var c1 = changeSet.getChanges().get(0);
        assertEquals(0, c1.x());
        assertEquals(10, c1.y());
        assertEquals(0, c1.z());
        assertEquals(Block.AIR, c1.previousBlock());
        assertEquals(Block.STONE, c1.newBlock());
    }

    @Test
    @DisplayName("Should accurately identify boundary blocks when selection bounds are provided")
    public void should_accurately_identify_boundary_blocks() {
        // Arrange
        InMemoryWorld world = new InMemoryWorld(Block.AIR);
        // 3x3x3 box: x in [0, 2], y in [0, 2], z in [0, 2]
        CuboidSelection selection = new CuboidSelection(new Vec(0, 0, 0), new Vec(2, 2, 2));
        AsyncEditSession session = new AsyncEditSession(world, 100, selection);

        // Act
        // Boundary block (x=0)
        session.setBlock(0, 1, 1, Block.STONE);
        // Inner block (x=1, y=1, z=1)
        session.setBlock(1, 1, 1, Block.DIRT);

        // Assert
        var boundaries = session.getBoundaryBlocks();
        assertEquals(1, boundaries.size());
        assertTrue(boundaries.contains(new Vec(0, 1, 1)));
        assertFalse(boundaries.contains(new Vec(1, 1, 1)));
    }
}
