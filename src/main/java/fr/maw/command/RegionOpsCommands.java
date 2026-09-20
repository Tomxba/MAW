package fr.maw.command;

import fr.maw.MawConfig;
import fr.maw.async.AsyncEditSession;
import fr.maw.async.MawAsyncEngine;
import fr.maw.async.TickDispatcher;
import fr.maw.clipboard.Clipboard;
import fr.maw.clipboard.Transform.Direction;
import fr.maw.operation.*;
import fr.maw.pattern.Pattern;
import fr.maw.pattern.PatternParser;
import fr.maw.selection.CuboidSelection;
import fr.maw.selection.Selection;
import fr.maw.selection.SelectionHelper;
import fr.maw.session.PlayerSession;
import fr.maw.session.SessionManager;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;

import java.util.ArrayList;
import java.util.List;

public final class RegionOpsCommands {

    private RegionOpsCommands() {}

    public static List<Command> create(
            MawConfig config,
            SessionManager sessionManager,
            MawAsyncEngine asyncEngine,
            TickDispatcher dispatcher
    ) {
        List<Command> commands = new ArrayList<>();

        // 1. //move
        Command move = new Command("//move", "/move", "move");
        move.setDefaultExecutor((sender, context) -> CommandHelper.sendError(sender, "Usage: //move [count] [direction] [-s]"));
        var moveArgs = ArgumentType.StringArray("args");
        move.addSyntax((sender, context) -> {
            if (!(sender instanceof Player player)) return;
            Instance instance = player.getInstance();
            if (instance == null) return;

            PlayerSession session = sessionManager.getSession(player);
            if (!session.isSelectionComplete()) {
                CommandHelper.sendError(player, "Make a selection first.");
                return;
            }

            String[] args = context.get(moveArgs);
            int count = 1;
            Direction dir = SelectionHelper.getPlayerDirection(player);
            boolean shiftSel = CommandHelper.hasFlag(args, "-s");

            for (String a : args) {
                if (a.equalsIgnoreCase("-s")) continue;
                if (a.matches("\\d+")) {
                    count = Integer.parseInt(a);
                } else {
                    dir = SelectionHelper.parseDirection(a, dir);
                }
            }

            final int dist = count;
            final Direction direction = dir;
            final boolean doShift = shiftSel;
            Selection selection = session.getSelection();

            CommandHelper.sendInfo(player, String.format("Moving selection %d blocks %s...", dist, direction.name().toLowerCase()));

            asyncEngine.runAsync(() -> {
                try {
                    AsyncEditSession editSession = CommandHelper.newSession(config, player, instance, config.maxBlocksPerOperation(), selection);
                    long moved = MoveOperation.execute(editSession, selection, dist, direction, true);

                    editSession.commit(dispatcher, session.isUpdatePhysics(), session.isManageEntities()).thenAccept(res -> {
                        session.getHistoryManager().record(editSession.createChangeSet());

                        if (doShift && selection instanceof CuboidSelection cuboid) {
                            CuboidSelection shifted = SelectionHelper.shift(cuboid, dist, direction);
                            session.setPos1(new Vec(shifted.getMinX(), shifted.getMinY(), shifted.getMinZ()));
                            session.setPos2(new Vec(shifted.getMaxX(), shifted.getMaxY(), shifted.getMaxZ()));
                        }

                        CommandHelper.sendSuccess(player, String.format("Moved %,d blocks.", moved));
                    });
                } catch (Exception e) {
                    CommandHelper.sendError(player, "Failed to move blocks: " + e.getMessage());
                }
            });
        }, moveArgs);
        commands.add(move);

        // 2. //stack
        Command stack = new Command("//stack", "/stack", "stack");
        stack.setDefaultExecutor((sender, context) -> CommandHelper.sendError(sender, "Usage: //stack [count] [direction] [-s]"));
        var stackArgs = ArgumentType.StringArray("args");
        stack.addSyntax((sender, context) -> {
            if (!(sender instanceof Player player)) return;
            Instance instance = player.getInstance();
            if (instance == null) return;

            PlayerSession session = sessionManager.getSession(player);
            if (!session.isSelectionComplete()) {
                CommandHelper.sendError(player, "Make a selection first.");
                return;
            }

            String[] args = context.get(stackArgs);
            int count = 1;
            Direction dir = SelectionHelper.getPlayerDirection(player);

            for (String a : args) {
                if (a.equalsIgnoreCase("-s")) continue;
                if (a.matches("\\d+")) {
                    count = Integer.parseInt(a);
                } else {
                    dir = SelectionHelper.parseDirection(a, dir);
                }
            }

            final int repeat = count;
            final Direction direction = dir;
            Selection selection = session.getSelection();

            CommandHelper.sendInfo(player, String.format("Stacking selection %d times %s...", repeat, direction.name().toLowerCase()));

            asyncEngine.runAsync(() -> {
                try {
                    AsyncEditSession editSession = CommandHelper.newSession(config, player, instance, config.maxBlocksPerOperation(), selection);
                    long stacked = StackOperation.execute(editSession, selection, repeat, direction);

                    editSession.commit(dispatcher, session.isUpdatePhysics(), session.isManageEntities()).thenAccept(res -> {
                        session.getHistoryManager().record(editSession.createChangeSet());
                        CommandHelper.sendSuccess(player, String.format("Stacked %,d blocks.", stacked));
                    });
                } catch (Exception e) {
                    CommandHelper.sendError(player, "Failed to stack blocks: " + e.getMessage());
                }
            });
        }, stackArgs);
        commands.add(stack);

        // 3. //cut
        Command cut = new Command("//cut", "/cut", "cut");
        cut.setDefaultExecutor((sender, context) -> {
            if (!(sender instanceof Player player)) return;
            Instance instance = player.getInstance();
            if (instance == null) return;

            PlayerSession session = sessionManager.getSession(player);
            if (!session.isSelectionComplete()) {
                CommandHelper.sendError(player, "Make a selection first.");
                return;
            }

            Selection selection = session.getSelection();
            CommandHelper.sendInfo(player, "Cutting selection (" + String.format("%,d", selection.getVolume()) + " blocks)...");

            asyncEngine.runAsync(() -> {
                try {
                    Clipboard clipboard = CopyOperation.execute(instance, selection, player.getPosition(), session.isManageEntities());
                    session.setClipboard(clipboard);

                    AsyncEditSession editSession = CommandHelper.newSession(config, player, instance, config.maxBlocksPerOperation(), selection);
                    for (Point p : selection) {
                        editSession.setBlock(p.blockX(), p.blockY(), p.blockZ(), Block.AIR);
                    }

                    editSession.commit(dispatcher, session.isUpdatePhysics(), session.isManageEntities()).thenAccept(res -> {
                        session.getHistoryManager().record(editSession.createChangeSet());
                        CommandHelper.sendSuccess(player, String.format("Cut %,d blocks to clipboard.", clipboard.size()));
                    });
                } catch (Exception e) {
                    CommandHelper.sendError(player, "Failed to cut selection: " + e.getMessage());
                }
            });
        });
        commands.add(cut);

        // 4. //naturalize
        Command naturalize = new Command("//naturalize", "/naturalize", "naturalize");
        naturalize.setDefaultExecutor((sender, context) -> {
            if (!(sender instanceof Player player)) return;
            Instance instance = player.getInstance();
            if (instance == null) return;

            PlayerSession session = sessionManager.getSession(player);
            if (!session.isSelectionComplete()) {
                CommandHelper.sendError(player, "Make a selection first.");
                return;
            }

            Selection selection = session.getSelection();
            asyncEngine.runAsync(() -> {
                try {
                    AsyncEditSession editSession = CommandHelper.newSession(config, player, instance, config.maxBlocksPerOperation(), selection);
                    long count = NaturalizeOperation.execute(editSession, selection);

                    editSession.commit(dispatcher, session.isUpdatePhysics(), session.isManageEntities()).thenAccept(res -> {
                        session.getHistoryManager().record(editSession.createChangeSet());
                        CommandHelper.sendSuccess(player, String.format("Naturalized %,d blocks.", count));
                    });
                } catch (Exception e) {
                    CommandHelper.sendError(player, "Failed to naturalize: " + e.getMessage());
                }
            });
        });
        commands.add(naturalize);

        // 5. //overlay
        Command overlay = new Command("//overlay", "/overlay", "overlay");
        overlay.setDefaultExecutor((sender, context) -> CommandHelper.sendError(sender, "Usage: //overlay <pattern>"));
        var patternArg = ArgumentType.String("pattern");
        overlay.addSyntax((sender, context) -> {
            if (!(sender instanceof Player player)) return;
            Instance instance = player.getInstance();
            if (instance == null) return;

            PlayerSession session = sessionManager.getSession(player);
            if (!session.isSelectionComplete()) {
                CommandHelper.sendError(player, "Make a selection first.");
                return;
            }

            String raw = context.get(patternArg);
            Pattern pattern;
            try {
                pattern = PatternParser.parsePattern(raw);
            } catch (Exception e) {
                CommandHelper.sendError(player, "Invalid pattern: " + e.getMessage());
                return;
            }

            Selection selection = session.getSelection();
            asyncEngine.runAsync(() -> {
                try {
                    AsyncEditSession editSession = CommandHelper.newSession(config, player, instance, config.maxBlocksPerOperation(), selection);
                    long count = OverlayOperation.execute(editSession, selection, pattern);

                    editSession.commit(dispatcher, session.isUpdatePhysics(), session.isManageEntities()).thenAccept(res -> {
                        session.getHistoryManager().record(editSession.createChangeSet());
                        CommandHelper.sendSuccess(player, String.format("Overlayed %,d blocks with %s.", count, raw));
                    });
                } catch (Exception e) {
                    CommandHelper.sendError(player, "Failed to overlay: " + e.getMessage());
                }
            });
        }, patternArg);
        commands.add(overlay);

        // 6. //smooth
        Command smooth = new Command("//smooth", "/smooth", "smooth");
        smooth.setDefaultExecutor((sender, context) -> {
            executeSmooth(sender, sessionManager, config, asyncEngine, dispatcher, 1);
        });
        var iterArg = ArgumentType.Integer("iterations");
        smooth.addSyntax((sender, context) -> {
            int it = context.get(iterArg);
            executeSmooth(sender, sessionManager, config, asyncEngine, dispatcher, it);
        }, iterArg);
        commands.add(smooth);

        // 7. //line
        Command line = new Command("//line", "/line", "line");
        line.setDefaultExecutor((sender, context) -> CommandHelper.sendError(sender, "Usage: //line <pattern> [thickness]"));
        var linePatternArg = ArgumentType.String("pattern");
        var lineThickArg = ArgumentType.Integer("thickness");
        line.addSyntax((sender, context) -> {
            executeLine(sender, sessionManager, config, asyncEngine, dispatcher, context.get(linePatternArg), 0);
        }, linePatternArg);
        line.addSyntax((sender, context) -> {
            executeLine(sender, sessionManager, config, asyncEngine, dispatcher, context.get(linePatternArg), context.get(lineThickArg));
        }, linePatternArg, lineThickArg);
        commands.add(line);

        // 8. //center
        Command center = new Command("//center", "/center", "center");
        center.setDefaultExecutor((sender, context) -> CommandHelper.sendError(sender, "Usage: //center <pattern>"));
        var centerPatternArg = ArgumentType.String("pattern");
        center.addSyntax((sender, context) -> {
            if (!(sender instanceof Player player)) return;
            Instance instance = player.getInstance();
            if (instance == null) return;

            PlayerSession session = sessionManager.getSession(player);
            if (!session.isSelectionComplete()) {
                CommandHelper.sendError(player, "Make a selection first.");
                return;
            }

            Pattern pattern;
            try {
                pattern = PatternParser.parsePattern(context.get(centerPatternArg));
            } catch (Exception e) {
                CommandHelper.sendError(player, "Invalid pattern: " + e.getMessage());
                return;
            }

            Selection selection = session.getSelection();
            asyncEngine.runAsync(() -> {
                try {
                    AsyncEditSession editSession = CommandHelper.newSession(config, player, instance, 10, selection);
                    long count = CenterOperation.execute(editSession, selection, pattern);

                    editSession.commit(dispatcher, session.isUpdatePhysics(), session.isManageEntities()).thenAccept(res -> {
                        session.getHistoryManager().record(editSession.createChangeSet());
                        CommandHelper.sendSuccess(player, String.format("Set center of selection (%,d blocks).", count));
                    });
                } catch (Exception e) {
                    CommandHelper.sendError(player, "Center error: " + e.getMessage());
                }
            });
        }, centerPatternArg);
        commands.add(center);

        // 9. //fall
        Command fall = new Command("//fall", "/fall", "fall");
        fall.setDefaultExecutor((sender, context) -> {
            if (!(sender instanceof Player player)) return;
            Instance instance = player.getInstance();
            if (instance == null) return;

            PlayerSession session = sessionManager.getSession(player);
            if (!session.isSelectionComplete()) {
                CommandHelper.sendError(player, "Make a selection first.");
                return;
            }

            Selection selection = session.getSelection();
            asyncEngine.runAsync(() -> {
                try {
                    AsyncEditSession editSession = CommandHelper.newSession(config, player, instance, config.maxBlocksPerOperation(), selection);
                    long count = FallOperation.execute(editSession, selection);

                    editSession.commit(dispatcher, session.isUpdatePhysics(), session.isManageEntities()).thenAccept(res -> {
                        session.getHistoryManager().record(editSession.createChangeSet());
                        CommandHelper.sendSuccess(player, String.format("Fell %,d gravity blocks.", count));
                    });
                } catch (Exception e) {
                    CommandHelper.sendError(player, "Fall error: " + e.getMessage());
                }
            });
        });
        commands.add(fall);

        // 10. //forest
        Command forest = new Command("//forest", "/forest", "forest");
        forest.setDefaultExecutor((sender, context) -> executeForest(sender, sessionManager, config, asyncEngine, dispatcher, "oak", 0.05));
        var forestTypeArg = ArgumentType.String("type");
        forest.addSyntax((sender, context) -> {
            executeForest(sender, sessionManager, config, asyncEngine, dispatcher, context.get(forestTypeArg), 0.05);
        }, forestTypeArg);
        commands.add(forest);

        // 11. //flora
        Command flora = new Command("//flora", "/flora", "flora");
        flora.setDefaultExecutor((sender, context) -> executeFlora(sender, sessionManager, config, asyncEngine, dispatcher, 0.20));
        var floraDensityArg = ArgumentType.Double("density");
        flora.addSyntax((sender, context) -> {
            executeFlora(sender, sessionManager, config, asyncEngine, dispatcher, context.get(floraDensityArg));
        }, floraDensityArg);
        commands.add(flora);

        return commands;
    }

