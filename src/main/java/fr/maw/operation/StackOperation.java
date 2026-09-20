package fr.maw.operation;

import fr.maw.async.AsyncEditSession;
import fr.maw.clipboard.Transform.Direction;
import fr.maw.selection.Selection;
import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.block.Block;

import java.util.HashMap;
import java.util.Map;

public final class StackOperation {

    private StackOperation() {}

    public static long execute(
            AsyncEditSession session,
            Selection selection,
            int repeatCount,
            Direction direction
    ) {
        int stepX = 0, stepY = 0, stepZ = 0;
        switch (direction) {
            case NORTH -> stepZ = -selection.getLengthZ();
            case SOUTH -> stepZ = selection.getLengthZ();
            case EAST -> stepX = selection.getWidthX();
            case WEST -> stepX = -selection.getWidthX();
            case UP -> stepY = selection.getHeightY();
            case DOWN -> stepY = -selection.getHeightY();
        }

        // Cache existing blocks in selection
        Map<Point, Block> sourceBlocks = new HashMap<>();
        for (Point p : selection) {
            Block b = session.getBlock(p.blockX(), p.blockY(), p.blockZ());
            if (!b.air()) {
                sourceBlocks.put(p, b);
            }
        }

        long placed = 0;
        for (int i = 1; i <= repeatCount; i++) {
            int offX = stepX * i;
            int offY = stepY * i;
            int offZ = stepZ * i;

            for (var entry : sourceBlocks.entrySet()) {
                Point p = entry.getKey();
                session.setBlock(p.blockX() + offX, p.blockY() + offY, p.blockZ() + offZ, entry.getValue());
                placed++;
            }
        }

        return placed;
    }
}
