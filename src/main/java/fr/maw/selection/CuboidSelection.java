package fr.maw.selection;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;

/**
 * An axis-aligned cuboid selection defined by two corner points.
 */
public final class CuboidSelection implements Selection {

    private final Point pos1;
    private final Point pos2;
    private final int minX, maxX;
    private final int minY, maxY;
    private final int minZ, maxZ;
    private final int widthX;
    private final int heightY;
    private final int lengthZ;
    private final long volume;

    public CuboidSelection(Point pos1, Point pos2) {
        this.pos1 = Objects.requireNonNull(pos1, "pos1 cannot be null");
        this.pos2 = Objects.requireNonNull(pos2, "pos2 cannot be null");

        this.minX = Math.min(pos1.blockX(), pos2.blockX());
        this.maxX = Math.max(pos1.blockX(), pos2.blockX());

        this.minY = Math.min(pos1.blockY(), pos2.blockY());
        this.maxY = Math.max(pos1.blockY(), pos2.blockY());

        this.minZ = Math.min(pos1.blockZ(), pos2.blockZ());
        this.maxZ = Math.max(pos1.blockZ(), pos2.blockZ());

        this.widthX = (maxX - minX) + 1;
        this.heightY = (maxY - minY) + 1;
        this.lengthZ = (maxZ - minZ) + 1;

        this.volume = (long) widthX * (long) heightY * (long) lengthZ;
    }

    @Override
    public Point getPos1() {
        return pos1;
    }

    @Override
    public Point getPos2() {
        return pos2;
    }

    @Override
    public int getMinX() {
        return minX;
    }

    @Override
    public int getMaxX() {
        return maxX;
    }

    @Override
    public int getMinY() {
        return minY;
    }

    @Override
    public int getMaxY() {
        return maxY;
    }

    @Override
    public int getMinZ() {
        return minZ;
    }

    @Override
    public int getMaxZ() {
        return maxZ;
    }

    @Override
    public int getWidthX() {
        return widthX;
    }

    @Override
    public int getHeightY() {
        return heightY;
    }

    @Override
    public int getLengthZ() {
        return lengthZ;
    }

    @Override
    public long getVolume() {
        return volume;
    }

    @Override
    public boolean contains(int x, int y, int z) {
        return x >= minX && x <= maxX &&
               y >= minY && y <= maxY &&
               z >= minZ && z <= maxZ;
    }

    @Override
    public Iterator<Point> iterator() {
        return new ChunkOrderedIterator();
    }

    /**
     * Chunk-ordered iterator for optimized memory and chunk access.
     */
    private final class ChunkOrderedIterator implements Iterator<Point> {
        private int curX = minX;
        private int curY = minY;
        private int curZ = minZ;

        @Override
        public boolean hasNext() {
            return curX <= maxX && curY <= maxY && curZ <= maxZ;
        }

        @Override
        public Point next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            Point point = new Vec(curX, curY, curZ);

            curX++;
            if (curX > maxX) {
                curX = minX;
                curZ++;
                if (curZ > maxZ) {
                    curZ = minZ;
                    curY++;
                }
            }
            return point;
        }
    }

    @Override
    public String toString() {
        return "CuboidSelection[" +
                "min=(" + minX + "," + minY + "," + minZ + "), " +
                "max=(" + maxX + "," + maxY + "," + maxZ + "), " +
                "volume=" + volume + "]";
    }
}
