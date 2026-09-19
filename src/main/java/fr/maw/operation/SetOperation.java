package fr.maw.operation;

import fr.maw.async.AsyncEditSession;
import fr.maw.pattern.Pattern;
import fr.maw.selection.Selection;
import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.block.Block;

/**
 * Fills a selection with a Pattern.
 */
public final class SetOperation {

    private SetOperation() {}

    public static long execute(AsyncEditSession session, Selection selection, Pattern pattern) {
        long count = 0;
        for (Point p : selection) {
            int x = p.blockX();
            int y = p.blockY();
            int z = p.blockZ();

            Block current = session.getBlock(x, y, z);
            Block next = pattern.apply(x, y, z, current);
            session.setBlock(x, y, z, next);
            count++;
        }
        return count;
    }
}
