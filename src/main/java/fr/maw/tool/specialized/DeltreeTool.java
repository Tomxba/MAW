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

import java.util.*;

public class DeltreeTool implements Tool {

    private final MawConfig config;
    private final SessionManager sessionManager;
    private final MawAsyncEngine asyncEngine;
    private final TickDispatcher dispatcher;

    public DeltreeTool(
            MawConfig config,
            SessionManager sessionManager,
            MawAsyncEngine asyncEngine,
            TickDispatcher dispatcher
    ) {
        this.config = config;
        this.sessionManager = sessionManager;
        this.asyncEngine = asyncEngine;
        this.dispatcher = dispatcher;
    }

    private boolean isTreeBlock(Block block) {
        if (block == null || block.isAir()) return false;
        String name = block.name().toLowerCase(Locale.ROOT);
        return name.contains("log") || name.contains("wood") || name.contains("leaves") || name.contains("mangrove_roots");
    }

    @Override
    public boolean execute(Player player, Instance instance, Point targetBlock, BlockFace face, HandClickType clickType) {
        if (targetBlock == null) return false;

        Block rootBlock = instance.getBlock(targetBlock.blockX(), targetBlock.blockY(), targetBlock.blockZ());
        if (!isTreeBlock(rootBlock)) {
            CommandHelper.sendInfo(player, "That's not a tree block!");
            return true;
        }

        PlayerSession session = sessionManager.getSession(player);
        int sx = targetBlock.blockX();
        int sy = targetBlock.blockY();
        int sz = targetBlock.blockZ();

        asyncEngine.runAsync(() -> {
            try {
                // BFS to find connected tree blocks (max 500 blocks)
                Set<Vec> visited = new HashSet<>();
                Queue<Vec> queue = new ArrayDeque<>();
                Vec start = new Vec(sx, sy, sz);
                visited.add(start);
                queue.add(start);

                int minX = sx, maxX = sx;
                int minY = sy, maxY = sy;
                int minZ = sz, maxZ = sz;

                int limit = 500;
                while (!queue.isEmpty() && visited.size() < limit) {
                    Vec current = queue.poll();
                    minX = Math.min(minX, current.blockX());
                    maxX = Math.max(maxX, current.blockX());
                    minY = Math.min(minY, current.blockY());
                    maxY = Math.max(maxY, current.blockY());
                    minZ = Math.min(minZ, current.blockZ());
                    maxZ = Math.max(maxZ, current.blockZ());

                    for (int dx = -1; dx <= 1; dx++) {
                        for (int dy = -1; dy <= 1; dy++) {
                            for (int dz = -1; dz <= 1; dz++) {
                                if (dx == 0 && dy == 0 && dz == 0) continue;
                                Vec neighbor = new Vec(current.blockX() + dx, current.blockY() + dy, current.blockZ() + dz);
                                if (!visited.contains(neighbor)) {
                                    Block b = instance.getBlock(neighbor.blockX(), neighbor.blockY(), neighbor.blockZ());
                                    if (isTreeBlock(b)) {
                                        visited.add(neighbor);
                                        queue.add(neighbor);
                                    }
                                }
                            }
                        }
                    }
                }

                CuboidSelection sel = new CuboidSelection(new Vec(minX, minY, minZ), new Vec(maxX, maxY, maxZ));
                AsyncEditSession editSession = CommandHelper.newSession(config, player, instance, visited.size() + 10, sel);
                for (Vec p : visited) {
                    editSession.setBlock(p.blockX(), p.blockY(), p.blockZ(), Block.AIR);
                }

                editSession.commit(dispatcher, session.isUpdatePhysics(), session.isManageEntities()).thenAccept(res -> {
                    session.getHistoryManager().record(editSession.createChangeSet());
                    CommandHelper.sendSuccess(player, String.format("Removed tree (%,d blocks).", visited.size()));
                });
            } catch (Exception e) {
                CommandHelper.sendError(player, "Deltree error: " + e.getMessage());
            }
        });

        return true;
    }
}
