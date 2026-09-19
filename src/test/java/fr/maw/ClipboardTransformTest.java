package fr.maw;

import fr.maw.clipboard.Clipboard;
import fr.maw.clipboard.Transform;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.block.Block;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class ClipboardTransformTest {

    @Test
    public void testRotate90Degrees() {
        Map<Point, Block> blocks = new HashMap<>();
        // Block facing north at (1, 0, 0)
        Block stair = Block.OAK_STAIRS.withProperty("facing", "north");
        blocks.put(new Vec(1, 0, 0), stair);

        Clipboard clipboard = new Clipboard(blocks, Vec.ZERO, new Vec(2, 1, 2));

        Clipboard rotated = Transform.rotate(clipboard, 90);

        // (1, 0, 0) rotated 90 deg CW becomes (-z, y, x) = (0, 0, 1)
        Block rotatedBlock = rotated.getBlocks().get(new Vec(0, 0, 1));
        assertNotNull(rotatedBlock, "Rotated block must be present at (0, 0, 1)");
        assertEquals("east", rotatedBlock.getProperty("facing"), "Facing should rotate from north to east");
    }

    @Test
    public void testFlipZ() {
        Map<Point, Block> blocks = new HashMap<>();
        Block stair = Block.OAK_STAIRS.withProperty("facing", "north");
        blocks.put(new Vec(0, 0, 2), stair);

        Clipboard clipboard = new Clipboard(blocks, Vec.ZERO, new Vec(1, 1, 3));

        Clipboard flipped = Transform.flip(clipboard, Transform.Direction.NORTH);

        Block flippedBlock = flipped.getBlocks().get(new Vec(0, 0, -2));
        assertNotNull(flippedBlock, "Flipped block must be present at (0, 0, -2)");
        assertEquals("south", flippedBlock.getProperty("facing"), "Facing should flip from north to south");
    }
}
