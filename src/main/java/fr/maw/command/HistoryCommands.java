package fr.maw.command;

import fr.maw.async.AsyncEditSession;
import fr.maw.async.MawAsyncEngine;
import fr.maw.async.TickDispatcher;
import fr.maw.history.BlockChange;
import fr.maw.history.ChangeSet;
import fr.maw.session.PlayerSession;
import fr.maw.session.SessionManager;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;

import java.util.List;

public final class HistoryCommands {

    private HistoryCommands() {}

    public static List<Command> create(
            SessionManager sessionManager,
            MawAsyncEngine asyncEngine,
            TickDispatcher dispatcher
    ) {
        Command undo = new Command("//undo", "/undo", "undo");
        undo.setDefaultExecutor((sender, context) -> {
            if (!(sender instanceof Player player)) return;
            handleUndo(player, 1, sessionManager, asyncEngine, dispatcher);
        });
        var undoTimesArg = ArgumentType.Integer("times");
        undo.addSyntax((sender, context) -> {
            if (!(sender instanceof Player player)) return;
            handleUndo(player, context.get(undoTimesArg), sessionManager, asyncEngine, dispatcher);
        }, undoTimesArg);

        Command redo = new Command("//redo", "/redo", "redo");
        redo.setDefaultExecutor((sender, context) -> {
            if (!(sender instanceof Player player)) return;
            handleRedo(player, 1, sessionManager, asyncEngine, dispatcher);
        });
        var redoTimesArg = ArgumentType.Integer("times");
        redo.addSyntax((sender, context) -> {
            if (!(sender instanceof Player player)) return;
            handleRedo(player, context.get(redoTimesArg), sessionManager, asyncEngine, dispatcher);
        }, redoTimesArg);

        Command clearHistory = new Command("//clearhistory", "/clearhistory", "clearhistory");
        clearHistory.setDefaultExecutor((sender, context) -> {
            if (!(sender instanceof Player player)) return;
            PlayerSession session = sessionManager.getSession(player);
            session.getHistoryManager().clear();
            CommandHelper.sendSuccess(player, "History cleared.");
        });

        return List.of(undo, redo, clearHistory);
    }

    private static void handleUndo(
            Player player,
            int times,
            SessionManager sessionManager,
            MawAsyncEngine asyncEngine,
            TickDispatcher dispatcher
    ) {
        Instance instance = player.getInstance();
        if (instance == null) return;
        PlayerSession playerSession = sessionManager.getSession(player);

        if (playerSession.getHistoryManager().getUndoCount() == 0) {
            CommandHelper.sendError(player, "Nothing left to undo.");
            return;
        }

        int count = Math.min(times, playerSession.getHistoryManager().getUndoCount());
        CommandHelper.sendInfo(player, "Undoing " + count + " operation(s)...");

        asyncEngine.runAsync(() -> {
            try {
                for (int i = 0; i < count; i++) {
                    ChangeSet changeSet = playerSession.getHistoryManager().popUndo();
                    if (changeSet == null) break;

                    ChangeSet inverted = changeSet.inverse();
                    AsyncEditSession session = new AsyncEditSession(instance, Integer.MAX_VALUE);
                    for (BlockChange change : inverted.getChanges()) {
                        session.setBlock(change.x(), change.y(), change.z(), change.newBlock());
                    }
                    session.commit(dispatcher, false, false).join();
                }
                CommandHelper.sendSuccess(player, "Undo completed.");
            } catch (Exception e) {
                CommandHelper.sendError(player, "Failed to undo: " + e.getMessage());
            }
        });
    }

    private static void handleRedo(
            Player player,
            int times,
            SessionManager sessionManager,
            MawAsyncEngine asyncEngine,
            TickDispatcher dispatcher
    ) {
        Instance instance = player.getInstance();
        if (instance == null) return;
        PlayerSession playerSession = sessionManager.getSession(player);

        if (playerSession.getHistoryManager().getRedoCount() == 0) {
            CommandHelper.sendError(player, "Nothing left to redo.");
            return;
        }

        int count = Math.min(times, playerSession.getHistoryManager().getRedoCount());
        CommandHelper.sendInfo(player, "Redoing " + count + " operation(s)...");

        asyncEngine.runAsync(() -> {
            try {
                for (int i = 0; i < count; i++) {
                    ChangeSet changeSet = playerSession.getHistoryManager().popRedo();
                    if (changeSet == null) break;

                    AsyncEditSession session = new AsyncEditSession(instance, Integer.MAX_VALUE);
                    for (BlockChange change : changeSet.getChanges()) {
                        session.setBlock(change.x(), change.y(), change.z(), change.newBlock());
                    }
                    session.commit(dispatcher, false, false).join();
                }
                CommandHelper.sendSuccess(player, "Redo completed.");
            } catch (Exception e) {
                CommandHelper.sendError(player, "Failed to redo: " + e.getMessage());
            }
        });
    }
}
