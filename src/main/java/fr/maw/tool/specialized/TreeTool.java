package fr.maw.tool.specialized;

import fr.maw.MawConfig;
import fr.maw.async.AsyncEditSession;
import fr.maw.async.MawAsyncEngine;
import fr.maw.async.TickDispatcher;
import fr.maw.command.CommandHelper;
import fr.maw.selection.CuboidSelection;
import fr.maw.session.PlayerSession;
import fr.maw.session.SessionManager;
import fr.maw.tool.HandClickType;
import fr.maw.tool.Tool;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;

import java.util.Locale;

public class TreeTool implements Tool {

    private final MawConfig config;
    private final SessionManager sessionManager;
    private final MawAsyncEngine asyncEngine;
    private final TickDispatcher dispatcher;
    private String treeType = "oak";

    public TreeTool(
            MawConfig config,
            SessionManager sessionManager,
            MawAsyncEngine asyncEngine,
            TickDispatcher dispatcher,
            String treeType
    ) {
        this.config = config;
        this.sessionManager = sessionManager;
        this.asyncEngine = asyncEngine;
        this.dispatcher = dispatcher;
        this.treeType = treeType != null ? treeType.toLowerCase(Locale.ROOT) : "oak";
    }

    public String getTreeType() {
        return treeType;
    }

    public void setTreeType(String treeType) {
        this.treeType = treeType != null ? treeType.toLowerCase(Locale.ROOT) : "oak";
    }

    @Override
    public boolean execute(Player player, Instance instance, Point targetBlock, BlockFace face, HandClickType clickType) {
        if (targetBlock == null) return false;

        PlayerSession session = sessionManager.getSession(player);
        int bx = targetBlock.blockX();
        int by = targetBlock.blockY() + 1; // Grow above ground
        int bz = targetBlock.blockZ();

        Block logBlock;
        Block leavesBlock;

        switch (treeType) {
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

        int trunkHeight = 5;
        Point min = new Vec(bx - 3, by, bz - 3);
        Point max = new Vec(bx + 3, by + trunkHeight + 2, bz + 3);
        CuboidSelection sel = new CuboidSelection(min, max);

        asyncEngine.runAsync(() -> {
            try {
                AsyncEditSession editSession = CommandHelper.newSession(config, player, instance, 500, sel);

                // Trunk
                for (int y = 0; y < trunkHeight; y++) {
                    editSession.setBlock(bx, by + y, bz, logBlock);
                }

                // Leaves
                int canopyCenterY = by + trunkHeight - 1;
                for (int dy = -1; dy <= 2; dy++) {
                    int leafRadius = dy == 2 ? 1 : 2;
                    for (int dx = -leafRadius; dx <= leafRadius; dx++) {
                        for (int dz = -leafRadius; dz <= leafRadius; dz++) {
                            if (dx == 0 && dz == 0 && dy < 1) continue; // trunk position
                            if (Math.abs(dx) == leafRadius && Math.abs(dz) == leafRadius && dy >= 1) continue; // round corners

                            int lx = bx + dx;
                            int ly = canopyCenterY + dy;
                            int lz = bz + dz;

                            Block existing = editSession.getBlock(lx, ly, lz);
                            if (existing.isAir()) {
                                editSession.setBlock(lx, ly, lz, leavesBlock);
                            }
                        }
                    }
                }

                editSession.commit(dispatcher, session.isUpdatePhysics(), session.isManageEntities()).thenAccept(res -> {
                    session.getHistoryManager().record(editSession.createChangeSet());
                });
            } catch (Exception e) {
                CommandHelper.sendError(player, "Tree tool error: " + e.getMessage());
            }
        });

        return true;
    }
}
