package fr.maw.operation;

import fr.maw.async.AsyncEditSession;
import fr.maw.pattern.Pattern;
import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.block.Block;

public final class TorusOperation {

    private TorusOperation() {}

    public static long execute(
            AsyncEditSession session,
            Point origin,
            Pattern pattern,
            int majorRadius,
            int minorRadius,
            boolean hollow
    ) {
        int cx = origin.blockX();
        int cy = origin.blockY();
        int cz = origin.blockZ();

        int maxHBound = majorRadius + minorRadius;
        double minorSq = (minorRadius + 0.5) * (minorRadius + 0.5);
        double innerMinorSq = hollow ? Math.max(0, (minorRadius - 0.5) * (minorRadius - 0.5)) : -1;

        long count = 0;
        for (int x = cx - maxHBound; x <= cx + maxHBound; x++) {
            double dx = x - cx;
            for (int z = cz - maxHBound; z <= cz + maxHBound; z++) {
                double dz = z - cz;
                double hDist = Math.sqrt(dx * dx + dz * dz);
                double tubeDist = hDist - majorRadius;

                for (int y = cy - minorRadius; y <= cy + minorRadius; y++) {
                    double dy = y - cy;
                    double distSq = tubeDist * tubeDist + dy * dy;

                    if (distSq <= minorSq) {
                        if (hollow && distSq < innerMinorSq) {
                            continue;
                        }
                        Block existing = session.getBlock(x, y, z);
                        Block newBlock = pattern.apply(x, y, z, existing);
                        session.setBlock(x, y, z, newBlock);
                        count++;
                    }
                }
            }
        }

        return count;
    }
}
