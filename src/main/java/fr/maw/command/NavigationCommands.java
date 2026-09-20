package fr.maw.command;

import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;

import java.util.ArrayList;
import java.util.List;

public final class NavigationCommands {

    private NavigationCommands() {}

    public static List<Command> create() {
        List<Command> commands = new ArrayList<>();

        // 1. //up <height>
        Command up = new Command("//up", "/up", "up");
        up.setDefaultExecutor((sender, context) -> CommandHelper.sendError(sender, "Usage: //up <height>"));
        var upHeightArg = ArgumentType.Integer("height");
        up.addSyntax((sender, context) -> {
            if (!(sender instanceof Player player)) return;
            Instance instance = player.getInstance();
            if (instance == null) return;

            int h = context.get(upHeightArg);
            int newY = player.getPosition().blockY() + h;
            int px = player.getPosition().blockX();
            int pz = player.getPosition().blockZ();

            instance.setBlock(px, newY - 1, pz, Block.GLASS);
            player.teleport(new Pos(px + 0.5, newY, pz + 0.5, player.getPosition().yaw(), player.getPosition().pitch()));
            CommandHelper.sendSuccess(player, "Moved up " + h + " blocks (glass placed beneath feet).");
        }, upHeightArg);
        commands.add(up);

        // 2. //ceil [clearance]
        Command ceil = new Command("//ceil", "/ceil", "ceil");
        ceil.setDefaultExecutor((sender, context) -> executeCeil(sender, 0));
        var clearArg = ArgumentType.Integer("clearance");
        ceil.addSyntax((sender, context) -> executeCeil(sender, context.get(clearArg)), clearArg);
        commands.add(ceil);

        // 3. //jumpto (or /j)
        Command jumpto = new Command("//jumpto", "/jumpto", "jumpto", "/j", "j");
        jumpto.setDefaultExecutor((sender, context) -> {
            if (!(sender instanceof Player player)) return;
            Point target = CommandHelper.getTargetBlock(player, 200);
            if (target == null) {
                CommandHelper.sendError(player, "No block in sight within 200 blocks.");
                return;
            }

            Pos newPos = new Pos(target.blockX() + 0.5, target.blockY() + 1, target.blockZ() + 0.5, player.getPosition().yaw(), player.getPosition().pitch());
            player.teleport(newPos);
            CommandHelper.sendSuccess(player, "Poof!");
        });
        commands.add(jumpto);

        // 4. //thru
        Command thru = new Command("//thru", "/thru", "thru");
        thru.setDefaultExecutor((sender, context) -> {
            if (!(sender instanceof Player player)) return;
            Instance instance = player.getInstance();
            if (instance == null) return;

            Vec dir = player.getPosition().direction().normalize();
            Point eye = player.getPosition().add(0, player.getEyeHeight(), 0);

            // Step forward through wall
            double maxDist = 25.0;
            boolean hitWall = false;
            Point target = null;

            for (double d = 0.5; d <= maxDist; d += 0.5) {
                Point p = eye.add(dir.mul(d));
                int bx = p.blockX();
                int by = p.blockY();
                int bz = p.blockZ();

                Block b = instance.getBlock(bx, by, bz);
                if (b != null && !b.isAir()) {
                    hitWall = true;
                } else if (hitWall) {
                    // Check if block below is air or floor
                    Block bAbove = instance.getBlock(bx, by + 1, bz);
                    if (bAbove.isAir()) {
                        target = new Vec(bx + 0.5, by, bz + 0.5);
                        break;
                    }
                }
            }

            if (target != null) {
                player.teleport(new Pos(target.x(), target.y(), target.z(), player.getPosition().yaw(), player.getPosition().pitch()));
                CommandHelper.sendSuccess(player, "Whoosh!");
            } else {
                CommandHelper.sendError(player, "No open space found on the other side of the wall.");
            }
        });
        commands.add(thru);

        // 5. //unstuck
        Command unstuck = new Command("//unstuck", "/unstuck", "unstuck");
        unstuck.setDefaultExecutor((sender, context) -> {
            if (!(sender instanceof Player player)) return;
            Instance instance = player.getInstance();
            if (instance == null) return;

            Point p = player.getPosition();
            int px = p.blockX();
            int py = p.blockY();
            int pz = p.blockZ();

            // Search upward for 2 air blocks
            int safeY = -1;
            for (int y = py; y <= Math.min(318, py + 50); y++) {
                if (instance.getBlock(px, y, pz).isAir() && instance.getBlock(px, y + 1, pz).isAir()) {
                    safeY = y;
                    break;
                }
            }

            if (safeY != -1) {
                player.teleport(new Pos(px + 0.5, safeY, pz + 0.5, player.getPosition().yaw(), player.getPosition().pitch()));
                CommandHelper.sendSuccess(player, "There you go!");
            } else {
                CommandHelper.sendError(player, "No free space above to get unstuck.");
            }
        });
        commands.add(unstuck);

        // 6. //ascend & //descend
        Command ascend = new Command("//ascend", "/ascend", "ascend");
        ascend.setDefaultExecutor((sender, context) -> executeAscend(sender, 1));
        var ascCountArg = ArgumentType.Integer("count");
        ascend.addSyntax((sender, context) -> executeAscend(sender, context.get(ascCountArg)), ascCountArg);
        commands.add(ascend);

        Command descend = new Command("//descend", "/descend", "descend");
        descend.setDefaultExecutor((sender, context) -> executeDescend(sender, 1));
        var descCountArg = ArgumentType.Integer("count");
        descend.addSyntax((sender, context) -> executeDescend(sender, context.get(descCountArg)), descCountArg);
        commands.add(descend);

        // 7. //top
        Command top = new Command("//top", "/top", "top");
        top.setDefaultExecutor((sender, context) -> {
            if (!(sender instanceof Player player)) return;
            Instance instance = player.getInstance();
            if (instance == null) return;

            int px = player.getPosition().blockX();
            int pz = player.getPosition().blockZ();

            for (int y = 319; y >= -64; y--) {
                Block b = instance.getBlock(px, y, pz);
                if (b != null && !b.isAir()) {
                    player.teleport(new Pos(px + 0.5, y + 1, pz + 0.5, player.getPosition().yaw(), player.getPosition().pitch()));
                    CommandHelper.sendSuccess(player, "Teleported to the top!");
                    return;
                }
            }
            CommandHelper.sendError(player, "No solid block found in column.");
        });
        commands.add(top);

        return commands;
    }

