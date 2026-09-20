package fr.maw.operation;

import fr.maw.async.AsyncEditSession;
import fr.maw.selection.Selection;
import net.minestom.server.instance.block.Block;

import java.util.List;
import java.util.Random;

public final class FloraOperation {

    private FloraOperation() {}

    private static final List<Block> FLOWERS = List.of(
            Block.SHORT_GRASS,
            Block.SHORT_GRASS,
            Block.SHORT_GRASS,
            Block.DANDELION,
            Block.POPPY,
            Block.CORNFLOWER,
            Block.ALLIUM
    );

    public static long execute(AsyncEditSession session, Selection selection, double density) {
        Random rand = new Random(1337);
        long count = 0;

        for (int x = selection.getMinX(); x <= selection.getMaxX(); x++) {
            for (int z = selection.getMinZ(); z <= selection.getMaxZ(); z++) {
                if (rand.nextDouble() > density) continue;

                for (int y = selection.getMaxY() - 1; y >= selection.getMinY(); y--) {
                    Block ground = session.getBlock(x, y, z);
                    if (ground.compare(Block.GRASS_BLOCK)) {
                        Block above = session.getBlock(x, y + 1, z);
                        if (above.isAir()) {
                            Block flower = FLOWERS.get(rand.nextInt(FLOWERS.size()));
                            session.setBlock(x, y + 1, z, flower);
                            count++;
                        }
                        break;
                    }
                }
            }
        }

        return count;
    }
}
