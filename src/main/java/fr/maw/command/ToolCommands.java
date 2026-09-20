package fr.maw.command;

import fr.maw.MawConfig;
import fr.maw.async.MawAsyncEngine;
import fr.maw.async.TickDispatcher;
import fr.maw.pattern.Pattern;
import fr.maw.pattern.PatternParser;
import fr.maw.session.PlayerSession;
import fr.maw.session.SessionManager;
import fr.maw.tool.specialized.*;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.entity.Player;
import net.minestom.server.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public final class ToolCommands {

    private ToolCommands() {}

    public static List<Command> create(
            MawConfig config,
            SessionManager sessionManager,
            MawAsyncEngine asyncEngine,
            TickDispatcher dispatcher
    ) {
        List<Command> commands = new ArrayList<>();

        // 1. /tool (or /t)
        Command tool = new Command("/tool", "tool", "/t", "t");
        tool.setDefaultExecutor((sender, context) -> {
            CommandHelper.sendError(sender, "Usage: /tool <info|repl|cycler|tree|deltree|lrbuild|floodfill|farwand|none>");
        });

        var toolArgs = ArgumentType.StringArray("args");
        tool.addSyntax((sender, context) -> {
            if (!(sender instanceof Player player)) {
                CommandHelper.sendError(sender, "Only players can use /tool.");
                return;
            }

            ItemStack item = player.getItemInMainHand();
            if (item.isAir()) {
                CommandHelper.sendError(player, "Hold an item in your main hand to bind a tool to it.");
                return;
            }

            String[] raw = context.get(toolArgs);
            if (raw.length == 0) {
                CommandHelper.sendError(player, "Usage: /tool <type>");
                return;
            }

            String type = raw[0].toLowerCase();
            PlayerSession session = sessionManager.getSession(player);
            String itemNamespace = item.material().name();

            try {
                switch (type) {
                    case "info" -> {
                        session.bindTool(itemNamespace, new InfoTool());
                        CommandHelper.sendSuccess(player, "Bound Info tool to " + item.material().name() + ".");
                    }
                    case "repl", "replacer" -> {
                        String patternStr = raw.length > 1 ? raw[1] : "stone";
                        Pattern pattern = PatternParser.parsePattern(patternStr);
                        session.bindTool(itemNamespace, new ReplacerTool(config, sessionManager, asyncEngine, dispatcher, pattern));
                        CommandHelper.sendSuccess(player, String.format("Bound Replacer tool (%s) to %s.", patternStr, item.material().name()));
                    }
                    case "cycler" -> {
                        session.bindTool(itemNamespace, new CyclerTool(config, sessionManager, asyncEngine, dispatcher));
                        CommandHelper.sendSuccess(player, "Bound Block Cycler tool to " + item.material().name() + ".");
                    }
                    case "tree" -> {
                        String treeType = raw.length > 1 ? raw[1] : "oak";
                        session.bindTool(itemNamespace, new TreeTool(config, sessionManager, asyncEngine, dispatcher, treeType));
                        CommandHelper.sendSuccess(player, String.format("Bound Tree tool (%s) to %s.", treeType, item.material().name()));
                    }
                    case "deltree" -> {
                        session.bindTool(itemNamespace, new DeltreeTool(config, sessionManager, asyncEngine, dispatcher));
                        CommandHelper.sendSuccess(player, "Bound Floating Tree Remover (deltree) to " + item.material().name() + ".");
                    }
                    case "lrbuild" -> {
                        String patternStr = raw.length > 1 ? raw[1] : "stone";
                        Pattern pattern = PatternParser.parsePattern(patternStr);
                        session.bindTool(itemNamespace, new LongRangeBuildTool(config, sessionManager, asyncEngine, dispatcher, pattern));
                        CommandHelper.sendSuccess(player, String.format("Bound Long Range Build tool (%s) to %s.", patternStr, item.material().name()));
                    }
                    case "floodfill" -> {
                        String patternStr = raw.length > 1 ? raw[1] : "stone";
                        int range = raw.length > 2 && raw[2].matches("\\d+") ? Integer.parseInt(raw[2]) : 15;
                        Pattern pattern = PatternParser.parsePattern(patternStr);
                        session.bindTool(itemNamespace, new FloodFillTool(config, sessionManager, asyncEngine, dispatcher, pattern, range));
                        CommandHelper.sendSuccess(player, String.format("Bound Flood Fill tool (%s, range=%d) to %s.", patternStr, range, item.material().name()));
                    }
                    case "farwand" -> {
                        session.bindTool(itemNamespace, new FarwandTool(sessionManager));
                        CommandHelper.sendSuccess(player, "Bound Farwand tool (raycast pos1/pos2) to " + item.material().name() + ".");
                    }
                    case "none" -> {
                        session.unbindTool(itemNamespace);
                        CommandHelper.sendSuccess(player, "Unbound any tool or brush from " + item.material().name() + ".");
                    }
                    default -> CommandHelper.sendError(player, "Unknown tool type: " + type + ". Available: info, repl, cycler, tree, deltree, lrbuild, floodfill, farwand, none");
                }
            } catch (Exception e) {
                CommandHelper.sendError(player, "Failed to bind tool: " + e.getMessage());
            }
        }, toolArgs);
        commands.add(tool);

        // 2. /none (shortcut to unbind held tool)
        Command none = new Command("/none", "none");
        none.setDefaultExecutor((sender, context) -> {
            if (!(sender instanceof Player player)) return;
            ItemStack item = player.getItemInMainHand();
            if (item.isAir()) {
                CommandHelper.sendInfo(player, "You are not holding any item.");
                return;
            }
            PlayerSession session = sessionManager.getSession(player);
            session.unbindTool(item.material().name());
            CommandHelper.sendSuccess(player, "Unbound any tool from " + item.material().name() + ".");
        });
        commands.add(none);

        return commands;
    }
}
