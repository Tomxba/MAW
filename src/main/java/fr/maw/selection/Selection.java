package fr.maw.selection;

import net.minestom.server.coordinate.Point;
import java.util.Iterator;

/**
 * Represents a 3D region of blocks in an instance.
 */
public interface Selection extends Iterable<Point> {

    Point getPos1();

    Point getPos2();

    int getMinX();

    int getMaxX();

    int getMinY();

    int getMaxY();

    int getMinZ();

    int getMaxZ();

    int getWidthX();

    int getHeightY();

    int getLengthZ();

    long getVolume();

    boolean contains(int x, int y, int z);

    default boolean contains(Point point) {
        return contains(point.blockX(), point.blockY(), point.blockZ());
    }

    @Override
    Iterator<Point> iterator();
}
