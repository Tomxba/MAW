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
        hcyl.setDefaultExecutor((sender, context) -> CommandHelper.sendError(sender, "Usage: //hcyl <pattern> <radius> [height] [thickness] [-u] [-e]"));
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

        List<String> cleaned = clean(rawArgs);

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
                AsyncEditSession editSession = CommandHelper.newSession(config, player, instance, config.maxBlocksPerOperation(), null);
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

    /**
     * A cylinder at the player's feet: {@code //cyl [-h] <pattern> <radius> [height] [thickness] [-u] [-e]}.
     * The fourth argument is the thickness of the wall of a hollow cylinder, so
     * {@code //hcyl stone 10 20 3} builds a tube of radius 10, 20 blocks tall, with a wall of 3 blocks.
     */
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

        List<String> cleaned = clean(rawArgs);

        if (cleaned.size() < 2) {
            CommandHelper.sendError(player, "Usage: //cyl [-h] <pattern> <radius> [height] [-u] [-e] | //hcyl <pattern> <radius> [height] [thickness]");
            return;
        }

        Pattern pattern;
        double radiusX, radiusZ;
        int height = 1;
        int thickness = 1;

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
            if (cleaned.size() >= 4) {
                thickness = Integer.parseInt(cleaned.get(3));
            }
        } catch (Exception e) {
            CommandHelper.sendError(player, "Invalid arguments: " + e.getMessage());
            return;
        }

        if (cleaned.size() >= 4 && !hollow) {
            // A thickness says how thick the wall of a hollow cylinder is: on a solid one it would mean
            // nothing, and silently ignoring it would leave the player with a shape they did not ask for.
            CommandHelper.sendError(player, cleaned.get(3) + " is the thickness of the wall of a hollow cylinder:"
                    + " use //hcyl or //cyl -h, e.g. //hcyl " + cleaned.get(0) + " " + cleaned.get(1) + " "
                    + cleaned.get(2) + " " + cleaned.get(3));
            return;
        }

        Point center = player.getPosition();
        PlayerSession playerSession = sessionManager.getSession(player);
        String what = hollow ? "Hollow cylinder (wall of " + Math.max(1, thickness) + " block(s))" : "Cylinder";
        CommandHelper.sendInfo(player, "Generating " + what.toLowerCase(java.util.Locale.ROOT) + "...");

        int finalHeight = height;
        int finalThickness = thickness;
        asyncEngine.runAsync(() -> {
            try {
                AsyncEditSession editSession = CommandHelper.newSession(config, player, instance, config.maxBlocksPerOperation(), null);
                CylinderOperation.execute(editSession, center, radiusX, radiusZ, finalHeight, hollow, pattern, finalThickness);

                editSession.commit(dispatcher, updatePhysics, manageEntities).thenAccept(result -> {
                    playerSession.getHistoryManager().record(editSession.createChangeSet());
                    CommandHelper.sendSuccess(player, what + " generated: " + result.toString());
                });
            } catch (Exception e) {
                CommandHelper.sendError(player, "Failed to generate cylinder: " + e.getMessage());
            }
        });
    }

    /** The arguments of a shape without its flags ({@code -h}, {@code -u}, {@code -e}). */
    private static List<String> clean(String[] rawArgs) {
        List<String> cleaned = new ArrayList<>();
        for (String a : rawArgs) {
            if (!a.equalsIgnoreCase("-h") && !a.equalsIgnoreCase("-u") && !a.equalsIgnoreCase("-e")) {
                cleaned.add(a);
            }
        }
        return cleaned;
    }
}
