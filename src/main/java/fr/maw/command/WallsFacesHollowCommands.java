package fr.maw.command;

import fr.maw.MawConfig;
import fr.maw.async.AsyncEditSession;
import fr.maw.async.MawAsyncEngine;
import fr.maw.async.TickDispatcher;
import fr.maw.objects.SurroundingObjectHandler;
import fr.maw.operation.FacesOperation;
import fr.maw.operation.HollowOperation;
import fr.maw.operation.WallsOperation;
import fr.maw.pattern.Pattern;
import fr.maw.pattern.PatternParser;
import fr.maw.pattern.SingleBlockPattern;
import fr.maw.selection.Selection;
import fr.maw.session.PlayerSession;
import fr.maw.session.SessionManager;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;

import java.util.ArrayList;
import java.util.List;

public final class WallsFacesHollowCommands {

    private WallsFacesHollowCommands() {}

    public static List<Command> create(
            MawConfig config,
            SessionManager sessionManager,
            MawAsyncEngine asyncEngine,
            TickDispatcher dispatcher
    ) {
        Command walls = new Command("//walls", "/walls", "walls");
        walls.setDefaultExecutor((sender, context) -> CommandHelper.sendError(sender, "Usage: //walls <pattern> [-u] [-e]"));
        var wallsArg = ArgumentType.StringArray("args");
        walls.addSyntax((sender, context) -> {
            if (!(sender instanceof Player player)) return;
            handleWalls(player, context.get(wallsArg), config, sessionManager, asyncEngine, dispatcher);
        }, wallsArg);

        Command faces = new Command("//faces", "/faces", "faces", "//outline", "/outline", "outline");
        faces.setDefaultExecutor((sender, context) -> CommandHelper.sendError(sender, "Usage: //faces <pattern> [-u] [-e]"));
        var facesArg = ArgumentType.StringArray("args");
        faces.addSyntax((sender, context) -> {
            if (!(sender instanceof Player player)) return;
            handleFaces(player, context.get(facesArg), config, sessionManager, asyncEngine, dispatcher);
        }, facesArg);

        Command hollow = new Command("//hollow", "/hollow", "hollow");
        hollow.setDefaultExecutor((sender, context) -> {
            if (!(sender instanceof Player player)) return;
            handleHollow(player, new String[]{"1"}, config, sessionManager, asyncEngine, dispatcher);
        });
        var hollowArg = ArgumentType.StringArray("args");
        hollow.addSyntax((sender, context) -> {
            if (!(sender instanceof Player player)) return;
            handleHollow(player, context.get(hollowArg), config, sessionManager, asyncEngine, dispatcher);
        }, hollowArg);

        return List.of(walls, faces, hollow);
    }

    private static void handleWalls(
            Player player,
            String[] rawArgs,
            MawConfig config,
            SessionManager sessionManager,
            MawAsyncEngine asyncEngine,
            TickDispatcher dispatcher
    ) {
        Instance instance = player.getInstance();
        if (instance == null) return;
        PlayerSession playerSession = sessionManager.getSession(player);
        if (!playerSession.isSelectionComplete()) {
            CommandHelper.sendError(player, "Make a selection first.");
            return;
        }

        boolean updatePhysics = CommandHelper.hasFlag(rawArgs, "-u");
        boolean manageEntities = CommandHelper.hasFlag(rawArgs, "-e") || playerSession.isManageEntities();

        String patternStr = extractCleanString(rawArgs);
        if (patternStr.isEmpty()) {
            CommandHelper.sendError(player, "Missing pattern argument.");
            return;
        }

        Pattern pattern;
        try {
            pattern = PatternParser.parsePattern(patternStr);
        } catch (Exception e) {
            CommandHelper.sendError(player, "Invalid pattern: " + e.getMessage());
            return;
        }

        Selection selection = playerSession.getSelection();
        CommandHelper.sendInfo(player, "Processing //walls...");

        asyncEngine.runAsync(() -> {
            try {
                SurroundingObjectHandler.handlePreBlockChanges(instance, selection, manageEntities);
                AsyncEditSession editSession = CommandHelper.newSession(config, player, instance, config.maxBlocksPerOperation(), selection);
                WallsOperation.execute(editSession, selection, pattern);

                editSession.commit(dispatcher, updatePhysics, manageEntities).thenAccept(result -> {
                    playerSession.getHistoryManager().record(editSession.createChangeSet());
                    CommandHelper.sendSuccess(player, "Operation completed: " + result.toString());
                });
            } catch (Exception e) {
                CommandHelper.sendError(player, "Failed to execute //walls: " + e.getMessage());
            }
        });
    }

