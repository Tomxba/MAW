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

public class GravityBrush extends Brush {

    public GravityBrush(
            MawConfig config,
            SessionManager sessionManager,
            MawAsyncEngine asyncEngine,
            TickDispatcher dispatcher,
            int radius
    ) {
        super(config, sessionManager, asyncEngine, dispatcher, null, radius, false);
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

                        List<Block> nonAir = new ArrayList<>();
                        for (int y = minY; y <= maxY; y++) {
                            Block b = editSession.getBlock(x, y, z);
                            if (b != null && !b.isAir()) {
                                if ((brushMask == null || brushMask.test(x, y, z, b))
                                        && (globalMask == null || globalMask.test(x, y, z, b))) {
                                    nonAir.add(b);
                                }
                            }
                        }

                        // Write compacted blocks at the bottom of the column
                        for (int i = 0; i < nonAir.size(); i++) {
                            editSession.setBlock(x, minY + i, z, nonAir.get(i));
                        }
                        // Clear the rest with air
                        for (int y = minY + nonAir.size(); y <= maxY; y++) {
                            if (!editSession.getBlock(x, y, z).isAir()) {
                                editSession.setBlock(x, y, z, Block.AIR);
                            }
                        }
                    }
                }

                editSession.commit(dispatcher, session.isUpdatePhysics(), session.isManageEntities()).thenAccept(result -> {
                    session.getHistoryManager().record(editSession.createChangeSet());
                });
            } catch (Exception e) {
                CommandHelper.sendError(player, "Gravity brush error: " + e.getMessage());
            }
        });
    }
}
