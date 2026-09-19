package fr.maw.operation;

import fr.maw.async.AsyncEditSession;
import fr.maw.pattern.Mask;
import fr.maw.pattern.Pattern;
import fr.maw.selection.Selection;
import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.block.Block;

/**
 * Replaces blocks in a selection matching a Mask with a Pattern.
 */
public final class ReplaceOperation {

    private ReplaceOperation() {}

    public static long execute(AsyncEditSession session, Selection selection, Mask mask, Pattern pattern) {
        long count = 0;
        for (Point p : selection) {
            int x = p.blockX();
            int y = p.blockY();
            int z = p.blockZ();

            Block current = session.getBlock(x, y, z);
            if (mask.test(x, y, z, current)) {
                Block next = pattern.apply(x, y, z, current);
                session.setBlock(x, y, z, next);
                count++;
            }
        }
        return count;
    }
}