    private static void handleFaces(
            Player player,
            String[] rawArgs,
            MawConfig config,
            SessionManager sessionManager,
            MawAsyncEngine asyncEngine,
            TickDispatcher dispatcher
    ) {
        Instance instance = player.getInstance();
        if (instance == null) return;
        PlayerSession playerSession = sessionManager.getSession(player);
        if (!playerSession.isSelectionComplete()) {
            CommandHelper.sendError(player, "Make a selection first.");
            return;
        }

        boolean updatePhysics = CommandHelper.hasFlag(rawArgs, "-u");
        boolean manageEntities = CommandHelper.hasFlag(rawArgs, "-e") || playerSession.isManageEntities();

        String patternStr = extractCleanString(rawArgs);
        if (patternStr.isEmpty()) {
            CommandHelper.sendError(player, "Missing pattern argument.");
            return;
        }

        Pattern pattern;
        try {
            pattern = PatternParser.parsePattern(patternStr);
        } catch (Exception e) {
            CommandHelper.sendError(player, "Invalid pattern: " + e.getMessage());
            return;
        }

        Selection selection = playerSession.getSelection();
        CommandHelper.sendInfo(player, "Processing //faces...");

        asyncEngine.runAsync(() -> {
            try {
                SurroundingObjectHandler.handlePreBlockChanges(instance, selection, manageEntities);
                AsyncEditSession editSession = CommandHelper.newSession(config, player, instance, config.maxBlocksPerOperation(), selection);
                FacesOperation.execute(editSession, selection, pattern);

                editSession.commit(dispatcher, updatePhysics, manageEntities).thenAccept(result -> {
                    playerSession.getHistoryManager().record(editSession.createChangeSet());
                    CommandHelper.sendSuccess(player, "Operation completed: " + result.toString());
                });
            } catch (Exception e) {
                CommandHelper.sendError(player, "Failed to execute //faces: " + e.getMessage());
            }
        });
    }

    private static void handleHollow(
            Player player,
            String[] rawArgs,
            MawConfig config,
            SessionManager sessionManager,
            MawAsyncEngine asyncEngine,
            TickDispatcher dispatcher
    ) {
        Instance instance = player.getInstance();
        if (instance == null) return;
        PlayerSession playerSession = sessionManager.getSession(player);
        if (!playerSession.isSelectionComplete()) {
            CommandHelper.sendError(player, "Make a selection first.");
            return;
        }

        boolean updatePhysics = CommandHelper.hasFlag(rawArgs, "-u");
        boolean manageEntities = CommandHelper.hasFlag(rawArgs, "-e") || playerSession.isManageEntities();

        List<String> cleaned = new ArrayList<>();
        for (String a : rawArgs) {
            if (!a.equalsIgnoreCase("-u") && !a.equalsIgnoreCase("-e")) {
                cleaned.add(a);
            }
        }

        int thickness = 1;
        Pattern interior = new SingleBlockPattern(Block.AIR);

        if (!cleaned.isEmpty()) {
            try {
                thickness = Integer.parseInt(cleaned.get(0));
                if (cleaned.size() > 1) {
                    interior = PatternParser.parsePattern(cleaned.get(1));
                }
            } catch (NumberFormatException e) {
                // First arg was a pattern
                try {
                    interior = PatternParser.parsePattern(cleaned.get(0));
                } catch (Exception ex) {
                    CommandHelper.sendError(player, "Invalid argument: " + ex.getMessage());
                    return;
                }
            }
        }

        Selection selection = playerSession.getSelection();
        CommandHelper.sendInfo(player, "Processing //hollow...");

        int finalThickness = thickness;
        Pattern finalInterior = interior;
        asyncEngine.runAsync(() -> {
            try {
                SurroundingObjectHandler.handlePreBlockChanges(instance, selection, manageEntities);
                AsyncEditSession editSession = CommandHelper.newSession(config, player, instance, config.maxBlocksPerOperation(), selection);
                HollowOperation.execute(editSession, selection, finalThickness, finalInterior);

                editSession.commit(dispatcher, updatePhysics, manageEntities).thenAccept(result -> {
                    playerSession.getHistoryManager().record(editSession.createChangeSet());
                    CommandHelper.sendSuccess(player, "Operation completed: " + result.toString());
                });
            } catch (Exception e) {
                CommandHelper.sendError(player, "Failed to execute //hollow: " + e.getMessage());
            }
        });
    }

    private static String extractCleanString(String[] args) {
        StringBuilder sb = new StringBuilder();
        for (String a : args) {
            if (!a.equalsIgnoreCase("-u") && !a.equalsIgnoreCase("-e")) {
                if (!sb.isEmpty()) sb.append(" ");
                sb.append(a);
            }
        }
        return sb.toString().trim();
    }
}
