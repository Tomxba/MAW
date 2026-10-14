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

    /** A hollow cylinder with a wall of one block: what {@code //hcyl} builds without a thickness. */
    public static long execute(
            AsyncEditSession session,
            Point baseCenter,
            double radiusX,
            double radiusZ,
            int height,
            boolean hollow,
            Pattern pattern
    ) {
        return execute(session, baseCenter, radiusX, radiusZ, height, hollow, pattern, 1);
    }

    /**
     * Creates a cylinder: solid, or hollow with a wall {@code thickness} blocks thick.
     * <p>
     * A thickness of 1 builds the single-block wall of WorldEdit's {@code //hcyl}; a bigger one builds a tube
     * of that thickness ({@code //hcyl stone 10 20 3} : radius 10, height 20, a wall of 3 blocks), and a
     * thickness that reaches the axis fills the cylinder entirely, as if it were not hollow.
     */
    public static long execute(
            AsyncEditSession session,
            Point baseCenter,
            double radiusX,
            double radiusZ,
            int height,
            boolean hollow,
            Pattern pattern,
            int thickness
    ) {
        int cx = baseCenter.blockX();
        int cy = baseCenter.blockY();
        int cz = baseCenter.blockZ();

        int ceilRx = (int) Math.ceil(radiusX);
        int ceilRz = (int) Math.ceil(radiusZ);

        double invRx2 = 1.0 / (radiusX * radiusX);
        double invRz2 = 1.0 / (radiusZ * radiusZ);

        // The hole of a hollow cylinder is what is left of the disc once the thickness of the wall is taken
        // off the radius. A wall as thick as the radius leaves no hole: the cylinder is solid.
        double wall = Math.max(1, thickness);
        double holeRadiusX = Math.max(0, radiusX - wall);
        double holeRadiusZ = Math.max(0, radiusZ - wall);
        boolean hasHole = hollow && holeRadiusX > 0 && holeRadiusZ > 0;

        double holeInvRx2 = hasHole ? 1.0 / (holeRadiusX * holeRadiusX) : 0;
        double holeInvRz2 = hasHole ? 1.0 / (holeRadiusZ * holeRadiusZ) : 0;

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
                        if (hasHole) {
                            double holeDistSq = (dx * dx) * holeInvRx2 + (dz * dz) * holeInvRz2;
                            if (holeDistSq < 1.0) {
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
