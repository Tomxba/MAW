package fr.maw.command;

import fr.maw.MawConfig;
import fr.maw.async.AsyncEditSession;
import fr.maw.async.MawAsyncEngine;
import fr.maw.async.TickDispatcher;
import fr.maw.operation.ConeOperation;
import fr.maw.operation.EllipsoidOperation;
import fr.maw.operation.PyramidOperation;
import fr.maw.operation.TorusOperation;
import fr.maw.pattern.Pattern;
import fr.maw.pattern.PatternParser;
import fr.maw.selection.CuboidSelection;
import fr.maw.session.PlayerSession;
import fr.maw.session.SessionManager;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;

import java.util.ArrayList;
import java.util.List;

public final class ShapeCommands {

    private ShapeCommands() {}

    public static List<Command> create(
            MawConfig config,
            SessionManager sessionManager,
            MawAsyncEngine asyncEngine,
            TickDispatcher dispatcher
    ) {
        List<Command> commands = new ArrayList<>();

        // 1. //pyramid & //hpyramid
        Command pyramid = new Command("//pyramid", "/pyramid", "pyramid");
        Command hpyramid = new Command("//hpyramid", "/hpyramid", "hpyramid");
        setupPyramid(pyramid, false, config, sessionManager, asyncEngine, dispatcher);
        setupPyramid(hpyramid, true, config, sessionManager, asyncEngine, dispatcher);
        commands.add(pyramid);
        commands.add(hpyramid);

        // 2. //cone & //hcone
        Command cone = new Command("//cone", "/cone", "cone");
        Command hcone = new Command("//hcone", "/hcone", "hcone");
        setupCone(cone, false, config, sessionManager, asyncEngine, dispatcher);
        setupCone(hcone, true, config, sessionManager, asyncEngine, dispatcher);
        commands.add(cone);
        commands.add(hcone);

        // 3. //torus & //htorus
        Command torus = new Command("//torus", "/torus", "torus");
        Command htorus = new Command("//htorus", "/htorus", "htorus");
        setupTorus(torus, false, config, sessionManager, asyncEngine, dispatcher);
        setupTorus(htorus, true, config, sessionManager, asyncEngine, dispatcher);
        commands.add(torus);
        commands.add(htorus);

        // 4. //ellipsoid & //hellipsoid
        Command ellipsoid = new Command("//ellipsoid", "/ellipsoid", "ellipsoid");
        Command hellipsoid = new Command("//hellipsoid", "/hellipsoid", "hellipsoid");
        setupEllipsoid(ellipsoid, false, config, sessionManager, asyncEngine, dispatcher);
        setupEllipsoid(hellipsoid, true, config, sessionManager, asyncEngine, dispatcher);
        commands.add(ellipsoid);
        commands.add(hellipsoid);

        return commands;
    }

    private static void setupPyramid(Command cmd, boolean defaultHollow, MawConfig config, SessionManager sessionManager, MawAsyncEngine asyncEngine, TickDispatcher dispatcher) {
        cmd.setDefaultExecutor((sender, context) -> CommandHelper.sendError(sender, "Usage: " + cmd.getName() + " [-h] <pattern> <size>"));
        var argsType = ArgumentType.StringArray("args");
        cmd.addSyntax((sender, context) -> {
            if (!(sender instanceof Player player)) return;
            Instance instance = player.getInstance();
            if (instance == null) return;

            String[] args = context.get(argsType);
            boolean hollow = defaultHollow || CommandHelper.hasFlag(args, "-h");
            String patternStr = "stone";
            int size = 5;

            for (String a : args) {
                if (a.equalsIgnoreCase("-h")) continue;
                if (a.matches("\\d+")) {
                    size = Integer.parseInt(a);
                } else {
                    patternStr = a;
                }
            }

            Pattern pattern;
            try {
                pattern = PatternParser.parsePattern(patternStr);
            } catch (Exception e) {
                CommandHelper.sendError(player, "Invalid pattern: " + e.getMessage());
                return;
            }

            Point origin = player.getPosition();
            final int finalSize = size;
            final boolean finalHollow = hollow;
            final String finalPat = patternStr;

            PlayerSession session = sessionManager.getSession(player);
            Point min = new Vec(origin.blockX() - size, origin.blockY(), origin.blockZ() - size);
            Point max = new Vec(origin.blockX() + size, origin.blockY() + size, origin.blockZ() + size);
            CuboidSelection sel = new CuboidSelection(min, max);

            CommandHelper.sendInfo(player, String.format("Generating pyramid (size=%d, hollow=%s)...", size, hollow));
            asyncEngine.runAsync(() -> {
                try {
                    AsyncEditSession editSession = CommandHelper.newSession(config, player, instance, config.maxBlocksPerOperation(), sel);
                    long count = PyramidOperation.execute(editSession, origin, pattern, finalSize, finalHollow);

                    editSession.commit(dispatcher, session.isUpdatePhysics(), session.isManageEntities()).thenAccept(res -> {
                        session.getHistoryManager().record(editSession.createChangeSet());
                        CommandHelper.sendSuccess(player, String.format("Pyramid generated with %,d blocks (%s).", count, finalPat));
                    });
                } catch (Exception e) {
                    CommandHelper.sendError(player, "Failed to generate pyramid: " + e.getMessage());
                }
            });
        }, argsType);
    }