    private static void executeCeil(Object sender, int clearance) {
        if (!(sender instanceof Player player)) return;
        Instance instance = player.getInstance();
        if (instance == null) return;

        int px = player.getPosition().blockX();
        int py = player.getPosition().blockY();
        int pz = player.getPosition().blockZ();

        int ceilY = -1;
        for (int y = py + 2; y <= 319; y++) {
            Block b = instance.getBlock(px, y, pz);
            if (b != null && !b.isAir()) {
                ceilY = y;
                break;
            }
        }

        if (ceilY == -1) {
            CommandHelper.sendError(player, "No ceiling detected above.");
            return;
        }

        int targetY = ceilY - 2 - clearance;
        instance.setBlock(px, targetY - 1, pz, Block.GLASS);
        player.teleport(new Pos(px + 0.5, targetY, pz + 0.5, player.getPosition().yaw(), player.getPosition().pitch()));
        CommandHelper.sendSuccess(player, "Whoosh! (Glass platform placed beneath feet).");
    }

    private static void executeAscend(Object sender, int count) {
        if (!(sender instanceof Player player)) return;
        Instance instance = player.getInstance();
        if (instance == null) return;

        int px = player.getPosition().blockX();
        int py = player.getPosition().blockY();
        int pz = player.getPosition().blockZ();

        int levelsFound = 0;
        int targetY = -1;
        boolean throughSolid = false;

        for (int y = py + 2; y <= 318; y++) {
            Block b = instance.getBlock(px, y, pz);
            if (!b.isAir()) {
                throughSolid = true;
            } else if (throughSolid && instance.getBlock(px, y + 1, pz).isAir()) {
                levelsFound++;
                targetY = y;
                throughSolid = false;
                if (levelsFound == count) break;
            }
        }

        if (targetY != -1) {
            player.teleport(new Pos(px + 0.5, targetY, pz + 0.5, player.getPosition().yaw(), player.getPosition().pitch()));
            CommandHelper.sendSuccess(player, "Ascended " + levelsFound + " level(s).");
        } else {
            CommandHelper.sendError(player, "No floor found above.");
        }
    }

    private static void executeDescend(Object sender, int count) {
        if (!(sender instanceof Player player)) return;
        Instance instance = player.getInstance();
        if (instance == null) return;

        int px = player.getPosition().blockX();
        int py = player.getPosition().blockY();
        int pz = player.getPosition().blockZ();

        int levelsFound = 0;
        int targetY = -1;
        boolean throughSolid = false;

        for (int y = py - 1; y >= -63; y--) {
            Block b = instance.getBlock(px, y, pz);
            if (!b.isAir()) {
                throughSolid = true;
            } else if (throughSolid && instance.getBlock(px, y - 1, pz).isAir()) {
                levelsFound++;
                targetY = y - 1;
                throughSolid = false;
                if (levelsFound == count) break;
            }
        }

        if (targetY != -1) {
            player.teleport(new Pos(px + 0.5, targetY, pz + 0.5, player.getPosition().yaw(), player.getPosition().pitch()));
            CommandHelper.sendSuccess(player, "Descended " + levelsFound + " level(s).");
        } else {
            CommandHelper.sendError(player, "No floor found below.");
        }
    }
}
