package fr.maw.command;

import fr.maw.MawConfig;
import fr.maw.async.AsyncEditSession;
import fr.maw.async.MawAsyncEngine;
import fr.maw.async.TickDispatcher;
import fr.maw.selection.CuboidSelection;
import fr.maw.session.PlayerSession;
import fr.maw.session.SessionManager;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.ItemEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class EnvironmentCommands {

    private EnvironmentCommands() {}

    public static List<Command> create(
            MawConfig config,
            SessionManager sessionManager,
            MawAsyncEngine asyncEngine,
            TickDispatcher dispatcher
    ) {
        List<Command> commands = new ArrayList<>();

        // 1. //drain <radius>
        Command drain = new Command("//drain", "/drain", "drain");
        drain.setDefaultExecutor((sender, context) -> executeDrain(sender, sessionManager, config, asyncEngine, dispatcher, 10));
        var drainArg = ArgumentType.Integer("radius");
        drain.addSyntax((sender, context) -> executeDrain(sender, sessionManager, config, asyncEngine, dispatcher, context.get(drainArg)), drainArg);
        commands.add(drain);

        // 2. //fixwater & //fixlava
        Command fixwater = new Command("//fixwater", "/fixwater", "fixwater");
        fixwater.setDefaultExecutor((sender, context) -> executeFixFluid(sender, sessionManager, config, asyncEngine, dispatcher, 10, true));
        var fixwaterArg = ArgumentType.Integer("radius");
        fixwater.addSyntax((sender, context) -> executeFixFluid(sender, sessionManager, config, asyncEngine, dispatcher, context.get(fixwaterArg), true), fixwaterArg);
        commands.add(fixwater);

        Command fixlava = new Command("//fixlava", "/fixlava", "fixlava");
        fixlava.setDefaultExecutor((sender, context) -> executeFixFluid(sender, sessionManager, config, asyncEngine, dispatcher, 10, false));
        var fixlavaArg = ArgumentType.Integer("radius");
        fixlava.addSyntax((sender, context) -> executeFixFluid(sender, sessionManager, config, asyncEngine, dispatcher, context.get(fixlavaArg), false), fixlavaArg);
        commands.add(fixlava);

        // 3. //extinguish [radius] (or //ex)
        Command ex = new Command("//extinguish", "/extinguish", "extinguish", "//ex", "/ex", "ex");
        ex.setDefaultExecutor((sender, context) -> executeExtinguish(sender, sessionManager, config, asyncEngine, dispatcher, 40));
        var exArg = ArgumentType.Integer("radius");
        ex.addSyntax((sender, context) -> executeExtinguish(sender, sessionManager, config, asyncEngine, dispatcher, context.get(exArg)), exArg);
        commands.add(ex);

        // 4. //snow & //thaw
        Command snow = new Command("//snow", "/snow", "snow");
        snow.setDefaultExecutor((sender, context) -> executeSnow(sender, sessionManager, config, asyncEngine, dispatcher, 20));
        var snowArg = ArgumentType.Integer("radius");
        snow.addSyntax((sender, context) -> executeSnow(sender, sessionManager, config, asyncEngine, dispatcher, context.get(snowArg)), snowArg);
        commands.add(snow);

        Command thaw = new Command("//thaw", "/thaw", "thaw");
        thaw.setDefaultExecutor((sender, context) -> executeThaw(sender, sessionManager, config, asyncEngine, dispatcher, 20));
        var thawArg = ArgumentType.Integer("radius");
        thaw.addSyntax((sender, context) -> executeThaw(sender, sessionManager, config, asyncEngine, dispatcher, context.get(thawArg)), thawArg);
        commands.add(thaw);

        // 5. //green
        Command green = new Command("//green", "/green", "green");
        green.setDefaultExecutor((sender, context) -> executeGreen(sender, sessionManager, config, asyncEngine, dispatcher, 20));
        var greenArg = ArgumentType.Integer("radius");
        green.addSyntax((sender, context) -> executeGreen(sender, sessionManager, config, asyncEngine, dispatcher, context.get(greenArg)), greenArg);
        commands.add(green);

        // 6. //butcher [radius]
        Command butcher = new Command("//butcher", "/butcher", "butcher");
        butcher.setDefaultExecutor((sender, context) -> executeButcher(sender, -1));
        var butcherArg = ArgumentType.Integer("radius");
        butcher.addSyntax((sender, context) -> executeButcher(sender, context.get(butcherArg)), butcherArg);
        commands.add(butcher);

        // 7. //remove <type> [radius]
        Command remove = new Command("//remove", "/remove", "remove");
        remove.setDefaultExecutor((sender, context) -> CommandHelper.sendError(sender, "Usage: //remove <items|arrows|mobs|all> [radius]"));
        var remTypeArg = ArgumentType.Word("type").from("items", "arrows", "mobs", "all");
        var remRadiusArg = ArgumentType.Integer("radius");
        remove.addSyntax((sender, context) -> executeRemove(sender, context.get(remTypeArg), -1), remTypeArg);
        remove.addSyntax((sender, context) -> executeRemove(sender, context.get(remTypeArg), context.get(remRadiusArg)), remTypeArg, remRadiusArg);
        commands.add(remove);

        return commands;
    }

    private static void executeDrain(Object sender, SessionManager sessionManager, MawConfig config, MawAsyncEngine asyncEngine, TickDispatcher dispatcher, int radius) {
        if (!(sender instanceof Player player)) return;
        Instance instance = player.getInstance();
        if (instance == null) return;

        PlayerSession session = sessionManager.getSession(player);
        int cx = player.getPosition().blockX();
        int cy = player.getPosition().blockY();
        int cz = player.getPosition().blockZ();

        Point min = new Vec(cx - radius, cy - radius, cz - radius);
        Point max = new Vec(cx + radius, cy + radius, cz + radius);
        CuboidSelection sel = new CuboidSelection(min, max);

        asyncEngine.runAsync(() -> {
            try {
                AsyncEditSession editSession = CommandHelper.newSession(config, player, instance, config.maxBlocksPerOperation(), sel);
                double rSq = (radius + 0.5) * (radius + 0.5);
                long drained = 0;

                for (Point p : sel) {
                    double dx = p.x() - cx;
                    double dy = p.y() - cy;
                    double dz = p.z() - cz;
                    if (dx * dx + dy * dy + dz * dz <= rSq) {
                        Block b = editSession.getBlock(p.blockX(), p.blockY(), p.blockZ());
                        if (b.compare(Block.WATER) || b.compare(Block.LAVA)) {
                            editSession.setBlock(p.blockX(), p.blockY(), p.blockZ(), Block.AIR);
                            drained++;
                        } else if ("true".equalsIgnoreCase(b.getProperty("waterlogged"))) {
                            editSession.setBlock(p.blockX(), p.blockY(), p.blockZ(), b.withProperty("waterlogged", "false"));
                            drained++;
                        }
                    }
                }

                long finalDrained = drained;
                editSession.commit(dispatcher, session.isUpdatePhysics(), session.isManageEntities()).thenAccept(res -> {
                    session.getHistoryManager().record(editSession.createChangeSet());
                    CommandHelper.sendSuccess(player, String.format("Drained %,d fluid blocks.", finalDrained));
                });
            } catch (Exception e) {
                CommandHelper.sendError(player, "Drain error: " + e.getMessage());
            }
        });
    }

    private static void executeFixFluid(Object sender, SessionManager sessionManager, MawConfig config, MawAsyncEngine asyncEngine, TickDispatcher dispatcher, int radius, boolean isWater) {
        if (!(sender instanceof Player player)) return;
        Instance instance = player.getInstance();
        if (instance == null) return;

        PlayerSession session = sessionManager.getSession(player);
        int cx = player.getPosition().blockX();
        int cy = player.getPosition().blockY();
        int cz = player.getPosition().blockZ();

        Point min = new Vec(cx - radius, cy - radius, cz - radius);
        Point max = new Vec(cx + radius, cy + radius, cz + radius);
        CuboidSelection sel = new CuboidSelection(min, max);
        Block targetFluid = isWater ? Block.WATER : Block.LAVA;

        asyncEngine.runAsync(() -> {
            try {
                AsyncEditSession editSession = CommandHelper.newSession(config, player, instance, config.maxBlocksPerOperation(), sel);
                double rSq = (radius + 0.5) * (radius + 0.5);
                long count = 0;

                for (Point p : sel) {
                    double dx = p.x() - cx;
                    double dy = p.y() - cy;
                    double dz = p.z() - cz;
                    if (dx * dx + dy * dy + dz * dz <= rSq) {
                        Block b = editSession.getBlock(p.blockX(), p.blockY(), p.blockZ());
                        if (b.name().contains(isWater ? "water" : "lava")) {
                            editSession.setBlock(p.blockX(), p.blockY(), p.blockZ(), targetFluid);
                            count++;
                        }
                    }
                }

                long finalCount = count;
                editSession.commit(dispatcher, session.isUpdatePhysics(), session.isManageEntities()).thenAccept(res -> {
                    session.getHistoryManager().record(editSession.createChangeSet());
                    CommandHelper.sendSuccess(player, String.format("Fixed %,d fluid blocks.", finalCount));
                });
            } catch (Exception e) {
                CommandHelper.sendError(player, "Fixfluid error: " + e.getMessage());
            }
        });
    }

    private static void executeExtinguish(Object sender, SessionManager sessionManager, MawConfig config, MawAsyncEngine asyncEngine, TickDispatcher dispatcher, int radius) {
        if (!(sender instanceof Player player)) return;
        Instance instance = player.getInstance();
        if (instance == null) return;

        PlayerSession session = sessionManager.getSession(player);
        int cx = player.getPosition().blockX();
        int cy = player.getPosition().blockY();
        int cz = player.getPosition().blockZ();

        Point min = new Vec(cx - radius, cy - radius, cz - radius);
        Point max = new Vec(cx + radius, cy + radius, cz + radius);
        CuboidSelection sel = new CuboidSelection(min, max);

        asyncEngine.runAsync(() -> {
            try {
                AsyncEditSession editSession = CommandHelper.newSession(config, player, instance, config.maxBlocksPerOperation(), sel);
                double rSq = (radius + 0.5) * (radius + 0.5);
                long count = 0;

                for (Point p : sel) {
                    double dx = p.x() - cx;
                    double dy = p.y() - cy;
                    double dz = p.z() - cz;
                    if (dx * dx + dy * dy + dz * dz <= rSq) {
                        Block b = editSession.getBlock(p.blockX(), p.blockY(), p.blockZ());
                        if (b.compare(Block.FIRE) || b.compare(Block.SOUL_FIRE)) {
                            editSession.setBlock(p.blockX(), p.blockY(), p.blockZ(), Block.AIR);
                            count++;
                        }
                    }
                }

                long finalCount = count;
                editSession.commit(dispatcher, session.isUpdatePhysics(), session.isManageEntities()).thenAccept(res -> {
                    session.getHistoryManager().record(editSession.createChangeSet());
                    CommandHelper.sendSuccess(player, String.format("Extinguished %,d fire blocks.", finalCount));
                });
            } catch (Exception e) {
                CommandHelper.sendError(player, "Extinguish error: " + e.getMessage());
            }
        });
    }

    private static void executeSnow(Object sender, SessionManager sessionManager, MawConfig config, MawAsyncEngine asyncEngine, TickDispatcher dispatcher, int radius) {
        if (!(sender instanceof Player player)) return;
        Instance instance = player.getInstance();
        if (instance == null) return;

        PlayerSession session = sessionManager.getSession(player);
        int cx = player.getPosition().blockX();
        int cy = player.getPosition().blockY();
        int cz = player.getPosition().blockZ();

        Point min = new Vec(cx - radius, Math.max(-64, cy - 30), cz - radius);
        Point max = new Vec(cx + radius, Math.min(319, cy + 30), cz + radius);
        CuboidSelection sel = new CuboidSelection(min, max);

        asyncEngine.runAsync(() -> {
            try {
                AsyncEditSession editSession = CommandHelper.newSession(config, player, instance, config.maxBlocksPerOperation(), sel);
                double rSq = (radius + 0.5) * (radius + 0.5);
                long count = 0;

                for (int x = cx - radius; x <= cx + radius; x++) {
                    for (int z = cz - radius; z <= cz + radius; z++) {
                        if ((x - cx) * (x - cx) + (z - cz) * (z - cz) > rSq) continue;

                        for (int y = max.blockY(); y >= min.blockY(); y--) {
                            Block b = editSession.getBlock(x, y, z);
                            if (b != null && !b.air()) {
                                if (b.compare(Block.WATER)) {
                                    editSession.setBlock(x, y, z, Block.ICE);
                                    count++;
                                } else if (editSession.getBlock(x, y + 1, z).air()) {
                                    editSession.setBlock(x, y + 1, z, Block.SNOW);
                                    count++;
                                }
                                break;
                            }
                        }
                    }
                }

                long finalCount = count;
                editSession.commit(dispatcher, session.isUpdatePhysics(), session.isManageEntities()).thenAccept(res -> {
                    session.getHistoryManager().record(editSession.createChangeSet());
                    CommandHelper.sendSuccess(player, String.format("Placed snow on %,d blocks.", finalCount));
                });
            } catch (Exception e) {
                CommandHelper.sendError(player, "Snow error: " + e.getMessage());
            }
        });
    }

    private static void executeThaw(Object sender, SessionManager sessionManager, MawConfig config, MawAsyncEngine asyncEngine, TickDispatcher dispatcher, int radius) {
        if (!(sender instanceof Player player)) return;
        Instance instance = player.getInstance();
        if (instance == null) return;

        PlayerSession session = sessionManager.getSession(player);
        int cx = player.getPosition().blockX();
        int cy = player.getPosition().blockY();
        int cz = player.getPosition().blockZ();

        Point min = new Vec(cx - radius, cy - radius, cz - radius);
        Point max = new Vec(cx + radius, cy + radius, cz + radius);
        CuboidSelection sel = new CuboidSelection(min, max);

        asyncEngine.runAsync(() -> {
            try {
                AsyncEditSession editSession = CommandHelper.newSession(config, player, instance, config.maxBlocksPerOperation(), sel);
                double rSq = (radius + 0.5) * (radius + 0.5);
                long count = 0;

                for (Point p : sel) {
                    double dx = p.x() - cx;
                    double dy = p.y() - cy;
                    double dz = p.z() - cz;
                    if (dx * dx + dy * dy + dz * dz <= rSq) {
                        Block b = editSession.getBlock(p.blockX(), p.blockY(), p.blockZ());
                        if (b.compare(Block.SNOW) || b.compare(Block.SNOW_BLOCK)) {
                            editSession.setBlock(p.blockX(), p.blockY(), p.blockZ(), Block.AIR);
                            count++;
                        } else if (b.compare(Block.ICE)) {
                            editSession.setBlock(p.blockX(), p.blockY(), p.blockZ(), Block.WATER);
                            count++;
                        }
                    }
                }

                long finalCount = count;
                editSession.commit(dispatcher, session.isUpdatePhysics(), session.isManageEntities()).thenAccept(res -> {
                    session.getHistoryManager().record(editSession.createChangeSet());
                    CommandHelper.sendSuccess(player, String.format("Thawed %,d ice and snow blocks.", finalCount));
                });
            } catch (Exception e) {
                CommandHelper.sendError(player, "Thaw error: " + e.getMessage());
            }
        });
    }

    private static void executeGreen(Object sender, SessionManager sessionManager, MawConfig config, MawAsyncEngine asyncEngine, TickDispatcher dispatcher, int radius) {
        if (!(sender instanceof Player player)) return;
        Instance instance = player.getInstance();
        if (instance == null) return;

        PlayerSession session = sessionManager.getSession(player);
        int cx = player.getPosition().blockX();
        int cy = player.getPosition().blockY();
        int cz = player.getPosition().blockZ();

        Point min = new Vec(cx - radius, cy - radius, cz - radius);
        Point max = new Vec(cx + radius, cy + radius, cz + radius);
        CuboidSelection sel = new CuboidSelection(min, max);

        asyncEngine.runAsync(() -> {
            try {
                AsyncEditSession editSession = CommandHelper.newSession(config, player, instance, config.maxBlocksPerOperation(), sel);
                double rSq = (radius + 0.5) * (radius + 0.5);
                long count = 0;

                for (Point p : sel) {
                    double dx = p.x() - cx;
                    double dy = p.y() - cy;
                    double dz = p.z() - cz;
                    if (dx * dx + dy * dy + dz * dz <= rSq) {
                        Block b = editSession.getBlock(p.blockX(), p.blockY(), p.blockZ());
                        if (b.compare(Block.DIRT) || b.compare(Block.COARSE_DIRT)) {
                            Block above = editSession.getBlock(p.blockX(), p.blockY() + 1, p.blockZ());
                            if (above.air()) {
                                editSession.setBlock(p.blockX(), p.blockY(), p.blockZ(), Block.GRASS_BLOCK);
                                count++;
                            }
                        }
                    }
                }

                long finalCount = count;
                editSession.commit(dispatcher, session.isUpdatePhysics(), session.isManageEntities()).thenAccept(res -> {
                    session.getHistoryManager().record(editSession.createChangeSet());
                    CommandHelper.sendSuccess(player, String.format("Greened %,d dirt blocks.", finalCount));
                });
            } catch (Exception e) {
                CommandHelper.sendError(player, "Green error: " + e.getMessage());
            }
        });
    }

    private static void executeButcher(Object sender, int radius) {
        if (!(sender instanceof Player player)) return;
        Instance instance = player.getInstance();
        if (instance == null) return;

        double rSq = radius > 0 ? (radius * radius) : Double.MAX_VALUE;
        Point pPos = player.getPosition();

        int removed = 0;
        for (Entity e : instance.getEntities()) {
            if (e instanceof Player) continue;
            if (radius > 0 && e.getPosition().distanceSquared(pPos) > rSq) continue;
            e.remove();
            removed++;
        }

        CommandHelper.sendSuccess(player, String.format("Butchered %,d entities.", removed));
    }

    private static void executeRemove(Object sender, String type, int radius) {
        if (!(sender instanceof Player player)) return;
        Instance instance = player.getInstance();
        if (instance == null) return;

        double rSq = radius > 0 ? (radius * radius) : Double.MAX_VALUE;
        Point pPos = player.getPosition();

        int removed = 0;
        for (Entity e : instance.getEntities()) {
            if (e instanceof Player) continue;
            if (radius > 0 && e.getPosition().distanceSquared(pPos) > rSq) continue;

            boolean match = switch (type.toLowerCase(Locale.ROOT)) {
                case "items" -> e instanceof ItemEntity;
                case "arrows" -> e.getEntityType().name().contains("arrow");
                case "mobs" -> !(e instanceof ItemEntity);
                case "all" -> true;
                default -> false;
            };

            if (match) {
                e.remove();
                removed++;
            }
        }

        CommandHelper.sendSuccess(player, String.format("Removed %,d %s entities.", removed, type));
    }
}
