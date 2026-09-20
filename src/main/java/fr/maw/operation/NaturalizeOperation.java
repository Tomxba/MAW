package fr.maw.operation;

import fr.maw.async.AsyncEditSession;
import fr.maw.selection.Selection;
import net.minestom.server.instance.block.Block;

public final class NaturalizeOperation {

    private NaturalizeOperation() {}

    public static long execute(AsyncEditSession session, Selection selection) {
        long count = 0;

        for (int x = selection.getMinX(); x <= selection.getMaxX(); x++) {
            for (int z = selection.getMinZ(); z <= selection.getMaxZ(); z++) {
                int depth = 0;
                for (int y = selection.getMaxY(); y >= selection.getMinY(); y--) {
                    Block current = session.getBlock(x, y, z);
                    if (current != null && !current.air()) {
                        if (depth == 0) {
                            session.setBlock(x, y, z, Block.GRASS_BLOCK);
                            count++;
                        } else if (depth <= 3) {
                            session.setBlock(x, y, z, Block.DIRT);
                            count++;
                        } else {
                            session.setBlock(x, y, z, Block.STONE);
                            count++;
                        }
                        depth++;
                    } else {
                        depth = 0; // Reset depth if air gap encountered
                    }
                }
            }
        }

        return count;
    }
}
