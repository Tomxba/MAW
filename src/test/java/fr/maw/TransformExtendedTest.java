package fr.maw;

import fr.maw.clipboard.Clipboard;
import fr.maw.clipboard.Transform;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.block.Block;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Transform and Clipboard Geometry Tests")
public class TransformExtendedTest {

    @Test
    @DisplayName("Should return identical clipboard on 0 or 360 degrees rotation")
    public void should_return_identical_clipboard_on_zero_or_360_rotation() {
        // Arrange
        Clipboard original = new Clipboard(
                Map.of(new Vec(1, 2, 3), Block.STONE),
                new Vec(1, 2, 3),
                new Vec(1, 1, 1)
        );

        // Act & Assert
        assertSame(original, Transform.rotate(original, 0));
        assertSame(original, Transform.rotate(original, 360));
    }

    @Test
    @DisplayName("Should rotate 180 degrees correctly")
    public void should_rotate_180_degrees_correctly() {
        // Arrange
        Block stair = Block.OAK_STAIRS.withProperty("facing", "north");
        Clipboard clipboard = new Clipboard(
                Map.of(new Vec(2, 0, 3), stair),
                Vec.ZERO,
                new Vec(3, 1, 4)
        );

        // Act - 180 deg CW: (x, y, z) -> (-x, y, -z)
        Clipboard rotated = Transform.rotate(clipboard, 180);

        // Assert
        Block b = rotated.getBlocks().get(new Vec(-2, 0, -3));
        assertNotNull(b);
        assertEquals("south", b.getProperty("facing"), "Facing should be south after 180 deg rotation");
    }

    @Test
    @DisplayName("Should rotate 270 degrees and negative 90 degrees identically")
    public void should_rotate_270_and_negative_90_identically() {
        // Arrange
        Block stair = Block.OAK_STAIRS.withProperty("facing", "north");
        Clipboard clipboard = new Clipboard(
                Map.of(new Vec(1, 0, 0), stair),
                Vec.ZERO,
                new Vec(2, 1, 1)
        );

        // Act
        Clipboard r270 = Transform.rotate(clipboard, 270);
        Clipboard rNeg90 = Transform.rotate(clipboard, -90);

        // Assert
        // 270 deg CW: (1, 0, 0) -> (0, 0, -1)
        Block b270 = r270.getBlocks().get(new Vec(0, 0, -1));
        Block bNeg90 = rNeg90.getBlocks().get(new Vec(0, 0, -1));

        assertNotNull(b270);
        assertNotNull(bNeg90);
        assertEquals("west", b270.getProperty("facing"));
        assertEquals("west", bNeg90.getProperty("facing"));
    }

    @Test
    @DisplayName("Should rotate log and pillar axis properties")
    public void should_rotate_log_axis_properties() {
        // Arrange
        Block logX = Block.OAK_LOG.withProperty("axis", "x");
        Block logY = Block.OAK_LOG.withProperty("axis", "y");

        Clipboard clipboard = new Clipboard(
                Map.of(new Vec(0, 0, 0), logX, new Vec(0, 1, 0), logY),
                Vec.ZERO,
                new Vec(1, 2, 1)
        );

        // Act
        Clipboard rotated = Transform.rotate(clipboard, 90);

        // Assert
        Block rotX = rotated.getBlocks().get(new Vec(0, 0, 0));
        Block rotY = rotated.getBlocks().get(new Vec(0, 1, 0));

        assertEquals("z", rotX.getProperty("axis"), "Axis x should rotate to axis z");
        assertEquals("y", rotY.getProperty("axis"), "Axis y should remain axis y");
    }

    @Test
    @DisplayName("Should flip UP and DOWN modifying half and facing properties")
    public void should_flip_up_down_correctly() {
        // Arrange
        Block stair = Block.OAK_STAIRS
                .withProperty("facing", "north")
                .withProperty("half", "bottom");

        Clipboard clipboard = new Clipboard(
                Map.of(new Vec(0, 5, 0), stair),
                Vec.ZERO,
                new Vec(1, 6, 1)
        );

        // Act
        Clipboard flipped = Transform.flip(clipboard, Transform.Direction.UP);

        // Assert
        Block b = flipped.getBlocks().get(new Vec(0, -5, 0));
        assertNotNull(b);
        assertEquals("top", b.getProperty("half"), "half should toggle to top on vertical flip");
        assertEquals("north", b.getProperty("facing"), "facing north should remain north on vertical flip");
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when rotation is not a multiple of 90")
    public void should_throw_when_rotation_not_multiple_of_90() {
        Clipboard clipboard = new Clipboard(Map.of(), Vec.ZERO, Vec.ZERO);
        assertThrows(IllegalArgumentException.class, () -> Transform.rotate(clipboard, 45));
        assertThrows(IllegalArgumentException.class, () -> Transform.rotate(clipboard, 137));
    }
}
