package fr.maw;

import fr.maw.history.BlockChange;
import fr.maw.history.ChangeSet;
import fr.maw.history.HistoryManager;
import net.minestom.server.instance.block.Block;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class HistoryTest {

    @Test
    public void testChangeSetInversion() {
        BlockChange c1 = new BlockChange(0, 0, 0, Block.AIR, Block.STONE);
        BlockChange c2 = new BlockChange(1, 2, 3, Block.DIRT, Block.GRASS_BLOCK);

        ChangeSet set = new ChangeSet(List.of(c1, c2));
        assertEquals(2, set.size());

        ChangeSet inv = set.inverse();
        assertEquals(2, inv.size());

        // First inverted change should be reverse of c2
        BlockChange inv1 = inv.getChanges().get(0);
        assertEquals(1, inv1.x());
        assertEquals(2, inv1.y());
        assertEquals(3, inv1.z());
        assertEquals(Block.GRASS_BLOCK, inv1.previousBlock());
        assertEquals(Block.DIRT, inv1.newBlock());
    }

    @Test
    public void testHistoryManagerStacksAndLimit() {
        HistoryManager history = new HistoryManager(2);

        ChangeSet cs1 = new ChangeSet(List.of(new BlockChange(0, 0, 0, Block.AIR, Block.STONE)));
        ChangeSet cs2 = new ChangeSet(List.of(new BlockChange(1, 0, 0, Block.AIR, Block.DIRT)));
        ChangeSet cs3 = new ChangeSet(List.of(new BlockChange(2, 0, 0, Block.AIR, Block.SAND)));

        history.record(cs1);
        history.record(cs2);
        assertEquals(2, history.getUndoCount());

        // Adding cs3 should evict cs1 because limit is 2
        history.record(cs3);
        assertEquals(2, history.getUndoCount());

        ChangeSet popped1 = history.popUndo();
        assertSame(cs3, popped1);
        assertEquals(1, history.getRedoCount());

        ChangeSet popped2 = history.popUndo();
        assertSame(cs2, popped2);
        assertEquals(2, history.getRedoCount());

        assertNull(history.popUndo()); // cs1 was evicted

        // Redo
        ChangeSet redo1 = history.popRedo();
        assertSame(cs2, redo1);
    }
}
