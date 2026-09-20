package fr.maw.operation;

import fr.maw.async.AsyncEditSession;
import fr.maw.clipboard.Transform.Direction;
import fr.maw.selection.Selection;
import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.block.Block;

import java.util.HashMap;
import java.util.Map;

public final class MoveOperation {

    private MoveOperation() {}

    public static long execute(
            AsyncEditSession session,
            Selection selection,
            int distance,
            Direction direction,
            boolean clearSource
    ) {
        int dx = 0, dy = 0, dz = 0;
        switch (direction) {
            case NORTH -> dz = -distance;
            case SOUTH -> dz = distance;
            case EAST -> dx = distance;
            case WEST -> dx = -distance;
            case UP -> dy = distance;
            case DOWN -> dy = -distance;
        }

        // 1. Read all blocks from selection
        Map<Point, Block> sourceBlocks = new HashMap<>();
        for (Point p : selection) {
            Block b = session.getBlock(p.blockX(), p.blockY(), p.blockZ());
            if (!b.air()) {
                sourceBlocks.put(p, b);
            }
        }

        // 2. Clear source if requested
        if (clearSource) {
            for (Point p : sourceBlocks.keySet()) {
                session.setBlock(p.blockX(), p.blockY(), p.blockZ(), Block.AIR);
            }
        }

        // 3. Write blocks at target offset
        long count = 0;
        for (var entry : sourceBlocks.entrySet()) {
            Point p = entry.getKey();
            session.setBlock(p.blockX() + dx, p.blockY() + dy, p.blockZ() + dz, entry.getValue());
            count++;
        }

        return count;
    }
}
