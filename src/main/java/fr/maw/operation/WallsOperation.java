package fr.maw.operation;

import fr.maw.async.AsyncEditSession;
import fr.maw.pattern.Pattern;
import fr.maw.selection.Selection;
import net.minestom.server.instance.block.Block;

/**
 * Creates the four vertical perimeter walls of a selection.
 */
public final class WallsOperation {

    private WallsOperation() {}

    public static long execute(AsyncEditSession session, Selection selection, Pattern pattern) {
        int minX = selection.getMinX();
        int maxX = selection.getMaxX();
        int minY = selection.getMinY();
        int maxY = selection.getMaxY();
        int minZ = selection.getMinZ();
        int maxZ = selection.getMaxZ();

        long count = 0;
        for (int y = minY; y <= maxY; y++) {
            for (int x = minX; x <= maxX; x++) {
                Block current1 = session.getBlock(x, y, minZ);
                session.setBlock(x, y, minZ, pattern.apply(x, y, minZ, current1));
                count++;

                if (minZ != maxZ) {
                    Block current2 = session.getBlock(x, y, maxZ);
                    session.setBlock(x, y, maxZ, pattern.apply(x, y, maxZ, current2));
                    count++;
                }
            }

            for (int z = minZ + 1; z < maxZ; z++) {
                Block current1 = session.getBlock(minX, y, z);
                session.setBlock(minX, y, z, pattern.apply(minX, y, z, current1));
                count++;

                if (minX != maxX) {
                    Block current2 = session.getBlock(maxX, y, z);
                    session.setBlock(maxX, y, z, pattern.apply(maxX, y, z, current2));
                    count++;
                }
            }
        }
        return count;
    }
}
