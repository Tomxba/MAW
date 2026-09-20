package fr.maw.operation;

import fr.maw.async.AsyncEditSession;
import fr.maw.pattern.Pattern;
import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.block.Block;

public final class ConeOperation {

    private ConeOperation() {}

    public static long execute(
            AsyncEditSession session,
            Point origin,
            Pattern pattern,
            int radius,
            int height,
            boolean hollow
    ) {
        int cx = origin.blockX();
        int cy = origin.blockY();
        int cz = origin.blockZ();

        long count = 0;
        for (int y = 0; y < height; y++) {
            double currentRadius = radius * (1.0 - (double) y / height);
            double rSq = (currentRadius + 0.5) * (currentRadius + 0.5);
            double innerSq = hollow ? Math.max(0, (currentRadius - 0.5) * (currentRadius - 0.5)) : -1;
            int bound = (int) Math.ceil(currentRadius);
            int currentY = cy + y;

            for (int x = cx - bound; x <= cx + bound; x++) {
                double dx = x - cx;
                for (int z = cz - bound; z <= cz + bound; z++) {
                    double dz = z - cz;
                    double distSq = dx * dx + dz * dz;

                    if (distSq <= rSq) {
                        if (hollow && y > 0 && distSq < innerSq) {
                            continue;
                        }
                        Block existing = session.getBlock(x, currentY, z);
                        Block newBlock = pattern.apply(x, currentY, z, existing);
                        session.setBlock(x, currentY, z, newBlock);
                        count++;
                    }
                }
            }
        }

        return count;
    }
}