    private static void executeSmooth(Object sender, SessionManager sessionManager, MawConfig config, MawAsyncEngine asyncEngine, TickDispatcher dispatcher, int it) {
        if (!(sender instanceof Player player)) return;
        Instance instance = player.getInstance();
        if (instance == null) return;

        PlayerSession session = sessionManager.getSession(player);
        if (!session.isSelectionComplete()) {
            CommandHelper.sendError(player, "Make a selection first.");
            return;
        }

        Selection selection = session.getSelection();
        asyncEngine.runAsync(() -> {
            try {
                AsyncEditSession editSession = CommandHelper.newSession(config, player, instance, config.maxBlocksPerOperation(), selection);
                long count = SmoothOperation.execute(editSession, selection, it);

                editSession.commit(dispatcher, session.isUpdatePhysics(), session.isManageEntities()).thenAccept(res -> {
                    session.getHistoryManager().record(editSession.createChangeSet());
                    CommandHelper.sendSuccess(player, String.format("Smoothed terrain (%d iterations, %,d blocks changed).", it, count));
                });
            } catch (Exception e) {
                CommandHelper.sendError(player, "Failed to smooth: " + e.getMessage());
            }
        });
    }

    private static void executeLine(Object sender, SessionManager sessionManager, MawConfig config, MawAsyncEngine asyncEngine, TickDispatcher dispatcher, String patternStr, int thickness) {
        if (!(sender instanceof Player player)) return;
        Instance instance = player.getInstance();
        if (instance == null) return;

        PlayerSession session = sessionManager.getSession(player);
        if (!session.isSelectionComplete()) {
            CommandHelper.sendError(player, "Set pos1 and pos2 first.");
            return;
        }

        Pattern pattern;
        try {
            pattern = PatternParser.parsePattern(patternStr);
        } catch (Exception e) {
            CommandHelper.sendError(player, "Invalid pattern: " + e.getMessage());
            return;
        }

        Point p1 = session.getPos1();
        Point p2 = session.getPos2();
        CuboidSelection bound = new CuboidSelection(p1, p2);
        CuboidSelection sel = SelectionHelper.outset(bound, thickness + 1);

        asyncEngine.runAsync(() -> {
            try {
                AsyncEditSession editSession = CommandHelper.newSession(config, player, instance, config.maxBlocksPerOperation(), sel);
                long count = LineOperation.execute(editSession, p1, p2, pattern, thickness);

                editSession.commit(dispatcher, session.isUpdatePhysics(), session.isManageEntities()).thenAccept(res -> {
                    session.getHistoryManager().record(editSession.createChangeSet());
                    CommandHelper.sendSuccess(player, String.format("Drew line with %s (%,d blocks).", patternStr, count));
                });
            } catch (Exception e) {
                CommandHelper.sendError(player, "Failed to draw line: " + e.getMessage());
            }
        });
    }

