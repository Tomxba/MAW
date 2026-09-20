package fr.maw;

import fr.maw.clipboard.Transform.Direction;
import fr.maw.selection.CuboidSelection;
import fr.maw.selection.SelectionHelper;
import net.minestom.server.coordinate.Vec;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SelectionModifierTest {

    @Test
    public void testExpandDirection() {
        CuboidSelection sel = new CuboidSelection(new Vec(0, 0, 0), new Vec(10, 10, 10));

        CuboidSelection expNorth = SelectionHelper.expand(sel, 5, Direction.NORTH);
        assertEquals(-5, expNorth.getMinZ());
        assertEquals(10, expNorth.getMaxZ());

        CuboidSelection expEast = SelectionHelper.expand(sel, 5, Direction.EAST);
        assertEquals(15, expEast.getMaxX());
        assertEquals(0, expEast.getMinX());

        CuboidSelection expUp = SelectionHelper.expand(sel, 5, Direction.UP);
        assertEquals(15, expUp.getMaxY());
        assertEquals(0, expUp.getMinY());
    }

    @Test
    public void testExpandWithReverse() {
        CuboidSelection sel = new CuboidSelection(new Vec(0, 0, 0), new Vec(10, 10, 10));
        CuboidSelection exp = SelectionHelper.expand(sel, 5, 2, Direction.NORTH);
        assertEquals(-5, exp.getMinZ());
        assertEquals(12, exp.getMaxZ());
    }

    @Test
    public void testExpandVert() {
        CuboidSelection sel = new CuboidSelection(new Vec(0, 10, 0), new Vec(10, 20, 10));
        CuboidSelection exp = SelectionHelper.expandVert(sel, -64, 319);
        assertEquals(-64, exp.getMinY());
        assertEquals(319, exp.getMaxY());
        assertEquals(0, exp.getMinX());
        assertEquals(10, exp.getMaxX());
    }

    @Test
    public void testContractDirection() {
        CuboidSelection sel = new CuboidSelection(new Vec(0, 0, 0), new Vec(10, 10, 10));
        CuboidSelection contracted = SelectionHelper.contract(sel, 2, Direction.NORTH);
        assertEquals(8, contracted.getMaxZ());
        assertEquals(0, contracted.getMinZ());
    }

    @Test
    public void testShiftDirection() {
        CuboidSelection sel = new CuboidSelection(new Vec(0, 0, 0), new Vec(10, 10, 10));
        CuboidSelection shifted = SelectionHelper.shift(sel, 5, Direction.EAST);
        assertEquals(5, shifted.getMinX());
        assertEquals(15, shifted.getMaxX());
        assertEquals(0, shifted.getMinY());
        assertEquals(10, shifted.getMaxY());
    }

    @Test
    public void testInsetAndOutset() {
        CuboidSelection sel = new CuboidSelection(new Vec(0, 0, 0), new Vec(10, 10, 10));

        CuboidSelection in = SelectionHelper.inset(sel, 2);
        assertEquals(2, in.getMinX());
        assertEquals(8, in.getMaxX());
        assertEquals(2, in.getMinY());
        assertEquals(8, in.getMaxY());

        CuboidSelection out = SelectionHelper.outset(sel, 2);
        assertEquals(-2, out.getMinX());
        assertEquals(12, out.getMaxX());
        assertEquals(-2, out.getMinY());
        assertEquals(12, out.getMaxY());
    }

    @Test
    public void testParseDirection() {
        assertEquals(Direction.NORTH, SelectionHelper.parseDirection("north", Direction.UP));
        assertEquals(Direction.SOUTH, SelectionHelper.parseDirection("s", Direction.UP));
        assertEquals(Direction.EAST, SelectionHelper.parseDirection("e", Direction.UP));
        assertEquals(Direction.WEST, SelectionHelper.parseDirection("west", Direction.UP));
        assertEquals(Direction.UP, SelectionHelper.parseDirection("u", Direction.NORTH));
        assertEquals(Direction.DOWN, SelectionHelper.parseDirection("down", Direction.NORTH));
        assertEquals(Direction.NORTH, SelectionHelper.parseDirection("invalid", Direction.NORTH));
    }
}
