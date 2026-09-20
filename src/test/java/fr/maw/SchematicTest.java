package fr.maw;

import fr.maw.clipboard.Clipboard;
import fr.maw.schematic.SchematicManager;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.block.Block;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Schematic Manager Unit Tests")
public class SchematicTest {

    @Test
    @DisplayName("Should save, load, list and delete schematic files")
    public void should_manage_schematic_lifecycle(@TempDir Path tempDir) throws IOException {
        SchematicManager manager = new SchematicManager(tempDir);

        Map<Point, Block> blocks = new HashMap<>();
        blocks.put(new Vec(0, 0, 0), Block.DIAMOND_BLOCK);
        blocks.put(new Vec(1, 2, 3), Block.OAK_LOG);
        Point origin = new Vec(0, 0, 0);

        Clipboard original = new Clipboard(blocks, origin, 2, 3, 4);

        // 1. Save
        manager.save(original, "test-build");

        // 2. List
        List<String> list = manager.list();
        assertEquals(1, list.size());
        assertEquals("test-build", list.getFirst());

        // 3. Load
        Clipboard loaded = manager.load("test-build");
        assertNotNull(loaded);
        assertEquals(2, loaded.size());
        assertEquals(2, loaded.getWidth());
        assertEquals(3, loaded.getHeight());
        assertEquals(4, loaded.getLength());
        assertEquals(Block.DIAMOND_BLOCK, loaded.getBlocks().get(new Vec(0, 0, 0)));
        assertEquals(Block.OAK_LOG, loaded.getBlocks().get(new Vec(1, 2, 3)));

        // 4. Delete
        assertTrue(manager.delete("test-build"));
        assertEquals(0, manager.list().size());
        assertThrows(FileNotFoundException.class, () -> manager.load("test-build"));
    }
}
