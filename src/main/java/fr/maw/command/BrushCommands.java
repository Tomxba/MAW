package fr.maw.command;

import fr.maw.MawConfig;
import fr.maw.async.MawAsyncEngine;
import fr.maw.async.TickDispatcher;
import fr.maw.pattern.Mask;
import fr.maw.pattern.Pattern;
import fr.maw.pattern.PatternParser;
import fr.maw.session.PlayerSession;
import fr.maw.session.SessionManager;
import fr.maw.tool.Tool;
import fr.maw.tool.brush.*;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.entity.Player;
import net.minestom.server.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public final class BrushCommands {

    private BrushCommands() {}

    public static List<Command> create(
            MawConfig config,
            SessionManager sessionManager,
            MawAsyncEngine asyncEngine,
            TickDispatcher dispatcher
    ) {
        List<Command> commands = new ArrayList<>();

        // 1. /brush (or /b)
        Command brush = new Command("/brush", "brush", "/b", "b");
        brush.setDefaultExecutor((sender, context) -> {
            CommandHelper.sendError(sender, "Usage: /brush <sphere|cyl|smooth|clipboard|gravity|raise|lower|flatten|paint> ...");
        });

        var brushArgs = ArgumentType.StringArray("args");
        brush.addSyntax((sender, context) -> {
            if (!(sender instanceof Player player)) {
                CommandHelper.sendError(sender, "Only players can use /brush.");
                return;
            }

            ItemStack item = player.getItemInMainHand();
            if (item.isAir()) {
                CommandHelper.sendError(player, "Hold an item in your main hand to bind a brush to it.");
                return;
            }

            String[] raw = context.get(brushArgs);
            if (raw.length == 0) {
                CommandHelper.sendError(player, "Usage: /brush <type> [options]");
                return;
            }

            String type = raw[0].toLowerCase();
            PlayerSession session = sessionManager.getSession(player);
            String itemNamespace = item.material().name();

            try {
                switch (type) {
                    case "sphere", "s" -> {
                        boolean hollow = CommandHelper.hasFlag(raw, "-h");
                        int radius = session.getBrushSize();
                        String patternStr = "stone";

                        for (int i = 1; i < raw.length; i++) {
                            String arg = raw[i];
                            if (arg.equalsIgnoreCase("-h")) continue;
                            if (arg.matches("\\d+")) {
                                radius = Integer.parseInt(arg);
                            } else {
                                patternStr = arg;
                            }
                        }

                        Pattern pattern = PatternParser.parsePattern(patternStr);
                        SphereBrush sb = new SphereBrush(config, sessionManager, asyncEngine, dispatcher, pattern, radius, hollow);
                        session.bindTool(itemNamespace, sb);
                        CommandHelper.sendSuccess(player, String.format("Bound Sphere brush (%s, radius=%d%s) to %s.",
                                patternStr, radius, hollow ? ", hollow" : "", item.material().name()));
                    }
                    case "cylinder", "cyl", "c" -> {
                        boolean hollow = CommandHelper.hasFlag(raw, "-h");
                        int radius = session.getBrushSize();
                        int height = 1;
                        String patternStr = "stone";

                        List<Integer> nums = new ArrayList<>();
                        for (int i = 1; i < raw.length; i++) {
                            String arg = raw[i];
                            if (arg.equalsIgnoreCase("-h")) continue;
                            if (arg.matches("\\d+")) {
                                nums.add(Integer.parseInt(arg));
                            } else {
                                patternStr = arg;
                            }
                        }

                        if (!nums.isEmpty()) radius = nums.get(0);
                        if (nums.size() > 1) height = nums.get(1);

                        Pattern pattern = PatternParser.parsePattern(patternStr);
                        CylinderBrush cb = new CylinderBrush(config, sessionManager, asyncEngine, dispatcher, pattern, radius, height, hollow);
                        session.bindTool(itemNamespace, cb);
                        CommandHelper.sendSuccess(player, String.format("Bound Cylinder brush (%s, radius=%d, height=%d%s) to %s.",
                                patternStr, radius, height, hollow ? ", hollow" : "", item.material().name()));
                    }
                    case "smooth" -> {
                        int radius = session.getBrushSize();
                        int iterations = 1;
                        if (raw.length > 1 && raw[1].matches("\\d+")) radius = Integer.parseInt(raw[1]);
                        if (raw.length > 2 && raw[2].matches("\\d+")) iterations = Integer.parseInt(raw[2]);

                        SmoothBrush smb = new SmoothBrush(config, sessionManager, asyncEngine, dispatcher, radius, iterations);
                        session.bindTool(itemNamespace, smb);
                        CommandHelper.sendSuccess(player, String.format("Bound Smooth brush (radius=%d, iterations=%d) to %s.",
                                radius, iterations, item.material().name()));
                    }
                    case "clipboard", "clip" -> {
                        ClipboardBrush clb = new ClipboardBrush(config, sessionManager, asyncEngine, dispatcher);
                        session.bindTool(itemNamespace, clb);
                        CommandHelper.sendSuccess(player, "Bound Clipboard brush to " + item.material().name() + ".");
                    }
                    case "gravity" -> {
                        int radius = session.getBrushSize();
                        if (raw.length > 1 && raw[1].matches("\\d+")) radius = Integer.parseInt(raw[1]);
                        GravityBrush gb = new GravityBrush(config, sessionManager, asyncEngine, dispatcher, radius);
                        session.bindTool(itemNamespace, gb);
                        CommandHelper.sendSuccess(player, String.format("Bound Gravity brush (radius=%d) to %s.", radius, item.material().name()));
                    }
                    case "raise" -> {
                        int radius = session.getBrushSize();
                        if (raw.length > 1 && raw[1].matches("\\d+")) radius = Integer.parseInt(raw[1]);
                        RaiseLowerBrush rlb = new RaiseLowerBrush(config, sessionManager, asyncEngine, dispatcher, radius, true);
                        session.bindTool(itemNamespace, rlb);
                        CommandHelper.sendSuccess(player, String.format("Bound Raise brush (radius=%d) to %s.", radius, item.material().name()));
                    }
                    case "lower" -> {
                        int radius = session.getBrushSize();
                        if (raw.length > 1 && raw[1].matches("\\d+")) radius = Integer.parseInt(raw[1]);
                        RaiseLowerBrush rlb = new RaiseLowerBrush(config, sessionManager, asyncEngine, dispatcher, radius, false);
                        session.bindTool(itemNamespace, rlb);
                        CommandHelper.sendSuccess(player, String.format("Bound Lower brush (radius=%d) to %s.", radius, item.material().name()));
                    }
                    case "flatten" -> {
                        int radius = session.getBrushSize();
                        if (raw.length > 1 && raw[1].matches("\\d+")) radius = Integer.parseInt(raw[1]);
                        FlattenBrush fb = new FlattenBrush(config, sessionManager, asyncEngine, dispatcher, radius);
                        session.bindTool(itemNamespace, fb);
                        CommandHelper.sendSuccess(player, String.format("Bound Flatten brush (radius=%d) to %s.", radius, item.material().name()));
                    }
                    case "paint" -> {
                        int radius = session.getBrushSize();
                        String patternStr = "stone";
                        for (int i = 1; i < raw.length; i++) {
                            if (raw[i].matches("\\d+")) {
                                radius = Integer.parseInt(raw[i]);
                            } else {
                                patternStr = raw[i];
                            }
                        }
                        Pattern pattern = PatternParser.parsePattern(patternStr);
                        PaintBrush pb = new PaintBrush(config, sessionManager, asyncEngine, dispatcher, pattern, radius);
                        session.bindTool(itemNamespace, pb);
                        CommandHelper.sendSuccess(player, String.format("Bound Paint brush (%s, radius=%d) to %s.", patternStr, radius, item.material().name()));
                    }
                    default -> CommandHelper.sendError(player, "Unknown brush type: " + type + ". Available: sphere, cyl, smooth, clipboard, gravity, raise, lower, flatten, paint");
                }
            } catch (Exception e) {
                CommandHelper.sendError(player, "Failed to create brush: " + e.getMessage());
            }
        }, brushArgs);
        commands.add(brush);

        // 2. /mask [mask]
        Command mask = new Command("/mask", "mask");
        mask.setDefaultExecutor((sender, context) -> {
            if (!(sender instanceof Player player)) return;
            PlayerSession session = sessionManager.getSession(player);
            session.setBrushMask(null);
            CommandHelper.sendSuccess(player, "Brush mask disabled.");
        });
        var maskArg = ArgumentType.String("maskArg");
        mask.addSyntax((sender, context) -> {
            if (!(sender instanceof Player player)) return;
            PlayerSession session = sessionManager.getSession(player);
            String raw = context.get(maskArg);
            try {
                Mask m = PatternParser.parseMask(raw);
                session.setBrushMask(m);
                CommandHelper.sendSuccess(player, "Brush mask set to: " + raw);
            } catch (Exception e) {
                CommandHelper.sendError(player, "Invalid mask: " + e.getMessage());
            }
        }, maskArg);
        commands.add(mask);

        // 3. /size <radius>
        Command size = new Command("/size");
        size.setDefaultExecutor((sender, context) -> CommandHelper.sendError(sender, "Usage: /size <radius>"));
        var sizeArg = ArgumentType.Integer("radius");
        size.addSyntax((sender, context) -> {
            if (!(sender instanceof Player player)) return;
            int r = context.get(sizeArg);
            PlayerSession session = sessionManager.getSession(player);
            session.setBrushSize(r);

            ItemStack item = player.getItemInMainHand();
            if (!item.isAir()) {
                Tool tool = session.getTool(item.material().name());
                if (tool instanceof Brush b) {
                    b.setRadius(r);
                }
            }
            CommandHelper.sendSuccess(player, "Brush size set to: " + r);
        }, sizeArg);
        commands.add(size);

        // 4. /range <distance>
        Command range = new Command("/range");
        range.setDefaultExecutor((sender, context) -> CommandHelper.sendError(sender, "Usage: /range <distance>"));
        var rangeArg = ArgumentType.Integer("distance");
        range.addSyntax((sender, context) -> {
            if (!(sender instanceof Player player)) return;
            int d = context.get(rangeArg);
            PlayerSession session = sessionManager.getSession(player);
            session.setBrushRange(d);
            CommandHelper.sendSuccess(player, "Brush reach range set to: " + d);
        }, rangeArg);
        commands.add(range);

        // 5. /mat <pattern>
        Command mat = new Command("/mat", "mat");
        mat.setDefaultExecutor((sender, context) -> CommandHelper.sendError(sender, "Usage: /mat <pattern>"));
        var matArg = ArgumentType.String("pattern");
        mat.addSyntax((sender, context) -> {
            if (!(sender instanceof Player player)) return;
            String raw = context.get(matArg);
            try {
                Pattern p = PatternParser.parsePattern(raw);
                PlayerSession session = sessionManager.getSession(player);
                ItemStack item = player.getItemInMainHand();
                if (!item.isAir()) {
                    Tool tool = session.getTool(item.material().name());
                    if (tool instanceof Brush b) {
                        b.setPattern(p);
                        CommandHelper.sendSuccess(player, "Updated material for held brush to: " + raw);
                        return;
                    }
                }
                CommandHelper.sendInfo(player, "Brush pattern: " + raw);
            } catch (Exception e) {
                CommandHelper.sendError(player, "Invalid pattern: " + e.getMessage());
            }
        }, matArg);
        commands.add(mat);

        return commands;
    }
}
