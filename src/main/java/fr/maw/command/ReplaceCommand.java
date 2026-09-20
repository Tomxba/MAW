package fr.maw.command;

import fr.maw.MawConfig;
import fr.maw.async.AsyncEditSession;
import fr.maw.async.MawAsyncEngine;
import fr.maw.async.TickDispatcher;
import fr.maw.objects.SurroundingObjectHandler;
import fr.maw.operation.ReplaceOperation;
import fr.maw.pattern.Mask;
import fr.maw.pattern.Masks;
import fr.maw.pattern.Pattern;
import fr.maw.pattern.PatternParser;
import fr.maw.selection.Selection;
import fr.maw.session.PlayerSession;
import fr.maw.session.SessionManager;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;

import java.util.ArrayList;
import java.util.List;

public final class ReplaceCommand extends Command {

    public ReplaceCommand(
            MawConfig config,
            SessionManager sessionManager,
            MawAsyncEngine asyncEngine,
            TickDispatcher dispatcher
    ) {
        super("//replace", "/replace", "replace");

        setDefaultExecutor((sender, context) -> {
            CommandHelper.sendError(sender, "Usage: //replace [mask] <pattern> [-u] [-e]");
        });

        var argsType = ArgumentType.StringArray("args");

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
                CommandHelper.sendError(player, "Make a selection first.");
                return;
            }

            String[] rawArgs = context.get(argsType);
            boolean updatePhysics = CommandHelper.hasFlag(rawArgs, "-u");
            boolean manageEntities = CommandHelper.hasFlag(rawArgs, "-e") || playerSession.isManageEntities();

            List<String> cleaned = new ArrayList<>();
            for (String a : rawArgs) {
                if (!a.equalsIgnoreCase("-u") && !a.equalsIgnoreCase("-e")) {
                    cleaned.add(a);
                }
            }

            if (cleaned.isEmpty()) {
                CommandHelper.sendError(player, "Missing arguments. Usage: //replace [mask] <pattern>");
                return;
            }

            Mask mask;
            Pattern pattern;

            try {
                if (cleaned.size() == 1) {
                    // //replace <pattern> -> replaces non-air
                    mask = Masks.EXISTING;
                    pattern = PatternParser.parsePattern(cleaned.get(0));
                } else {
                    // //replace <mask> <pattern>
                    mask = PatternParser.parseMask(cleaned.get(0));
                    pattern = PatternParser.parsePattern(cleaned.get(1));
                }
            } catch (Exception e) {
                CommandHelper.sendError(player, "Error parsing arguments: " + e.getMessage());
                return;
            }

            Selection selection = playerSession.getSelection();
            CommandHelper.sendInfo(player, "Processing //replace...");

            asyncEngine.runAsync(() -> {
                try {
                    SurroundingObjectHandler.handlePreBlockChanges(instance, selection, manageEntities);

                    AsyncEditSession editSession = CommandHelper.newSession(config, player, instance, config.maxBlocksPerOperation(), selection);
                    ReplaceOperation.execute(editSession, selection, mask, pattern);

                    editSession.commit(dispatcher, updatePhysics, manageEntities).thenAccept(result -> {
                        playerSession.getHistoryManager().record(editSession.createChangeSet());
                        CommandHelper.sendSuccess(player, "Operation completed: " + result.toString());
                    }).exceptionally(ex -> {
                        CommandHelper.sendError(player, "Error while applying blocks: " + ex.getMessage());
                        return null;
                    });
                } catch (Exception e) {
                    CommandHelper.sendError(player, "Failed to execute //replace: " + e.getMessage());
                }
            });

        }, argsType);
    }
}
