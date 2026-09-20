package fr.maw.tool.brush;

import fr.maw.MawConfig;
import fr.maw.async.AsyncEditSession;
import fr.maw.async.MawAsyncEngine;
import fr.maw.async.TickDispatcher;
import fr.maw.clipboard.Clipboard;
import fr.maw.command.CommandHelper;
import fr.maw.operation.PasteOperation;
import fr.maw.pattern.Mask;
import fr.maw.selection.CuboidSelection;
import fr.maw.session.PlayerSession;
import fr.maw.session.SessionManager;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.BlockFace;

public class ClipboardBrush extends Brush {

    private boolean ignoreAir = true;

    public ClipboardBrush(
            MawConfig config,
            SessionManager sessionManager,
            MawAsyncEngine asyncEngine,
            TickDispatcher dispatcher
    ) {
        super(config, sessionManager, asyncEngine, dispatcher, null, 1, false);
    }

    public boolean isIgnoreAir() {
        return ignoreAir;
    }

    public void setIgnoreAir(boolean ignoreAir) {
        this.ignoreAir = ignoreAir;
    }

    @Override
    protected void applyBrush(Player player, Instance instance, Point targetBlock, BlockFace face) {
        PlayerSession session = sessionManager.getSession(player);
        Clipboard clipboard = session.getClipboard();
        if (clipboard == null || clipboard.isEmpty()) {
            CommandHelper.sendError(player, "Your clipboard is empty! Copy something first with //copy.");
            return;
        }

        Point target = targetBlock;
        if (face != null) {
            target = new Vec(
                    targetBlock.blockX() + face.toDirection().normalX(),
                    targetBlock.blockY() + face.toDirection().normalY(),
                    targetBlock.blockZ() + face.toDirection().normalZ()
            );
        }

        Point min = new Vec(
                target.blockX() - clipboard.getWidth() / 2,
                target.blockY(),
                target.blockZ() - clipboard.getLength() / 2
        );
        Point max = new Vec(
                min.blockX() + clipboard.getWidth(),
                min.blockY() + clipboard.getHeight(),
                min.blockZ() + clipboard.getLength()
        );
        CuboidSelection selection = new CuboidSelection(min, max);

        final Point pasteTarget = target;
        Mask brushMask = this.mask != null ? this.mask : session.getBrushMask();
        Mask globalMask = session.getGlobalMask();

        asyncEngine.runAsync(() -> {
            try {
                AsyncEditSession editSession = CommandHelper.newSession(
                        config, player, instance, config.maxBlocksPerOperation(), selection
                );

                PasteOperation.execute(editSession, clipboard, pasteTarget, ignoreAir);

                editSession.commit(dispatcher, session.isUpdatePhysics(), session.isManageEntities()).thenAccept(result -> {
                    session.getHistoryManager().record(editSession.createChangeSet());
                });
            } catch (Exception e) {
                CommandHelper.sendError(player, "Clipboard brush error: " + e.getMessage());
            }
        });
    }
}
