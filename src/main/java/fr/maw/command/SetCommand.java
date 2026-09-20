package fr.maw.command;

import fr.maw.MawConfig;
import fr.maw.async.AsyncEditSession;
import fr.maw.async.MawAsyncEngine;
import fr.maw.async.TickDispatcher;
import fr.maw.objects.SurroundingObjectHandler;
import fr.maw.operation.SetOperation;
import fr.maw.pattern.Pattern;
import fr.maw.pattern.PatternParser;
import fr.maw.selection.Selection;
import fr.maw.session.PlayerSession;
import fr.maw.session.SessionManager;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;

public final class SetCommand extends Command {

    public SetCommand(
            MawConfig config,
            SessionManager sessionManager,
            MawAsyncEngine asyncEngine,
            TickDispatcher dispatcher
    ) {
        super("//set", "/set", "set");

        setDefaultExecutor((sender, context) -> {
            CommandHelper.sendError(sender, "Usage: //set <pattern> [-u] [-e]");
        });

        var patternArg = ArgumentType.StringArray("patternAndFlags");

        addSyntax((sender, context) -> {
            if (!(sender instanceof Player player)) {
                CommandHelper.sendError(sender, "Only players can use this command.");
                return;
            }

            Instance instance = player.getInstance();
            if (instance == null) {
                CommandHelper.sendError(player, "You must be in a loaded instance.");
                return;
            }

            PlayerSession playerSession = sessionManager.getSession(player);
            if (!playerSession.isSelectionComplete()) {
                CommandHelper.sendError(player, "Make a selection first with //wand, //pos1 and //pos2.");
                return;
            }

            String[] args = context.get(patternArg);
            boolean updatePhysics = CommandHelper.hasFlag(args, "-u");
            boolean manageEntities = CommandHelper.hasFlag(args, "-e") || playerSession.isManageEntities();

            StringBuilder patternRaw = new StringBuilder();
            for (String a : args) {
                if (!a.equalsIgnoreCase("-u") && !a.equalsIgnoreCase("-e")) {
                    if (!patternRaw.isEmpty()) patternRaw.append(" ");
                    patternRaw.append(a);
                }
            }

            String patternStr = patternRaw.toString().trim();
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
            CommandHelper.sendInfo(player, "Processing //set (" + String.format("%,d", selection.getVolume()) + " blocks)...");

            asyncEngine.runAsync(() -> {
                try {
                    SurroundingObjectHandler.handlePreBlockChanges(instance, selection, manageEntities);

                    AsyncEditSession editSession = CommandHelper.newSession(config, player, instance, config.maxBlocksPerOperation(), selection);
                    SetOperation.execute(editSession, selection, pattern);

                    editSession.commit(dispatcher, updatePhysics, manageEntities).thenAccept(result -> {
                        playerSession.getHistoryManager().record(editSession.createChangeSet());
                        CommandHelper.sendSuccess(player, "Operation completed: " + result.toString());
                    }).exceptionally(ex -> {
                        CommandHelper.sendError(player, "Error while applying blocks: " + ex.getMessage());
                        return null;
                    });
                } catch (Exception e) {
                    CommandHelper.sendError(player, "Failed to execute //set: " + e.getMessage());
                }
            });

        }, patternArg);
    }
}
