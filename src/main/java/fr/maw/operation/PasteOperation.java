package fr.maw.operation;

import fr.maw.async.AsyncEditSession;
import fr.maw.clipboard.Clipboard;
import fr.maw.clipboard.EntityData;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;

import java.util.Map;

/**
 * Pastes blocks and optional entities from a Clipboard relative to a target point.
 */
public final class PasteOperation {

    private PasteOperation() {}

    public static long execute(
            AsyncEditSession session,
            Clipboard clipboard,
            Point targetOrigin,
            boolean ignoreAir,
            boolean pasteEntities
    ) {
        int tx = targetOrigin.blockX();
        int ty = targetOrigin.blockY();
        int tz = targetOrigin.blockZ();

        long count = 0;
        for (Map.Entry<Point, Block> entry : clipboard.getBlocks().entrySet()) {
            Point rel = entry.getKey();
            Block block = entry.getValue();

            if (ignoreAir && block.air()) {
                continue;
            }

            int x = tx + rel.blockX();
            int y = ty + rel.blockY();
            int z = tz + rel.blockZ();

            session.setBlock(x, y, z, block);
            count++;
        }

        if (pasteEntities && clipboard.hasEntities()) {
            Instance instance = session.getInstance();
            for (EntityData ed : clipboard.getEntities()) {
                Point spawnPos = new Pos(
                        tx + ed.relativePosition().x(),
                        ty + ed.relativePosition().y(),
                        tz + ed.relativePosition().z(),
                        ed.yaw(),
                        ed.pitch()
                );
                Entity entity = new Entity(ed.entityType());
                entity.setInstance(instance, spawnPos);
            }
        }

        return count;
    }
}