    private static void setupCone(Command cmd, boolean defaultHollow, MawConfig config, SessionManager sessionManager, MawAsyncEngine asyncEngine, TickDispatcher dispatcher) {
        cmd.setDefaultExecutor((sender, context) -> CommandHelper.sendError(sender, "Usage: " + cmd.getName() + " [-h] <pattern> <radius> [height]"));
        var argsType = ArgumentType.StringArray("args");
        cmd.addSyntax((sender, context) -> {
            if (!(sender instanceof Player player)) return;
            Instance instance = player.getInstance();
            if (instance == null) return;

            String[] args = context.get(argsType);
            boolean hollow = defaultHollow || CommandHelper.hasFlag(args, "-h");
            String patternStr = "stone";
            int radius = 5;
            int height = 10;

            List<Integer> nums = new ArrayList<>();
            for (String a : args) {
                if (a.equalsIgnoreCase("-h")) continue;
                if (a.matches("\\d+")) {
                    nums.add(Integer.parseInt(a));
                } else {
                    patternStr = a;
                }
            }
            if (!nums.isEmpty()) radius = nums.get(0);
            if (nums.size() > 1) height = nums.get(1);

            Pattern pattern;
            try {
                pattern = PatternParser.parsePattern(patternStr);
            } catch (Exception e) {
                CommandHelper.sendError(player, "Invalid pattern: " + e.getMessage());
                return;
            }

            Point origin = player.getPosition();
            final int finalRadius = radius;
            final int finalHeight = height;
            final boolean finalHollow = hollow;
            final String finalPat = patternStr;

            PlayerSession session = sessionManager.getSession(player);
            Point min = new Vec(origin.blockX() - radius, origin.blockY(), origin.blockZ() - radius);
            Point max = new Vec(origin.blockX() + radius, origin.blockY() + height, origin.blockZ() + radius);
            CuboidSelection sel = new CuboidSelection(min, max);

            CommandHelper.sendInfo(player, String.format("Generating cone (radius=%d, height=%d, hollow=%s)...", radius, height, hollow));
            asyncEngine.runAsync(() -> {
                try {
                    AsyncEditSession editSession = CommandHelper.newSession(config, player, instance, config.maxBlocksPerOperation(), sel);
                    long count = ConeOperation.execute(editSession, origin, pattern, finalRadius, finalHeight, finalHollow);

                    editSession.commit(dispatcher, session.isUpdatePhysics(), session.isManageEntities()).thenAccept(res -> {
                        session.getHistoryManager().record(editSession.createChangeSet());
                        CommandHelper.sendSuccess(player, String.format("Cone generated with %,d blocks (%s).", count, finalPat));
                    });
                } catch (Exception e) {
                    CommandHelper.sendError(player, "Failed to generate cone: " + e.getMessage());
                }
            });
        }, argsType);
    }

