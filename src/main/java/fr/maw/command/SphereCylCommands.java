package fr.maw.command;

import fr.maw.MawConfig;
import fr.maw.async.AsyncEditSession;
import fr.maw.async.MawAsyncEngine;
import fr.maw.async.TickDispatcher;
import fr.maw.operation.CylinderOperation;
import fr.maw.operation.SphereOperation;
import fr.maw.pattern.Pattern;
import fr.maw.pattern.PatternParser;
import fr.maw.session.PlayerSession;
import fr.maw.session.SessionManager;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;

import java.util.ArrayList;
import java.util.List;

public final class SphereCylCommands {

    private SphereCylCommands() {}

    public static List<Command> create(
            MawConfig config,
            SessionManager sessionManager,
            MawAsyncEngine asyncEngine,
            TickDispatcher dispatcher
    ) {
        Command sphere = new Command("//sphere", "/sphere", "sphere");
        sphere.setDefaultExecutor((sender, context) -> CommandHelper.sendError(sender, "Usage: //sphere [-h] <pattern> <radius> [-u] [-e]"));
        var sphereArg = ArgumentType.StringArray("args");
        sphere.addSyntax((sender, context) -> {
            if (!(sender instanceof Player player)) return;
            handleSphere(player, context.get(sphereArg), false, config, sessionManager, asyncEngine, dispatcher);
        }, sphereArg);

        Command hsphere = new Command("//hsphere", "/hsphere", "hsphere");
        hsphere.setDefaultExecutor((sender, context) -> CommandHelper.sendError(sender, "Usage: //hsphere <pattern> <radius> [-u] [-e]"));
        var hsphereArg = ArgumentType.StringArray("args");
        hsphere.addSyntax((sender, context) -> {
            if (!(sender instanceof Player player)) return;
            handleSphere(player, context.get(hsphereArg), true, config, sessionManager, asyncEngine, dispatcher);
        }, hsphereArg);

        Command cyl = new Command("//cyl", "/cyl", "cyl");
        cyl.setDefaultExecutor((sender, context) -> CommandHelper.sendError(sender, "Usage: //cyl [-h] <pattern> <radius> [height] [-u] [-e]"));
        var cylArg = ArgumentType.StringArray("args");
        cyl.addSyntax((sender, context) -> {
            if (!(sender instanceof Player player)) return;
            handleCyl(player, context.get(cylArg), false, config, sessionManager, asyncEngine, dispatcher);
        }, cylArg);

        Command hcyl = new Command("//hcyl", "/hcyl", "hcyl");
        hcyl.setDefaultExecutor((sender, context) -> CommandHelper.sendError(sender, "Usage: //hcyl <pattern> <radius> [height] [-u] [-e]"));
        var hcylArg = ArgumentType.StringArray("args");
        hcyl.addSyntax((sender, context) -> {
            if (!(sender instanceof Player player)) return;
            handleCyl(player, context.get(hcylArg), true, config, sessionManager, asyncEngine, dispatcher);
        }, hcylArg);

        return List.of(sphere, hsphere, cyl, hcyl);
    }

