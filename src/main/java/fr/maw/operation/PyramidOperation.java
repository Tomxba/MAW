package fr.maw.operation;

import fr.maw.async.AsyncEditSession;
import fr.maw.pattern.Pattern;
import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.block.Block;

public final class PyramidOperation {

    private PyramidOperation() {}

    public static long execute(
            AsyncEditSession session,
            Point origin,
            Pattern pattern,
            int size,
            boolean hollow
    ) {
        int cx = origin.blockX();
        int cy = origin.blockY();
        int cz = origin.blockZ();

        long count = 0;
        for (int y = 0; y < size; y++) {
            int r = size - 1 - y;
            int currentY = cy + y;

            for (int x = cx - r; x <= cx + r; x++) {
                for (int z = cz - r; z <= cz + r; z++) {
                    int dx = Math.abs(x - cx);
                    int dz = Math.abs(z - cz);

                    if (hollow && y > 0 && dx < r && dz < r) {
                        continue;
                    }

                    Block existing = session.getBlock(x, currentY, z);
                    Block newBlock = pattern.apply(x, currentY, z, existing);
                    session.setBlock(x, currentY, z, newBlock);
                    count++;
                }
            }
        }

        return count;
    }
}
