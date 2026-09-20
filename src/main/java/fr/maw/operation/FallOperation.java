package fr.maw.operation;

import fr.maw.async.AsyncEditSession;
import fr.maw.selection.Selection;
import net.minestom.server.instance.block.Block;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class FallOperation {

    private FallOperation() {}

    private static boolean isGravityBlock(Block block) {
        if (block == null || block.isAir()) return false;
        String name = block.name().toLowerCase(Locale.ROOT);
        return name.contains("sand") || name.contains("gravel") || name.contains("concrete_powder") || name.contains("anvil");
    }

    public static long execute(AsyncEditSession session, Selection selection) {
        long count = 0;

        for (int x = selection.getMinX(); x <= selection.getMaxX(); x++) {
            for (int z = selection.getMinZ(); z <= selection.getMaxZ(); z++) {
                List<Block> column = new ArrayList<>();
                for (int y = selection.getMinY(); y <= selection.getMaxY(); y++) {
                    Block b = session.getBlock(x, y, z);
                    column.add(b);
                }

                // Compact gravity blocks down over empty spaces
                boolean changed = false;
                for (int i = 0; i < column.size(); i++) {
                    if (isGravityBlock(column.get(i))) {
                        // find lowest air slot below it
                        int targetIdx = i;
                        while (targetIdx > 0 && column.get(targetIdx - 1).isAir()) {
                            targetIdx--;
                        }
                        if (targetIdx != i) {
                            column.set(targetIdx, column.get(i));
                            column.set(i, Block.AIR);
                            changed = true;
                            count++;
                        }
                    }
                }

                if (changed) {
                    for (int i = 0; i < column.size(); i++) {
                        session.setBlock(x, selection.getMinY() + i, z, column.get(i));
                    }
                }
            }
        }

        return count;
    }
}
