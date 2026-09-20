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
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;

import java.util.List;
import java.util.Map;

public class CyclerTool implements Tool {

    private final MawConfig config;
    private final SessionManager sessionManager;
    private final MawAsyncEngine asyncEngine;
    private final TickDispatcher dispatcher;

    private static final List<String> CARDINAL_FACINGS = List.of("north", "east", "south", "west");
    private static final List<String> ALL_FACINGS = List.of("north", "east", "south", "west", "up", "down");
    private static final List<String> AXES = List.of("x", "y", "z");
    private static final List<String> HALVES = List.of("bottom", "top");

    public CyclerTool(
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

    @Override
    public boolean execute(Player player, Instance instance, Point targetBlock, BlockFace face, HandClickType clickType) {
        if (targetBlock == null) return false;

        PlayerSession session = sessionManager.getSession(player);
        int bx = targetBlock.blockX();
        int by = targetBlock.blockY();
        int bz = targetBlock.blockZ();

        Block block = instance.getBlock(bx, by, bz);
        Map<String, String> props = block.properties();
        if (props.isEmpty()) {
            CommandHelper.sendInfo(player, "Block " + block.name() + " has no configurable state properties.");
            return true;
        }

        // Cycle the first property found
        String targetProp = null;
        String nextVal = null;

        for (var entry : props.entrySet()) {
            String key = entry.getKey();
            String val = entry.getValue();

            if (val.equalsIgnoreCase("true") || val.equalsIgnoreCase("false")) {
                targetProp = key;
                nextVal = val.equalsIgnoreCase("true") ? "false" : "true";
                break;
            } else if (key.equalsIgnoreCase("facing")) {
                targetProp = key;
                List<String> list = ALL_FACINGS.contains(val.toLowerCase()) ? ALL_FACINGS : CARDINAL_FACINGS;
                int idx = list.indexOf(val.toLowerCase());
                nextVal = list.get((idx + 1) % list.size());
                break;
            } else if (key.equalsIgnoreCase("axis")) {
                targetProp = key;
                int idx = AXES.indexOf(val.toLowerCase());
                nextVal = AXES.get((idx + 1) % AXES.size());
                break;
            } else if (key.equalsIgnoreCase("half")) {
                targetProp = key;
                int idx = HALVES.indexOf(val.toLowerCase());
                nextVal = HALVES.get((idx + 1) % HALVES.size());
                break;
            }
        }

        if (targetProp == null) {
            // Fallback: cycle first property if possible
            var first = props.entrySet().iterator().next();
            targetProp = first.getKey();
            nextVal = first.getValue();
        }

        try {
            Block newBlock = block.withProperty(targetProp, nextVal);
            CuboidSelection sel = new CuboidSelection(targetBlock, targetBlock);

            String propInfo = targetProp + "=" + nextVal;
            asyncEngine.runAsync(() -> {
                AsyncEditSession editSession = CommandHelper.newSession(config, player, instance, 10, sel);
                editSession.setBlock(bx, by, bz, newBlock);
                editSession.commit(dispatcher, session.isUpdatePhysics(), session.isManageEntities()).thenAccept(res -> {
                    session.getHistoryManager().record(editSession.createChangeSet());
                    CommandHelper.sendInfo(player, "Cycled property: " + propInfo);
                });
            });
        } catch (Exception e) {
            CommandHelper.sendError(player, "Cannot cycle block: " + e.getMessage());
        }

        return true;
    }
}
