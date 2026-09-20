package fr.maw.command;

import fr.maw.clipboard.Clipboard;
import fr.maw.schematic.SchematicManager;
import fr.maw.session.PlayerSession;
import fr.maw.session.SessionManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.entity.Player;

import java.util.ArrayList;
import java.util.List;

public final class SchematicCommands {

    private SchematicCommands() {}

    public static List<Command> create(SessionManager sessionManager, SchematicManager schematicManager) {
        List<Command> commands = new ArrayList<>();

        // 1. //schem & //schematic
        Command schem = new Command("//schem", "/schem", "schem", "//schematic", "/schematic", "schematic");
        schem.setDefaultExecutor((sender, context) -> {
            CommandHelper.sendError(sender, "Usage: //schem <save|load|list|delete> [filename]");
        });

        var actionArg = ArgumentType.Word("action").from("save", "load", "list", "delete");
        var nameArg = ArgumentType.Word("name");

        // Syntax: //schem list
        schem.addSyntax((sender, context) -> {
            if (!(sender instanceof Player player)) return;
            String action = context.get(actionArg);
            if (action.equalsIgnoreCase("list")) {
                List<String> list = schematicManager.list();
                if (list.isEmpty()) {
                    CommandHelper.sendInfo(player, "No saved schematics found.");
                } else {
                    player.sendMessage(Component.text(String.format("--- Saved Schematics (%d) ---", list.size()), NamedTextColor.GOLD));
                    for (String s : list) {
                        player.sendMessage(Component.text("• " + s, NamedTextColor.YELLOW));
                    }
                }
            } else {
                CommandHelper.sendError(player, "Usage: //schem " + action + " <name>");
            }
        }, actionArg);

        // Syntax: //schem <save|load|delete> <name>
        schem.addSyntax((sender, context) -> {
            if (!(sender instanceof Player player)) return;
            String action = context.get(actionArg);
            String name = context.get(nameArg);
            PlayerSession session = sessionManager.getSession(player);

            try {
                switch (action.toLowerCase()) {
                    case "save" -> {
                        Clipboard cb = session.getClipboard();
                        if (cb == null || cb.isEmpty()) {
                            CommandHelper.sendError(player, "Clipboard is empty! Copy something first with //copy.");
                            return;
                        }
                        schematicManager.save(cb, name);
                        CommandHelper.sendSuccess(player, String.format("Saved schematic '%s' (%,d blocks).", name, cb.size()));
                    }
                    case "load" -> {
                        Clipboard cb = schematicManager.load(name);
                        session.setClipboard(cb);
                        CommandHelper.sendSuccess(player, String.format("Loaded schematic '%s' into clipboard (%,d blocks). Use //paste to place.", name, cb.size()));
                    }
                    case "delete" -> {
                        boolean deleted = schematicManager.delete(name);
                        if (deleted) {
                            CommandHelper.sendSuccess(player, "Deleted schematic: " + name);
                        } else {
                            CommandHelper.sendError(player, "Could not delete schematic (file not found): " + name);
                        }
                    }
                    case "list" -> {
                        List<String> list = schematicManager.list();
                        player.sendMessage(Component.text(String.format("--- Saved Schematics (%d) ---", list.size()), NamedTextColor.GOLD));
                        for (String s : list) {
                            player.sendMessage(Component.text("• " + s, NamedTextColor.YELLOW));
                        }
                    }
                    default -> CommandHelper.sendError(player, "Unknown schematic action: " + action);
                }
            } catch (Exception e) {
                CommandHelper.sendError(player, "Schematic error: " + e.getMessage());
            }
        }, actionArg, nameArg);
        commands.add(schem);

        // 2. //clearclipboard
        Command clearClipboard = new Command("//clearclipboard", "/clearclipboard", "clearclipboard");
        clearClipboard.setDefaultExecutor((sender, context) -> {
            if (!(sender instanceof Player player)) return;
            PlayerSession session = sessionManager.getSession(player);
            session.setClipboard(null);
            CommandHelper.sendSuccess(player, "Clipboard cleared from memory.");
        });
        commands.add(clearClipboard);

        return commands;
    }
}