    private static void setupTorus(Command cmd, boolean defaultHollow, MawConfig config, SessionManager sessionManager, MawAsyncEngine asyncEngine, TickDispatcher dispatcher) {
        cmd.setDefaultExecutor((sender, context) -> CommandHelper.sendError(sender, "Usage: " + cmd.getName() + " [-h] <pattern> <majorRadius> <minorRadius>"));
        var argsType = ArgumentType.StringArray("args");
        cmd.addSyntax((sender, context) -> {
            if (!(sender instanceof Player player)) return;
            Instance instance = player.getInstance();
            if (instance == null) return;

            String[] args = context.get(argsType);
            boolean hollow = defaultHollow || CommandHelper.hasFlag(args, "-h");
            String patternStr = "stone";
            int major = 10;
            int minor = 3;

            List<Integer> nums = new ArrayList<>();
            for (String a : args) {
                if (a.equalsIgnoreCase("-h")) continue;
                if (a.matches("\\d+")) {
                    nums.add(Integer.parseInt(a));
                } else {
                    patternStr = a;
                }
            }
            if (!nums.isEmpty()) major = nums.get(0);
            if (nums.size() > 1) minor = nums.get(1);

            Pattern pattern;
            try {
                pattern = PatternParser.parsePattern(patternStr);
            } catch (Exception e) {
                CommandHelper.sendError(player, "Invalid pattern: " + e.getMessage());
                return;
            }

            Point origin = player.getPosition();
            final int finalMajor = major;
            final int finalMinor = minor;
            final boolean finalHollow = hollow;
            final String finalPat = patternStr;

            PlayerSession session = sessionManager.getSession(player);
            int maxBound = major + minor;
            Point min = new Vec(origin.blockX() - maxBound, origin.blockY() - minor, origin.blockZ() - maxBound);
            Point max = new Vec(origin.blockX() + maxBound, origin.blockY() + minor, origin.blockZ() + maxBound);
            CuboidSelection sel = new CuboidSelection(min, max);

            CommandHelper.sendInfo(player, String.format("Generating torus (major=%d, minor=%d, hollow=%s)...", major, minor, hollow));
            asyncEngine.runAsync(() -> {
                try {
                    AsyncEditSession editSession = CommandHelper.newSession(config, player, instance, config.maxBlocksPerOperation(), sel);
                    long count = TorusOperation.execute(editSession, origin, pattern, finalMajor, finalMinor, finalHollow);

                    editSession.commit(dispatcher, session.isUpdatePhysics(), session.isManageEntities()).thenAccept(res -> {
                        session.getHistoryManager().record(editSession.createChangeSet());
                        CommandHelper.sendSuccess(player, String.format("Torus generated with %,d blocks (%s).", count, finalPat));
                    });
                } catch (Exception e) {
                    CommandHelper.sendError(player, "Failed to generate torus: " + e.getMessage());
                }
            });
        }, argsType);
    }

    private static void setupEllipsoid(Command cmd, boolean defaultHollow, MawConfig config, SessionManager sessionManager, MawAsyncEngine asyncEngine, TickDispatcher dispatcher) {
        cmd.setDefaultExecutor((sender, context) -> CommandHelper.sendError(sender, "Usage: " + cmd.getName() + " [-h] <pattern> <rx> <ry> <rz>"));
        var argsType = ArgumentType.StringArray("args");
        cmd.addSyntax((sender, context) -> {
            if (!(sender instanceof Player player)) return;
            Instance instance = player.getInstance();
            if (instance == null) return;

            String[] args = context.get(argsType);
            boolean hollow = defaultHollow || CommandHelper.hasFlag(args, "-h");
            String patternStr = "stone";
            int rx = 5, ry = 8, rz = 5;

            List<Integer> nums = new ArrayList<>();
            for (String a : args) {
                if (a.equalsIgnoreCase("-h")) continue;
                if (a.matches("\\d+")) {
                    nums.add(Integer.parseInt(a));
                } else {
                    patternStr = a;
                }
            }
            if (nums.size() >= 3) {
                rx = nums.get(0);
                ry = nums.get(1);
                rz = nums.get(2);
            } else if (nums.size() == 1) {
                rx = ry = rz = nums.get(0);
            }

            Pattern pattern;
            try {
                pattern = PatternParser.parsePattern(patternStr);
            } catch (Exception e) {
                CommandHelper.sendError(player, "Invalid pattern: " + e.getMessage());
                return;
            }

            Point origin = player.getPosition();
            final int finalRx = rx, finalRy = ry, finalRz = rz;
            final boolean finalHollow = hollow;
            final String finalPat = patternStr;

            PlayerSession session = sessionManager.getSession(player);
            Point min = new Vec(origin.blockX() - rx, origin.blockY() - ry, origin.blockZ() - rz);
            Point max = new Vec(origin.blockX() + rx, origin.blockY() + ry, origin.blockZ() + rz);
            CuboidSelection sel = new CuboidSelection(min, max);

            CommandHelper.sendInfo(player, String.format("Generating ellipsoid (rx=%d, ry=%d, rz=%d, hollow=%s)...", rx, ry, rz, hollow));
            asyncEngine.runAsync(() -> {
                try {
                    AsyncEditSession editSession = CommandHelper.newSession(config, player, instance, config.maxBlocksPerOperation(), sel);
                    long count = EllipsoidOperation.execute(editSession, origin, pattern, finalRx, finalRy, finalRz, finalHollow);

                    editSession.commit(dispatcher, session.isUpdatePhysics(), session.isManageEntities()).thenAccept(res -> {
                        session.getHistoryManager().record(editSession.createChangeSet());
                        CommandHelper.sendSuccess(player, String.format("Ellipsoid generated with %,d blocks (%s).", count, finalPat));
                    });
                } catch (Exception e) {
                    CommandHelper.sendError(player, "Failed to generate ellipsoid: " + e.getMessage());
                }
            });
        }, argsType);
    }
}
