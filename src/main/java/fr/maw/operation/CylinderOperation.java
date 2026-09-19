package fr.maw.operation;

import fr.maw.async.AsyncEditSession;
import fr.maw.pattern.Pattern;
import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.block.Block;

/**
 * Creates a solid or hollow cylinder centered at a base point.
 */
public final class CylinderOperation {

    private CylinderOperation() {}

    public static long execute(
            AsyncEditSession session,
            Point baseCenter,
            double radiusX,
            double radiusZ,
            int height,
            boolean hollow,
            Pattern pattern
    ) {
        int cx = baseCenter.blockX();
        int cy = baseCenter.blockY();
        int cz = baseCenter.blockZ();

        int ceilRx = (int) Math.ceil(radiusX);
        int ceilRz = (int) Math.ceil(radiusZ);

        double invRx2 = 1.0 / (radiusX * radiusX);
        double invRz2 = 1.0 / (radiusZ * radiusZ);

        double innerInvRx2 = hollow ? 1.0 / Math.max(0.01, (radiusX - 1.0) * (radiusX - 1.0)) : 0;
        double innerInvRz2 = hollow ? 1.0 / Math.max(0.01, (radiusZ - 1.0) * (radiusZ - 1.0)) : 0;

        int startY = height > 0 ? cy : cy + height + 1;
        int endY = height > 0 ? cy + height - 1 : cy;

        long count = 0;
        for (int y = startY; y <= endY; y++) {
            for (int x = cx - ceilRx; x <= cx + ceilRx; x++) {
                double dx = (x - cx) + 0.5;
                double dxNorm = (dx * dx) * invRx2;
                if (dxNorm > 1.0) continue;

                for (int z = cz - ceilRz; z <= cz + ceilRz; z++) {
                    double dz = (z - cz) + 0.5;
                    double distSq = dxNorm + (dz * dz) * invRz2;

                    if (distSq <= 1.0) {
                        if (hollow) {
                            double innerDistSq = (dx * dx) * innerInvRx2 + (dz * dz) * innerInvRz2;
                            if (innerDistSq < 1.0) {
                                continue;
                            }
                        }

                        Block current = session.getBlock(x, y, z);
                        Block next = pattern.apply(x, y, z, current);
                        session.setBlock(x, y, z, next);
                        count++;
                    }
                }
            }
        }
        return count;
    }
}
