package fr.maw.operation;

import fr.maw.async.AsyncEditSession;
import fr.maw.pattern.Pattern;
import fr.maw.selection.Selection;
import net.minestom.server.instance.block.Block;

/**
 * Creates the 6 bounding faces of a selection (a hollow box).
 */
public final class FacesOperation {

    private FacesOperation() {}

    public static long execute(AsyncEditSession session, Selection selection, Pattern pattern) {
        int minX = selection.getMinX();
        int maxX = selection.getMaxX();
        int minY = selection.getMinY();
        int maxY = selection.getMaxY();
        int minZ = selection.getMinZ();
        int maxZ = selection.getMaxZ();

        long count = 0;
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    boolean isFace = (x == minX || x == maxX ||
                                      y == minY || y == maxY ||
                                      z == minZ || z == maxZ);
                    if (isFace) {
                        Block current = session.getBlock(x, y, z);
                        session.setBlock(x, y, z, pattern.apply(x, y, z, current));
                        count++;
                    }
                }
            }
        }
        return count;
    }
}
