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
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;

public class ReplacerTool implements Tool {

    private final MawConfig config;
    private final SessionManager sessionManager;
    private final MawAsyncEngine asyncEngine;
    private final TickDispatcher dispatcher;
    private Pattern pattern;

    public ReplacerTool(
            MawConfig config,
            SessionManager sessionManager,
            MawAsyncEngine asyncEngine,
            TickDispatcher dispatcher,
            Pattern pattern
    ) {
        this.config = config;
        this.sessionManager = sessionManager;
        this.asyncEngine = asyncEngine;
        this.dispatcher = dispatcher;
        this.pattern = pattern;
    }

    public Pattern getPattern() {
        return pattern;
    }

    public void setPattern(Pattern pattern) {
        this.pattern = pattern;
    }

    @Override
    public boolean execute(Player player, Instance instance, Point targetBlock, BlockFace face, HandClickType clickType) {
        if (targetBlock == null) return false;

        PlayerSession session = sessionManager.getSession(player);
        int bx = targetBlock.blockX();
        int by = targetBlock.blockY();
        int bz = targetBlock.blockZ();

        CuboidSelection sel = new CuboidSelection(targetBlock, targetBlock);

        asyncEngine.runAsync(() -> {
            try {
                AsyncEditSession editSession = CommandHelper.newSession(
                        config, player, instance, 10, sel
                );
                Block existing = editSession.getBlock(bx, by, bz);
                Block newBlock = pattern.apply(bx, by, bz, existing);
                editSession.setBlock(bx, by, bz, newBlock);

                editSession.commit(dispatcher, session.isUpdatePhysics(), session.isManageEntities()).thenAccept(res -> {
                    session.getHistoryManager().record(editSession.createChangeSet());
                });
            } catch (Exception e) {
                CommandHelper.sendError(player, "Replacer tool error: " + e.getMessage());
            }
        });

        return true;
    }
}
