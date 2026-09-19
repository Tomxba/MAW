package fr.maw.objects;

import fr.maw.selection.Selection;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.ItemEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;

import java.util.Collection;
import java.util.Set;

/**
 * Handles surrounding entities and neighboring block updates (Option 3).
 * Provides clean entity management and boundary-limited physics updates.
 */
public final class SurroundingObjectHandler {

    private SurroundingObjectHandler() {}

    /**
     * Cleans up unwanted entities (e.g. dropped items and orphaned hanging entities) in the selection area before block modification.
     */
    public static void handlePreBlockChanges(Instance instance, Selection selection, boolean manageEntities) {
        if (!manageEntities || instance == null || selection == null) return;

        int minX = selection.getMinX();
        int maxX = selection.getMaxX();
        int minY = selection.getMinY();
        int maxY = selection.getMaxY();
        int minZ = selection.getMinZ();
        int maxZ = selection.getMaxZ();

        for (Entity entity : instance.getEntities()) {
            if (entity instanceof Player) continue;

            Point pos = entity.getPosition();
            int ex = pos.blockX();
            int ey = pos.blockY();
            int ez = pos.blockZ();

            if (ex >= minX && ex <= maxX && ey >= minY && ey <= maxY && ez >= minZ && ez <= maxZ) {
                // Remove item drops to prevent item spam lag
                if (entity instanceof ItemEntity) {
                    entity.remove();
                }
            }
        }
    }

    /**
     * Applies neighbor block updates or connection recalculation on boundary blocks if updatePhysics (-u) is enabled.
     */
    public static void handlePostBlockChanges(Instance instance, Collection<Point> boundaryBlocks, boolean updatePhysics) {
        if (!updatePhysics || instance == null || boundaryBlocks == null || boundaryBlocks.isEmpty()) {
            return;
        }

        // Apply neighbor updates only to boundary blocks to protect performance
        for (Point p : boundaryBlocks) {
            int x = p.blockX();
            int y = p.blockY();
            int z = p.blockZ();

            updateNeighborConnection(instance, x + 1, y, z);
            updateNeighborConnection(instance, x - 1, y, z);
            updateNeighborConnection(instance, x, y + 1, z);
            updateNeighborConnection(instance, x, y - 1, z);
            updateNeighborConnection(instance, x, y, z + 1);
            updateNeighborConnection(instance, x, y, z - 1);
        }
    }

    private static void updateNeighborConnection(Instance instance, int x, int y, int z) {
        Block block = instance.getBlock(x, y, z);
        if (block == null || block.air()) return;

        // If block is a fence, wall, pane, or chest, recalculating properties updates its visual connection
        String name = block.name();
        if (name.contains("fence") || name.contains("wall") || name.contains("pane")) {
            Block updated = recalculateConnections(instance, x, y, z, block);
            if (!updated.equals(block)) {
                instance.setBlock(x, y, z, updated);
            }
        }
    }

    private static Block recalculateConnections(Instance instance, int x, int y, int z, Block block) {
        // Dynamic connection properties for fences/walls/panes (north, south, east, west)
        if (block.getProperty("north") != null) {
            boolean north = canConnect(instance.getBlock(x, y, z - 1));
            boolean south = canConnect(instance.getBlock(x, y, z + 1));
            boolean east = canConnect(instance.getBlock(x + 1, y, z));
            boolean west = canConnect(instance.getBlock(x - 1, y, z));

            return block
                    .withProperty("north", north ? "true" : "false")
                    .withProperty("south", south ? "true" : "false")
                    .withProperty("east", east ? "true" : "false")
                    .withProperty("west", west ? "true" : "false");
        }
        return block;
    }

    private static boolean canConnect(Block neighbor) {
        if (neighbor == null || neighbor.air()) return false;
        return neighbor.name().contains("fence") || neighbor.name().contains("wall") || neighbor.name().contains("pane") || !neighbor.air();
    }
}
