package fr.maw.operation;

import fr.maw.async.AsyncEditSession;
import fr.maw.pattern.Pattern;
import fr.maw.pattern.SingleBlockPattern;
import fr.maw.selection.Selection;
import net.minestom.server.instance.block.Block;

/**
 * Hollows out the interior of a selection, leaving a shell of the specified thickness.
 */
public final class HollowOperation {

    private HollowOperation() {}

    public static long execute(AsyncEditSession session, Selection selection, int thickness, Pattern interiorPattern) {
        if (thickness <= 0) thickness = 1;
        if (interiorPattern == null) {
            interiorPattern = new SingleBlockPattern(Block.AIR);
        }

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
                    int distFromEdge = Math.min(
                            Math.min(x - minX, maxX - x),
                            Math.min(Math.min(y - minY, maxY - y), Math.min(z - minZ, maxZ - z))
                    );

                    // If inside the shell
                    if (distFromEdge >= thickness) {
                        Block current = session.getBlock(x, y, z);
                        if (!current.air()) {
                            Block next = interiorPattern.apply(x, y, z, current);
                            session.setBlock(x, y, z, next);
                            count++;
                        }
                    }
                }
            }
        }
        return count;
    }
}
