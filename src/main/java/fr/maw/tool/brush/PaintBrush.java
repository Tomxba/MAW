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

public class PaintBrush extends Brush {

    public PaintBrush(
            MawConfig config,
            SessionManager sessionManager,
            MawAsyncEngine asyncEngine,
            TickDispatcher dispatcher,
            Pattern pattern,
            int radius
    ) {
        super(config, sessionManager, asyncEngine, dispatcher, pattern, radius, false);
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

                for (int x = cx - radius; x <= cx + radius; x++) {
                    double dx = x - cx;
                    for (int y = cy - radius; y <= cy + radius; y++) {
                        double dy = y - cy;
                        for (int z = cz - radius; z <= cz + radius; z++) {
                            double dz = z - cz;
                            if (dx * dx + dy * dy + dz * dz > rSq) continue;

                            Block existing = editSession.getBlock(x, y, z);
                            if (existing.isAir()) continue;

                            // Check if adjacent to air
                            boolean hasAirNeighbor =
                                    editSession.getBlock(x + 1, y, z).isAir() ||
                                    editSession.getBlock(x - 1, y, z).isAir() ||
                                    editSession.getBlock(x, y + 1, z).isAir() ||
                                    editSession.getBlock(x, y - 1, z).isAir() ||
                                    editSession.getBlock(x, y, z + 1).isAir() ||
                                    editSession.getBlock(x, y, z - 1).isAir();

                            if (hasAirNeighbor) {
                                if (brushMask != null && !brushMask.test(x, y, z, existing)) continue;
                                if (globalMask != null && !globalMask.test(x, y, z, existing)) continue;

                                editSession.setBlock(x, y, z, pattern.apply(x, y, z, existing));
                            }
                        }
                    }
                }

                editSession.commit(dispatcher, session.isUpdatePhysics(), session.isManageEntities()).thenAccept(result -> {
                    session.getHistoryManager().record(editSession.createChangeSet());
                });
            } catch (Exception e) {
                CommandHelper.sendError(player, "Paint brush error: " + e.getMessage());
            }
        });
    }
}