    private static void handleSphere(
            Player player,
            String[] rawArgs,
            boolean forceHollow,
            MawConfig config,
            SessionManager sessionManager,
            MawAsyncEngine asyncEngine,
            TickDispatcher dispatcher
    ) {
        Instance instance = player.getInstance();
        if (instance == null) return;

        boolean hollow = forceHollow || CommandHelper.hasFlag(rawArgs, "-h");
        boolean updatePhysics = CommandHelper.hasFlag(rawArgs, "-u");
        boolean manageEntities = CommandHelper.hasFlag(rawArgs, "-e");

        List<String> cleaned = new ArrayList<>();
        for (String a : rawArgs) {
            if (!a.equalsIgnoreCase("-h") && !a.equalsIgnoreCase("-u") && !a.equalsIgnoreCase("-e")) {
                cleaned.add(a);
            }
        }

        if (cleaned.size() < 2) {
            CommandHelper.sendError(player, "Usage: //sphere [-h] <pattern> <radius>");
            return;
        }

        Pattern pattern;
        double radiusX, radiusY, radiusZ;

        try {
            pattern = PatternParser.parsePattern(cleaned.get(0));
            String radiusStr = cleaned.get(1);
            if (radiusStr.contains(",")) {
                String[] radii = radiusStr.split(",");
                radiusX = Double.parseDouble(radii[0]);
                radiusY = Double.parseDouble(radii[1]);
                radiusZ = Double.parseDouble(radii[2]);
            } else {
                radiusX = radiusY = radiusZ = Double.parseDouble(radiusStr);
            }
        } catch (Exception e) {
            CommandHelper.sendError(player, "Invalid arguments: " + e.getMessage());
            return;
        }

        Point center = player.getPosition();
        PlayerSession playerSession = sessionManager.getSession(player);
        CommandHelper.sendInfo(player, "Generating sphere...");

        asyncEngine.runAsync(() -> {
            try {
                AsyncEditSession editSession = new AsyncEditSession(instance, config.maxBlocksPerOperation());
                SphereOperation.execute(editSession, center, radiusX, radiusY, radiusZ, hollow, pattern);

                editSession.commit(dispatcher, updatePhysics, manageEntities).thenAccept(result -> {
                    playerSession.getHistoryManager().record(editSession.createChangeSet());
                    CommandHelper.sendSuccess(player, "Sphere generated: " + result.toString());
                });
            } catch (Exception e) {
                CommandHelper.sendError(player, "Failed to generate sphere: " + e.getMessage());
            }
        });
    }

    private static void handleCyl(
            Player player,
            String[] rawArgs,
            boolean forceHollow,
            MawConfig config,
            SessionManager sessionManager,
            MawAsyncEngine asyncEngine,
            TickDispatcher dispatcher
    ) {
        Instance instance = player.getInstance();
        if (instance == null) return;

        boolean hollow = forceHollow || CommandHelper.hasFlag(rawArgs, "-h");
        boolean updatePhysics = CommandHelper.hasFlag(rawArgs, "-u");
        boolean manageEntities = CommandHelper.hasFlag(rawArgs, "-e");

        List<String> cleaned = new ArrayList<>();
        for (String a : rawArgs) {
            if (!a.equalsIgnoreCase("-h") && !a.equalsIgnoreCase("-u") && !a.equalsIgnoreCase("-e")) {
                cleaned.add(a);
            }
        }

        if (cleaned.size() < 2) {
            CommandHelper.sendError(player, "Usage: //cyl [-h] <pattern> <radius> [height]");
            return;
        }

        Pattern pattern;
        double radiusX, radiusZ;
        int height = 1;

        try {
            pattern = PatternParser.parsePattern(cleaned.get(0));
            String radiusStr = cleaned.get(1);
            if (radiusStr.contains(",")) {
                String[] radii = radiusStr.split(",");
                radiusX = Double.parseDouble(radii[0]);
                radiusZ = Double.parseDouble(radii[1]);
            } else {
                radiusX = radiusZ = Double.parseDouble(radiusStr);
            }

            if (cleaned.size() >= 3) {
                height = Integer.parseInt(cleaned.get(2));
            }
        } catch (Exception e) {
            CommandHelper.sendError(player, "Invalid arguments: " + e.getMessage());
            return;
        }

        Point center = player.getPosition();
        PlayerSession playerSession = sessionManager.getSession(player);
        CommandHelper.sendInfo(player, "Generating cylinder...");

        int finalHeight = height;
        asyncEngine.runAsync(() -> {
            try {
                AsyncEditSession editSession = new AsyncEditSession(instance, config.maxBlocksPerOperation());
                CylinderOperation.execute(editSession, center, radiusX, radiusZ, finalHeight, hollow, pattern);

                editSession.commit(dispatcher, updatePhysics, manageEntities).thenAccept(result -> {
                    playerSession.getHistoryManager().record(editSession.createChangeSet());
                    CommandHelper.sendSuccess(player, "Cylinder generated: " + result.toString());
                });
            } catch (Exception e) {
                CommandHelper.sendError(player, "Failed to generate cylinder: " + e.getMessage());
            }
        });
    }
}
