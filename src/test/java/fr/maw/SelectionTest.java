package fr.maw;

import fr.maw.selection.CuboidSelection;
import fr.maw.selection.Selection;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class SelectionTest {

    @Test
    public void testCuboidSelectionDimensionsAndVolume() {
        Point p1 = new Vec(10, 5, 20);
        Point p2 = new Vec(0, 0, 10);

        Selection selection = new CuboidSelection(p1, p2);

        assertEquals(0, selection.getMinX());
        assertEquals(10, selection.getMaxX());
        assertEquals(0, selection.getMinY());
        assertEquals(5, selection.getMaxY());
        assertEquals(10, selection.getMinZ());
        assertEquals(20, selection.getMaxZ());

        assertEquals(11, selection.getWidthX());
        assertEquals(6, selection.getHeightY());
        assertEquals(11, selection.getLengthZ());

        assertEquals(11 * 6 * 11, selection.getVolume());

        assertTrue(selection.contains(0, 0, 10));
        assertTrue(selection.contains(10, 5, 20));
        assertTrue(selection.contains(5, 2, 15));
        assertFalse(selection.contains(-1, 0, 10));
        assertFalse(selection.contains(11, 5, 20));
        assertFalse(selection.contains(5, 6, 15));
    }

    @Test
    public void testSelectionIterationCount() {
        Point p1 = new Vec(0, 0, 0);
        Point p2 = new Vec(2, 2, 2); // 3x3x3 = 27 blocks

        Selection selection = new CuboidSelection(p1, p2);
        List<Point> points = new ArrayList<>();
        for (Point p : selection) {
            points.add(p);
        }

        assertEquals(27, points.size());
        assertEquals(selection.getVolume(), points.size());
    }
}
