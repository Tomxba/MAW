package fr.maw.operation;

import fr.maw.async.AsyncEditSession;
import fr.maw.pattern.Pattern;
import fr.maw.selection.Selection;
import net.minestom.server.instance.block.Block;

import java.util.ArrayList;
import java.util.List;

public final class CenterOperation {

    private CenterOperation() {}

    public static long execute(AsyncEditSession session, Selection selection, Pattern pattern) {
        int minX = selection.getMinX();
        int maxX = selection.getMaxX();
        int minY = selection.getMinY();
        int maxY = selection.getMaxY();
        int minZ = selection.getMinZ();
        int maxZ = selection.getMaxZ();

        List<Integer> xs = getCenters(minX, maxX);
        List<Integer> ys = getCenters(minY, maxY);
        List<Integer> zs = getCenters(minZ, maxZ);

        long count = 0;
        for (int x : xs) {
            for (int y : ys) {
                for (int z : zs) {
                    Block existing = session.getBlock(x, y, z);
                    Block newBlock = pattern.apply(x, y, z, existing);
                    session.setBlock(x, y, z, newBlock);
                    count++;
                }
            }
        }

        return count;
    }

    private static List<Integer> getCenters(int min, int max) {
        int span = max - min + 1;
        if (span % 2 == 1) {
            return List.of(min + span / 2);
        } else {
            return List.of(min + span / 2 - 1, min + span / 2);
        }
    }
}
