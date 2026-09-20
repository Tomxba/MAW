package fr.maw.operation;

import fr.maw.async.AsyncEditSession;
import fr.maw.selection.Selection;
import net.minestom.server.instance.block.Block;

import java.util.Locale;
import java.util.Random;

public final class ForestOperation {

    private ForestOperation() {}

    public static long execute(AsyncEditSession session, Selection selection, String treeType, double density) {
        String type = treeType != null ? treeType.toLowerCase(Locale.ROOT) : "oak";
        Block logBlock;
        Block leavesBlock;

        switch (type) {
            case "birch" -> {
                logBlock = Block.BIRCH_LOG;
                leavesBlock = Block.BIRCH_LEAVES;
            }
            case "spruce" -> {
                logBlock = Block.SPRUCE_LOG;
                leavesBlock = Block.SPRUCE_LEAVES;
            }
            case "jungle" -> {
                logBlock = Block.JUNGLE_LOG;
                leavesBlock = Block.JUNGLE_LEAVES;
            }
            case "acacia" -> {
                logBlock = Block.ACACIA_LOG;
                leavesBlock = Block.ACACIA_LEAVES;
            }
            case "dark_oak" -> {
                logBlock = Block.DARK_OAK_LOG;
                leavesBlock = Block.DARK_OAK_LEAVES;
            }
            default -> {
                logBlock = Block.OAK_LOG;
                leavesBlock = Block.OAK_LEAVES;
            }
        }

        Random rand = new Random(42);
        long treesPlanted = 0;

        for (int x = selection.getMinX() + 2; x <= selection.getMaxX() - 2; x += 3) {
            for (int z = selection.getMinZ() + 2; z <= selection.getMaxZ() - 2; z += 3) {
                if (rand.nextDouble() > density) continue;

                // Find surface
                for (int y = selection.getMaxY() - 6; y >= selection.getMinY(); y--) {
                    Block ground = session.getBlock(x, y, z);
                    if (ground.compare(Block.GRASS_BLOCK) || ground.compare(Block.DIRT)) {
                        int trunkY = y + 1;
                        int trunkH = 5;

                        // Trunk
                        for (int ty = 0; ty < trunkH; ty++) {
                            session.setBlock(x, trunkY + ty, z, logBlock);
                        }

                        // Leaves
                        int cy = trunkY + trunkH - 1;
                        for (int dy = -1; dy <= 2; dy++) {
                            int lr = dy == 2 ? 1 : 2;
                            for (int dx = -lr; dx <= lr; dx++) {
                                for (int dz = -lr; dz <= lr; dz++) {
                                    if (dx == 0 && dz == 0 && dy < 1) continue;
                                    if (Math.abs(dx) == lr && Math.abs(dz) == lr && dy >= 1) continue;

                                    if (session.getBlock(x + dx, cy + dy, z + dz).isAir()) {
                                        session.setBlock(x + dx, cy + dy, z + dz, leavesBlock);
                                    }
                                }
                            }
                        }
                        treesPlanted++;
                        break;
                    }
                }
            }
        }

        return treesPlanted;
    }
}
