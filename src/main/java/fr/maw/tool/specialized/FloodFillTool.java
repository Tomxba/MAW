package fr.maw.tool.specialized;

import fr.maw.MawConfig;
import fr.maw.async.AsyncEditSession;
import fr.maw.async.MawAsyncEngine;
import fr.maw.async.TickDispatcher;
import fr.maw.command.CommandHelper;
import fr.maw.pattern.Pattern;
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

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;

public class FloodFillTool implements Tool {

    private final MawConfig config;
    private final SessionManager sessionManager;
    private final MawAsyncEngine asyncEngine;
    private final TickDispatcher dispatcher;
    private Pattern pattern;
    private int range;

    public FloodFillTool(
            MawConfig config,
            SessionManager sessionManager,
            MawAsyncEngine asyncEngine,
            TickDispatcher dispatcher,
            Pattern pattern,
            int range
    ) {
        this.config = config;
        this.sessionManager = sessionManager;
        this.asyncEngine = asyncEngine;
        this.dispatcher = dispatcher;
        this.pattern = pattern;
        this.range = Math.max(1, Math.min(range, 30));
    }

    public Pattern getPattern() {
        return pattern;
    }

    public void setPattern(Pattern pattern) {
        this.pattern = pattern;
    }

    public int getRange() {
        return range;
    }

    public void setRange(int range) {
        this.range = Math.max(1, Math.min(range, 30));
    }

    @Override
    public boolean execute(Player player, Instance instance, Point targetBlock, BlockFace face, HandClickType clickType) {
        if (targetBlock == null) return false;

        PlayerSession session = sessionManager.getSession(player);
        int sx = targetBlock.blockX();
        int sy = targetBlock.blockY();
        int sz = targetBlock.blockZ();

        Block originBlock = instance.getBlock(sx, sy, sz);

        asyncEngine.runAsync(() -> {
            try {
                Set<Vec> matching = new HashSet<>();
                Queue<Vec> queue = new ArrayDeque<>();
                Vec start = new Vec(sx, sy, sz);
                matching.add(start);
                queue.add(start);

                int minX = sx, maxX = sx;
                int minY = sy, maxY = sy;
                int minZ = sz, maxZ = sz;

                int maxBlocks = 10_000;
                while (!queue.isEmpty() && matching.size() < maxBlocks) {
                    Vec cur = queue.poll();
                    minX = Math.min(minX, cur.blockX());
                    maxX = Math.max(maxX, cur.blockX());
                    minY = Math.min(minY, cur.blockY());
                    maxY = Math.max(maxY, cur.blockY());
                    minZ = Math.min(minZ, cur.blockZ());
                    maxZ = Math.max(maxZ, cur.blockZ());

                    for (BlockFace bf : BlockFace.values()) {
                        int nx = cur.blockX() + bf.toDirection().normalX();
                        int ny = cur.blockY() + bf.toDirection().normalY();
                        int nz = cur.blockZ() + bf.toDirection().normalZ();

                        if (Math.abs(nx - sx) > range || Math.abs(ny - sy) > range || Math.abs(nz - sz) > range) {
                            continue;
                        }

                        Vec nVec = new Vec(nx, ny, nz);
                        if (!matching.contains(nVec)) {
                            Block nb = instance.getBlock(nx, ny, nz);
                            if (nb.compare(originBlock)) {
                                matching.add(nVec);
                                queue.add(nVec);
                            }
                        }
                    }
                }

                CuboidSelection sel = new CuboidSelection(new Vec(minX, minY, minZ), new Vec(maxX, maxY, maxZ));
                AsyncEditSession editSession = CommandHelper.newSession(config, player, instance, matching.size() + 10, sel);
                for (Vec p : matching) {
                    Block existing = editSession.getBlock(p.blockX(), p.blockY(), p.blockZ());
                    editSession.setBlock(p.blockX(), p.blockY(), p.blockZ(), pattern.apply(p.blockX(), p.blockY(), p.blockZ(), existing));
                }

                editSession.commit(dispatcher, session.isUpdatePhysics(), session.isManageEntities()).thenAccept(res -> {
                    session.getHistoryManager().record(editSession.createChangeSet());
                    CommandHelper.sendSuccess(player, String.format("Flood filled %,d blocks.", matching.size()));
                });
            } catch (Exception e) {
                CommandHelper.sendError(player, "Flood fill error: " + e.getMessage());
            }
        });

        return true;
    }
}