    private static void executeForest(Object sender, SessionManager sessionManager, MawConfig config, MawAsyncEngine asyncEngine, TickDispatcher dispatcher, String type, double density) {
        if (!(sender instanceof Player player)) return;
        Instance instance = player.getInstance();
        if (instance == null) return;

        PlayerSession session = sessionManager.getSession(player);
        if (!session.isSelectionComplete()) {
            CommandHelper.sendError(player, "Make a selection first.");
            return;
        }

        Selection selection = session.getSelection();
        asyncEngine.runAsync(() -> {
            try {
                AsyncEditSession editSession = CommandHelper.newSession(config, player, instance, config.maxBlocksPerOperation(), selection);
                long count = ForestOperation.execute(editSession, selection, type, density);

                editSession.commit(dispatcher, session.isUpdatePhysics(), session.isManageEntities()).thenAccept(res -> {
                    session.getHistoryManager().record(editSession.createChangeSet());
                    CommandHelper.sendSuccess(player, String.format("Generated forest with %,d trees (%s).", count, type));
                });
            } catch (Exception e) {
                CommandHelper.sendError(player, "Forest error: " + e.getMessage());
            }
        });
    }

    private static void executeFlora(Object sender, SessionManager sessionManager, MawConfig config, MawAsyncEngine asyncEngine, TickDispatcher dispatcher, double density) {
        if (!(sender instanceof Player player)) return;
        Instance instance = player.getInstance();
        if (instance == null) return;

        PlayerSession session = sessionManager.getSession(player);
        if (!session.isSelectionComplete()) {
            CommandHelper.sendError(player, "Make a selection first.");
            return;
        }

        Selection selection = session.getSelection();
        asyncEngine.runAsync(() -> {
            try {
                AsyncEditSession editSession = CommandHelper.newSession(config, player, instance, config.maxBlocksPerOperation(), selection);
                long count = FloraOperation.execute(editSession, selection, density);

                editSession.commit(dispatcher, session.isUpdatePhysics(), session.isManageEntities()).thenAccept(res -> {
                    session.getHistoryManager().record(editSession.createChangeSet());
                    CommandHelper.sendSuccess(player, String.format("Placed %,d flora blocks.", count));
                });
            } catch (Exception e) {
                CommandHelper.sendError(player, "Flora error: " + e.getMessage());
            }
        });
    }
}
