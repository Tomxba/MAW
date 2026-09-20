package fr.maw.tool.brush;

import fr.maw.MawConfig;
import fr.maw.async.AsyncEditSession;
import fr.maw.async.MawAsyncEngine;
import fr.maw.async.TickDispatcher;
import fr.maw.command.CommandHelper;
import fr.maw.pattern.Mask;
import fr.maw.selection.CuboidSelection;
import fr.maw.session.PlayerSession;
import fr.maw.session.SessionManager;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;

import java.util.ArrayList;
import java.util.List;

public class RaiseLowerBrush extends Brush {

    private final boolean raise;

    public RaiseLowerBrush(
            MawConfig config,
            SessionManager sessionManager,
            MawAsyncEngine asyncEngine,
            TickDispatcher dispatcher,
            int radius,
            boolean raise
    ) {
        super(config, sessionManager, asyncEngine, dispatcher, null, radius, false);
        this.raise = raise;
    }

    @Override
    protected void applyBrush(Player player, Instance instance, Point targetBlock, BlockFace face) {
        PlayerSession session = sessionManager.getSession(player);
        int cx = targetBlock.blockX();
        int cy = targetBlock.blockY();
        int cz = targetBlock.blockZ();

        int minY = Math.max(-64, cy - radius * 2);
        int maxY = Math.min(320, cy + radius * 2);

        Point min = new Vec(cx - radius, minY, cz - radius);
        Point max = new Vec(cx + radius, maxY, cz + radius);
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
                    for (int z = cz - radius; z <= cz + radius; z++) {
                        double distSq = (x - cx) * (x - cx) + (z - cz) * (z - cz);
                        if (distSq > rSq) continue;

                        int topY = minY;
                        Block surfaceBlock = Block.DIRT;
                        for (int y = maxY; y >= minY; y--) {
                            Block b = editSession.getBlock(x, y, z);
                            if (b != null && !b.isAir()) {
                                topY = y;
                                surfaceBlock = b;
                                break;
                            }
                        }

                        if (raise) {
                            if (topY + 1 <= maxY) {
                                Block existing = editSession.getBlock(x, topY + 1, z);
                                if ((brushMask == null || brushMask.test(x, topY + 1, z, existing))
                                        && (globalMask == null || globalMask.test(x, topY + 1, z, existing))) {
                                    editSession.setBlock(x, topY + 1, z, surfaceBlock);
                                }
                            }
                        } else {
                            if (topY >= minY) {
                                Block existing = editSession.getBlock(x, topY, z);
                                if ((brushMask == null || brushMask.test(x, topY, z, existing))
                                        && (globalMask == null || globalMask.test(x, topY, z, existing))) {
                                    editSession.setBlock(x, topY, z, Block.AIR);
                                }
                            }
                        }
                    }
                }

                editSession.commit(dispatcher, session.isUpdatePhysics(), session.isManageEntities()).thenAccept(result -> {
                    session.getHistoryManager().record(editSession.createChangeSet());
                });
            } catch (Exception e) {
                CommandHelper.sendError(player, "Raise/Lower brush error: " + e.getMessage());
            }
        });
    }
}
