package fr.maw.operation;

import fr.maw.async.AsyncEditSession;
import fr.maw.pattern.Pattern;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.block.Block;

import java.util.HashSet;
import java.util.Set;

public final class LineOperation {

    private LineOperation() {}

    public static long execute(
            AsyncEditSession session,
            Point pos1,
            Point pos2,
            Pattern pattern
    ) {
        return execute(session, pos1, pos2, pattern, 0);
    }

    public static long execute(
            AsyncEditSession session,
            Point pos1,
            Point pos2,
            Pattern pattern,
            int thickness
    ) {
        double dx = pos2.x() - pos1.x();
        double dy = pos2.y() - pos1.y();
        double dz = pos2.z() - pos1.z();
        double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);

        int steps = (int) Math.ceil(distance * 2) + 1;
        Set<Vec> points = new HashSet<>();

        double rSq = (thickness + 0.5) * (thickness + 0.5);

        for (int i = 0; i <= steps; i++) {
            double t = steps == 0 ? 0 : (double) i / steps;
            double lx = pos1.x() + dx * t;
            double ly = pos1.y() + dy * t;
            double lz = pos1.z() + dz * t;

            int bx = (int) Math.round(lx);
            int by = (int) Math.round(ly);
            int bz = (int) Math.round(lz);

            if (thickness <= 0) {
                points.add(new Vec(bx, by, bz));
            } else {
                for (int ox = -thickness; ox <= thickness; ox++) {
                    for (int oy = -thickness; oy <= thickness; oy++) {
                        for (int oz = -thickness; oz <= thickness; oz++) {
                            if (ox * ox + oy * oy + oz * oz <= rSq) {
                                points.add(new Vec(bx + ox, by + oy, bz + oz));
                            }
                        }
                    }
                }
            }
        }

        long count = 0;
        for (Vec p : points) {
            Block existing = session.getBlock(p.blockX(), p.blockY(), p.blockZ());
            Block newBlock = pattern.apply(p.blockX(), p.blockY(), p.blockZ(), existing);
            session.setBlock(p.blockX(), p.blockY(), p.blockZ(), newBlock);
            count++;
        }

        return count;
    }
}
