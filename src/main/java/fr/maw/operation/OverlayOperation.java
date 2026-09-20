package fr.maw.operation;

import fr.maw.async.AsyncEditSession;
import fr.maw.pattern.Pattern;
import fr.maw.selection.Selection;
import net.minestom.server.instance.block.Block;

public final class OverlayOperation {

    private OverlayOperation() {}

    public static long execute(AsyncEditSession session, Selection selection, Pattern pattern) {
        long count = 0;

        for (int x = selection.getMinX(); x <= selection.getMaxX(); x++) {
            for (int z = selection.getMinZ(); z <= selection.getMaxZ(); z++) {
                for (int y = selection.getMaxY(); y >= selection.getMinY(); y--) {
                    Block current = session.getBlock(x, y, z);
                    if (current != null && !current.isAir()) {
                        // Check block immediately above
                        int targetY = y + 1;
                        Block above = session.getBlock(x, targetY, z);
                        if (above == null || above.isAir()) {
                            Block newBlock = pattern.apply(x, targetY, z, Block.AIR);
                            session.setBlock(x, targetY, z, newBlock);
                            count++;
                        }
                        break;
                    }
                }
            }
        }

        return count;
    }
}
