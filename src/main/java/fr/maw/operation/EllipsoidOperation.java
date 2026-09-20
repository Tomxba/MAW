package fr.maw.operation;

import fr.maw.async.AsyncEditSession;
import fr.maw.pattern.Pattern;
import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.block.Block;

public final class EllipsoidOperation {

    private EllipsoidOperation() {}

    public static long execute(
            AsyncEditSession session,
            Point origin,
            Pattern pattern,
            int rx,
            int ry,
            int rz,
            boolean hollow
    ) {
        int cx = origin.blockX();
        int cy = origin.blockY();
        int cz = origin.blockZ();

        double invRx2 = 1.0 / ((rx + 0.5) * (rx + 0.5));
        double invRy2 = 1.0 / ((ry + 0.5) * (ry + 0.5));
        double invRz2 = 1.0 / ((rz + 0.5) * (rz + 0.5));

        double innerInvRx2 = hollow ? 1.0 / (Math.max(0.5, rx - 0.5) * Math.max(0.5, rx - 0.5)) : 0;
        double innerInvRy2 = hollow ? 1.0 / (Math.max(0.5, ry - 0.5) * Math.max(0.5, ry - 0.5)) : 0;
        double innerInvRz2 = hollow ? 1.0 / (Math.max(0.5, rz - 0.5) * Math.max(0.5, rz - 0.5)) : 0;

        long count = 0;
        for (int x = cx - rx; x <= cx + rx; x++) {
            double dx = x - cx;
            double termX = dx * dx * invRx2;
            double innerTermX = dx * dx * innerInvRx2;

            for (int y = cy - ry; y <= cy + ry; y++) {
                double dy = y - cy;
                double termXY = termX + dy * dy * invRy2;
                double innerTermXY = innerTermX + dy * dy * innerInvRy2;
                if (termXY > 1.0) continue;

                for (int z = cz - rz; z <= cz + rz; z++) {
                    double dz = z - cz;
                    double termXYZ = termXY + dz * dz * invRz2;

                    if (termXYZ <= 1.0) {
                        if (hollow && (innerTermXY + dz * dz * innerInvRz2) < 1.0) {
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
