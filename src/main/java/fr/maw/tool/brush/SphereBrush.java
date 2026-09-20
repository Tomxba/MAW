package fr.maw.tool.brush;

import fr.maw.MawConfig;
import fr.maw.async.AsyncEditSession;
import fr.maw.async.MawAsyncEngine;
import fr.maw.async.TickDispatcher;
import fr.maw.command.CommandHelper;
import fr.maw.pattern.Mask;
import fr.maw.pattern.Pattern;
import fr.maw.selection.CuboidSelection;
import fr.maw.session.PlayerSession;
import fr.maw.session.SessionManager;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;

public class SphereBrush extends Brush {

    public SphereBrush(
            MawConfig config,
            SessionManager sessionManager,
            MawAsyncEngine asyncEngine,
            TickDispatcher dispatcher,
            Pattern pattern,
            int radius,
            boolean hollow
    ) {
        super(config, sessionManager, asyncEngine, dispatcher, pattern, radius, hollow);
    }

    @Override
    protected void applyBrush(Player player, Instance instance, Point targetBlock, BlockFace face) {
        PlayerSession session = sessionManager.getSession(player);
        int cx = targetBlock.blockX();
        int cy = targetBlock.blockY();
        int cz = targetBlock.blockZ();

        Point min = new Vec(cx - radius, cy - radius, cz - radius);
        Point max = new Vec(cx + radius, cy + radius, cz + radius);
        CuboidSelection selection = new CuboidSelection(min, max);

        Mask brushMask = this.mask != null ? this.mask : session.getBrushMask();
        Mask globalMask = session.getGlobalMask();

        asyncEngine.runAsync(() -> {
            try {
                AsyncEditSession editSession = CommandHelper.newSession(
                        config, player, instance, config.maxBlocksPerOperation(), selection
                );

                double rSq = (radius + 0.5) * (radius + 0.5);
                double innerSq = hollow ? Math.max(0, (radius - 0.5) * (radius - 0.5)) : -1;

                for (int x = cx - radius; x <= cx + radius; x++) {
                    double dx = x - cx;
                    for (int y = cy - radius; y <= cy + radius; y++) {
                        double dy = y - cy;
                        for (int z = cz - radius; z <= cz + radius; z++) {
                            double dz = z - cz;
                            double distSq = dx * dx + dy * dy + dz * dz;

                            if (distSq <= rSq && (!hollow || distSq >= innerSq)) {
                                Block existing = editSession.getBlock(x, y, z);
                                if (brushMask != null && !brushMask.test(x, y, z, existing)) {
                                    continue;
                                }
                                if (globalMask != null && !globalMask.test(x, y, z, existing)) {
                                    continue;
                                }
                                Block newBlock = pattern.apply(x, y, z, existing);
                                editSession.setBlock(x, y, z, newBlock);
                            }
                        }
                    }
                }

                editSession.commit(dispatcher, session.isUpdatePhysics(), session.isManageEntities()).thenAccept(result -> {
                    session.getHistoryManager().record(editSession.createChangeSet());
                });
            } catch (Exception e) {
                CommandHelper.sendError(player, "Brush error: " + e.getMessage());
            }
        });
    }
}
