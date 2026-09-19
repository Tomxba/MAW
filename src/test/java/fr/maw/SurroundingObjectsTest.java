package fr.maw;

import fr.maw.objects.SurroundingObjectHandler;
import fr.maw.selection.CuboidSelection;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@DisplayName("SurroundingObjectHandler and Physics Tests")
public class SurroundingObjectsTest {

    @Test
    @DisplayName("Should handle null and empty collections gracefully without exception")
    public void should_handle_null_and_empty_gracefully() {
        assertDoesNotThrow(() -> {
            SurroundingObjectHandler.handlePreBlockChanges(null, null, false);
            SurroundingObjectHandler.handlePreBlockChanges(null, null, true);

            CuboidSelection sel = new CuboidSelection(new Vec(0, 0, 0), new Vec(1, 1, 1));
            SurroundingObjectHandler.handlePreBlockChanges(null, sel, true);

            SurroundingObjectHandler.handlePostBlockChanges(null, null, false);
            SurroundingObjectHandler.handlePostBlockChanges(null, Collections.emptyList(), true);
            SurroundingObjectHandler.handlePostBlockChanges(null, List.of(new Vec(0, 0, 0)), false);
        });
    }
}
