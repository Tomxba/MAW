package fr.maw.command;

import fr.maw.MawConfig;
import fr.maw.async.AsyncEditSession;
import fr.maw.async.MawAsyncEngine;
import fr.maw.async.TickDispatcher;
import fr.maw.clipboard.Clipboard;
import fr.maw.clipboard.Transform;
import fr.maw.operation.CopyOperation;
import fr.maw.operation.PasteOperation;
import fr.maw.selection.Selection;
import fr.maw.session.PlayerSession;
import fr.maw.session.SessionManager;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;

import java.util.List;
import java.util.Locale;

public final class ClipboardCommands {

    private ClipboardCommands() {}

    public static List<Command> create(
            MawConfig config,
            SessionManager sessionManager,
            MawAsyncEngine asyncEngine,
            TickDispatcher dispatcher
    ) {
        Command copy = new Command("//copy", "/copy", "copy");
        copy.setDefaultExecutor((sender, context) -> {
            if (!(sender instanceof Player player)) return;
            handleCopy(player, new String[0], sessionManager);
        });
        var copyArg = ArgumentType.StringArray("flags");
        copy.addSyntax((sender, context) -> {
            if (!(sender instanceof Player player)) return;
            handleCopy(player, context.get(copyArg), sessionManager);
        }, copyArg);

        Command paste = new Command("//paste", "/paste", "paste");
        paste.setDefaultExecutor((sender, context) -> {
            if (!(sender instanceof Player player)) return;
            handlePaste(player, new String[0], config, sessionManager, asyncEngine, dispatcher);
        });
        var pasteArg = ArgumentType.StringArray("flags");
        paste.addSyntax((sender, context) -> {
            if (!(sender instanceof Player player)) return;
            handlePaste(player, context.get(pasteArg), config, sessionManager, asyncEngine, dispatcher);
        }, pasteArg);

        Command rotate = new Command("//rotate", "/rotate", "rotate");
        rotate.setDefaultExecutor((sender, context) -> CommandHelper.sendError(sender, "Usage: //rotate <angle> (e.g. 90, 180, 270)"));
        var angleArg = ArgumentType.Integer("angle");
        rotate.addSyntax((sender, context) -> {
            if (!(sender instanceof Player player)) return;
            handleRotate(player, context.get(angleArg), sessionManager);
        }, angleArg);

        Command flip = new Command("//flip", "/flip", "flip");
        flip.setDefaultExecutor((sender, context) -> {
            if (!(sender instanceof Player player)) return;
            handleFlip(player, null, sessionManager);
        });
        var dirArg = ArgumentType.Word("direction").from("north", "south", "east", "west", "up", "down");
        flip.addSyntax((sender, context) -> {
            if (!(sender instanceof Player player)) return;
            handleFlip(player, context.get(dirArg), sessionManager);
        }, dirArg);

        return List.of(copy, paste, rotate, flip);
    }

    private static void handleCopy(Player player, String[] args, SessionManager sessionManager) {
        Instance instance = player.getInstance();
        if (instance == null) return;
        PlayerSession playerSession = sessionManager.getSession(player);
        if (!playerSession.isSelectionComplete()) {
            CommandHelper.sendError(player, "Make a selection first.");
            return;
        }

        boolean copyEntities = CommandHelper.hasFlag(args, "-e");
        Selection selection = playerSession.getSelection();
        Point origin = player.getPosition();

        Clipboard clipboard = CopyOperation.execute(instance, selection, origin, copyEntities);
        playerSession.setClipboard(clipboard);

        String entityInfo = clipboard.hasEntities() ? " and " + clipboard.getEntities().size() + " entities" : "";
        CommandHelper.sendSuccess(player, String.format("%,d blocks%s copied to clipboard.", clipboard.getBlockCount(), entityInfo));
    }

    private static void handlePaste(
            Player player,
            String[] args,
            MawConfig config,
            SessionManager sessionManager,
            MawAsyncEngine asyncEngine,
            TickDispatcher dispatcher
    ) {
        Instance instance = player.getInstance();
        if (instance == null) return;
        PlayerSession playerSession = sessionManager.getSession(player);
        Clipboard clipboard = playerSession.getClipboard();
        if (clipboard == null) {
            CommandHelper.sendError(player, "Your clipboard is empty! Copy something first with //copy.");
            return;
        }

        boolean ignoreAir = CommandHelper.hasFlag(args, "-a");
        boolean pasteEntities = CommandHelper.hasFlag(args, "-e") || clipboard.hasEntities();
        boolean updatePhysics = CommandHelper.hasFlag(args, "-u");

        Point target = player.getPosition();
        CommandHelper.sendInfo(player, "Pasting clipboard...");

        asyncEngine.runAsync(() -> {
            try {
                AsyncEditSession editSession = new AsyncEditSession(instance, config.maxBlocksPerOperation());
                PasteOperation.execute(editSession, clipboard, target, ignoreAir, pasteEntities);

                editSession.commit(dispatcher, updatePhysics, pasteEntities).thenAccept(result -> {
                    playerSession.getHistoryManager().record(editSession.createChangeSet());
                    CommandHelper.sendSuccess(player, "Pasted: " + result.toString());
                });
            } catch (Exception e) {
                CommandHelper.sendError(player, "Failed to paste: " + e.getMessage());
            }
        });
    }

    private static void handleRotate(Player player, int angle, SessionManager sessionManager) {
        PlayerSession playerSession = sessionManager.getSession(player);
        Clipboard clipboard = playerSession.getClipboard();
        if (clipboard == null) {
            CommandHelper.sendError(player, "Your clipboard is empty!");
            return;
        }

        try {
            Clipboard rotated = Transform.rotate(clipboard, angle);
            playerSession.setClipboard(rotated);
            CommandHelper.sendSuccess(player, "Clipboard rotated by " + angle + " degrees.");
        } catch (IllegalArgumentException e) {
            CommandHelper.sendError(player, e.getMessage());
        }
    }

    private static void handleFlip(Player player, String dirStr, SessionManager sessionManager) {
        PlayerSession playerSession = sessionManager.getSession(player);
        Clipboard clipboard = playerSession.getClipboard();
        if (clipboard == null) {
            CommandHelper.sendError(player, "Your clipboard is empty!");
            return;
        }

        Transform.Direction dir = Transform.Direction.NORTH;
        if (dirStr != null) {
            try {
                dir = Transform.Direction.valueOf(dirStr.toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException e) {
                CommandHelper.sendError(player, "Invalid flip direction. Choose north, south, east, west, up, or down.");
                return;
            }
        }

        Clipboard flipped = Transform.flip(clipboard, dir);
        playerSession.setClipboard(flipped);
        CommandHelper.sendSuccess(player, "Clipboard flipped along " + dir.name().toLowerCase(Locale.ROOT) + ".");
    }
}
