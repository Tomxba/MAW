package fr.maw.operation;

import fr.maw.clipboard.Clipboard;
import fr.maw.clipboard.EntityData;
import fr.maw.selection.Selection;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Copies blocks and optional entities from a Selection relative to a reference point (usually player position).
 */
public final class CopyOperation {

    private CopyOperation() {}

    public static Clipboard execute(Instance instance, Selection selection, Point origin, boolean copyEntities) {
        return execute((Block.Getter) instance, selection, origin, copyEntities);
    }

    public static Clipboard execute(Block.Getter getter, Selection selection, Point origin, boolean copyEntities) {
        int ox = origin.blockX();
        int oy = origin.blockY();
        int oz = origin.blockZ();

        Map<Point, Block> blocks = new HashMap<>();

        for (Point p : selection) {
            int x = p.blockX();
            int y = p.blockY();
            int z = p.blockZ();

            Block b = getter.getBlock(x, y, z);
            if (!b.air()) {
                blocks.put(new Vec(x - ox, y - oy, z - oz), b);
            }
        }

        List<EntityData> entityDataList = new ArrayList<>();
        if (copyEntities && getter instanceof Instance instance) {
            int minX = selection.getMinX();
            int maxX = selection.getMaxX();
            int minY = selection.getMinY();
            int maxY = selection.getMaxY();
            int minZ = selection.getMinZ();
            int maxZ = selection.getMaxZ();

            for (Entity entity : instance.getEntities()) {
                if (entity instanceof Player) continue;

                net.minestom.server.coordinate.Pos pos = entity.getPosition();
                double ex = pos.x();
                double ey = pos.y();
                double ez = pos.z();

                if (ex >= minX && ex <= maxX + 1 && ey >= minY && ey <= maxY + 1 && ez >= minZ && ez <= maxZ + 1) {
                    Vec relPos = new Vec(ex - ox, ey - oy, ez - oz);
                    entityDataList.add(new EntityData(entity.getEntityType(), relPos, pos.yaw(), pos.pitch()));
                }
            }
        }

        Point dimensions = new Vec(selection.getWidthX(), selection.getHeightY(), selection.getLengthZ());
        return new Clipboard(blocks, entityDataList, origin, dimensions);
    }
}
