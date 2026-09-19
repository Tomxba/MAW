package fr.maw.operation;

import fr.maw.async.AsyncEditSession;
import fr.maw.pattern.Pattern;
import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.block.Block;

/**
 * Creates a solid or hollow sphere/ellipsoid centered at a point.
 */
public final class SphereOperation {

    private SphereOperation() {}

    public static long execute(
            AsyncEditSession session,
            Point center,
            double radiusX,
            double radiusY,
            double radiusZ,
            boolean hollow,
            Pattern pattern
    ) {
        int cx = center.blockX();
        int cy = center.blockY();
        int cz = center.blockZ();

        int ceilRx = (int) Math.ceil(radiusX);
        int ceilRy = (int) Math.ceil(radiusY);
        int ceilRz = (int) Math.ceil(radiusZ);

        double invRx2 = 1.0 / (radiusX * radiusX);
        double invRy2 = 1.0 / (radiusY * radiusY);
        double invRz2 = 1.0 / (radiusZ * radiusZ);

        double innerInvRx2 = hollow ? 1.0 / Math.max(0.01, (radiusX - 1.0) * (radiusX - 1.0)) : 0;
        double innerInvRy2 = hollow ? 1.0 / Math.max(0.01, (radiusY - 1.0) * (radiusY - 1.0)) : 0;
        double innerInvRz2 = hollow ? 1.0 / Math.max(0.01, (radiusZ - 1.0) * (radiusZ - 1.0)) : 0;

        long count = 0;
        for (int x = cx - ceilRx; x <= cx + ceilRx; x++) {
            double dx = (x - cx) + 0.5;
            double dxNorm = (dx * dx) * invRx2;
            if (dxNorm > 1.0) continue;

            for (int y = cy - ceilRy; y <= cy + ceilRy; y++) {
                double dy = (y - cy) + 0.5;
                double dxyNorm = dxNorm + (dy * dy) * invRy2;
                if (dxyNorm > 1.0) continue;

                for (int z = cz - ceilRz; z <= cz + ceilRz; z++) {
                    double dz = (z - cz) + 0.5;
                    double distSq = dxyNorm + (dz * dz) * invRz2;

                    if (distSq <= 1.0) {
                        if (hollow) {
                            double innerDistSq = (dx * dx) * innerInvRx2 + (dy * dy) * innerInvRy2 + (dz * dz) * innerInvRz2;
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
