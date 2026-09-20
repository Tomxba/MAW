package fr.maw.command;

import fr.maw.clipboard.Transform.Direction;
import fr.maw.selection.CuboidSelection;
import fr.maw.selection.Selection;
import fr.maw.selection.SelectionHelper;
import fr.maw.session.PlayerSession;
import fr.maw.session.SessionManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;

import java.util.*;

public final class SelectionModifyCommands {

    private SelectionModifyCommands() {}

    public static List<Command> create(SessionManager sessionManager) {
        List<Command> commands = new ArrayList<>();

        // 1. //expand
        Command expand = new Command("//expand", "/expand", "expand");
        expand.setDefaultExecutor((sender, context) -> CommandHelper.sendError(sender, "Usage: //expand <amount|vert> [reverseAmount] [direction]"));
        var expandArgs = ArgumentType.StringArray("args");
        expand.addSyntax((sender, context) -> {
            if (!(sender instanceof Player player)) return;
            PlayerSession session = sessionManager.getSession(player);
            if (!session.isSelectionComplete()) {
                CommandHelper.sendError(player, "Make a selection first.");
                return;
            }

            String[] args = context.get(expandArgs);
            if (args.length == 0) return;

            CuboidSelection current = (CuboidSelection) session.getSelection();

            if (args[0].equalsIgnoreCase("vert")) {
                CuboidSelection expanded = SelectionHelper.expandVert(current, -64, 319);
                session.setPos1(new Vec(expanded.getMinX(), expanded.getMinY(), expanded.getMinZ()));
                session.setPos2(new Vec(expanded.getMaxX(), expanded.getMaxY(), expanded.getMaxZ()));
                CommandHelper.sendSuccess(player, String.format("Region expanded vertically (%,d blocks).", expanded.getVolume()));
                return;
            }

            int amount = Integer.parseInt(args[0]);
            int reverse = 0;
            Direction dir = SelectionHelper.getPlayerDirection(player);

            if (args.length == 2) {
                if (args[1].matches("\\d+")) {
                    reverse = Integer.parseInt(args[1]);
                } else {
                    dir = SelectionHelper.parseDirection(args[1], dir);
                }
            } else if (args.length >= 3) {
                if (args[1].matches("\\d+")) reverse = Integer.parseInt(args[1]);
                dir = SelectionHelper.parseDirection(args[2], dir);
            }

            CuboidSelection expanded = SelectionHelper.expand(current, amount, reverse, dir);
            session.setPos1(new Vec(expanded.getMinX(), expanded.getMinY(), expanded.getMinZ()));
            session.setPos2(new Vec(expanded.getMaxX(), expanded.getMaxY(), expanded.getMaxZ()));
            CommandHelper.sendSuccess(player, String.format("Region expanded by %d %s (%,d blocks).", amount, dir.name().toLowerCase(), expanded.getVolume()));
        }, expandArgs);
        commands.add(expand);

        // 2. //contract
        Command contract = new Command("//contract", "/contract", "contract");
        contract.setDefaultExecutor((sender, context) -> CommandHelper.sendError(sender, "Usage: //contract <amount> [reverseAmount] [direction]"));
        var contractArgs = ArgumentType.StringArray("args");
        contract.addSyntax((sender, context) -> {
            if (!(sender instanceof Player player)) return;
            PlayerSession session = sessionManager.getSession(player);
            if (!session.isSelectionComplete()) {
                CommandHelper.sendError(player, "Make a selection first.");
                return;
            }

            String[] args = context.get(contractArgs);
            if (args.length == 0) return;

            CuboidSelection current = (CuboidSelection) session.getSelection();
            int amount = Integer.parseInt(args[0]);
            int reverse = 0;
            Direction dir = SelectionHelper.getPlayerDirection(player);

            if (args.length == 2) {
                if (args[1].matches("\\d+")) {
                    reverse = Integer.parseInt(args[1]);
                } else {
                    dir = SelectionHelper.parseDirection(args[1], dir);
                }
            } else if (args.length >= 3) {
                if (args[1].matches("\\d+")) reverse = Integer.parseInt(args[1]);
                dir = SelectionHelper.parseDirection(args[2], dir);
            }

            CuboidSelection contracted = SelectionHelper.contract(current, amount, reverse, dir);
            session.setPos1(new Vec(contracted.getMinX(), contracted.getMinY(), contracted.getMinZ()));
            session.setPos2(new Vec(contracted.getMaxX(), contracted.getMaxY(), contracted.getMaxZ()));
            CommandHelper.sendSuccess(player, String.format("Region contracted by %d %s (%,d blocks).", amount, dir.name().toLowerCase(), contracted.getVolume()));
        }, contractArgs);
        commands.add(contract);

        // 3. //shift
        Command shift = new Command("//shift", "/shift", "shift");
        shift.setDefaultExecutor((sender, context) -> CommandHelper.sendError(sender, "Usage: //shift <amount> [direction]"));
        var shiftArgs = ArgumentType.StringArray("args");
        shift.addSyntax((sender, context) -> {
            if (!(sender instanceof Player player)) return;
            PlayerSession session = sessionManager.getSession(player);
            if (!session.isSelectionComplete()) {
                CommandHelper.sendError(player, "Make a selection first.");
                return;
            }

            String[] args = context.get(shiftArgs);
            if (args.length == 0) return;

            int amount = Integer.parseInt(args[0]);
            Direction dir = SelectionHelper.getPlayerDirection(player);
            if (args.length > 1) {
                dir = SelectionHelper.parseDirection(args[1], dir);
            }

            CuboidSelection current = (CuboidSelection) session.getSelection();
            CuboidSelection shifted = SelectionHelper.shift(current, amount, dir);
            session.setPos1(new Vec(shifted.getMinX(), shifted.getMinY(), shifted.getMinZ()));
            session.setPos2(new Vec(shifted.getMaxX(), shifted.getMaxY(), shifted.getMaxZ()));
            CommandHelper.sendSuccess(player, String.format("Selection shifted %d blocks %s.", amount, dir.name().toLowerCase()));
        }, shiftArgs);
        commands.add(shift);

        // 4. //inset
        Command inset = new Command("//inset", "/inset", "inset");
        inset.setDefaultExecutor((sender, context) -> CommandHelper.sendError(sender, "Usage: //inset <amount>"));
        var insetArg = ArgumentType.Integer("amount");
        inset.addSyntax((sender, context) -> {
            if (!(sender instanceof Player player)) return;
            PlayerSession session = sessionManager.getSession(player);
            if (!session.isSelectionComplete()) {
                CommandHelper.sendError(player, "Make a selection first.");
                return;
            }
            int amount = context.get(insetArg);
            CuboidSelection current = (CuboidSelection) session.getSelection();
            CuboidSelection in = SelectionHelper.inset(current, amount);
            session.setPos1(new Vec(in.getMinX(), in.getMinY(), in.getMinZ()));
            session.setPos2(new Vec(in.getMaxX(), in.getMaxY(), in.getMaxZ()));
            CommandHelper.sendSuccess(player, String.format("Selection inset by %d (%,d blocks).", amount, in.getVolume()));
        }, insetArg);
        commands.add(inset);

        // 5. //outset
        Command outset = new Command("//outset", "/outset", "outset");
        outset.setDefaultExecutor((sender, context) -> CommandHelper.sendError(sender, "Usage: //outset <amount>"));
        var outsetArg = ArgumentType.Integer("amount");
        outset.addSyntax((sender, context) -> {
            if (!(sender instanceof Player player)) return;
            PlayerSession session = sessionManager.getSession(player);
            if (!session.isSelectionComplete()) {
                CommandHelper.sendError(player, "Make a selection first.");
                return;
            }
            int amount = context.get(outsetArg);
            CuboidSelection current = (CuboidSelection) session.getSelection();
            CuboidSelection out = SelectionHelper.outset(current, amount);
            session.setPos1(new Vec(out.getMinX(), out.getMinY(), out.getMinZ()));
            session.setPos2(new Vec(out.getMaxX(), out.getMaxY(), out.getMaxZ()));
            CommandHelper.sendSuccess(player, String.format("Selection outset by %d (%,d blocks).", amount, out.getVolume()));
        }, outsetArg);
        commands.add(outset);

        // 6. //chunk
        Command chunk = new Command("//chunk", "/chunk", "chunk");
        chunk.setDefaultExecutor((sender, context) -> {
            if (!(sender instanceof Player player)) return;
            PlayerSession session = sessionManager.getSession(player);
            int cx = player.getPosition().blockX() >> 4;
            int cz = player.getPosition().blockZ() >> 4;

            int minX = cx << 4;
            int maxX = minX + 15;
            int minZ = cz << 4;
            int maxZ = minZ + 15;

            session.setPos1(new Vec(minX, -64, minZ));
            session.setPos2(new Vec(maxX, 319, maxZ));
            CommandHelper.sendSuccess(player, String.format("Chunk selected: (%d, %d) [%,d blocks].", cx, cz, session.getSelection().getVolume()));
        });
        commands.add(chunk);

        // 7. //desel (or //deselect)
        Command desel = new Command("//desel", "/desel", "desel", "//deselect", "/deselect", "deselect");
        desel.setDefaultExecutor((sender, context) -> {
            if (!(sender instanceof Player player)) return;
            PlayerSession session = sessionManager.getSession(player);
            session.setPos1(null);
            session.setPos2(null);
            CommandHelper.sendSuccess(player, "Selection cleared.");
        });
        commands.add(desel);

        // 8. //distr
        Command distr = new Command("//distr", "/distr", "distr");
        distr.setDefaultExecutor((sender, context) -> {
            if (!(sender instanceof Player player)) return;
            Instance instance = player.getInstance();
            if (instance == null) return;

            PlayerSession session = sessionManager.getSession(player);
            if (!session.isSelectionComplete()) {
                CommandHelper.sendError(player, "Make a selection first.");
                return;
            }

            Selection sel = session.getSelection();
            Map<String, Long> counts = new HashMap<>();
            long total = 0;

            for (Point p : sel) {
                Block b = instance.getBlock(p.blockX(), p.blockY(), p.blockZ());
                String name = b.name();
                counts.put(name, counts.getOrDefault(name, 0L) + 1);
                total++;
            }

            List<Map.Entry<String, Long>> sorted = new ArrayList<>(counts.entrySet());
            sorted.sort((a, b) -> Long.compare(b.getValue(), a.getValue()));

            player.sendMessage(Component.text(String.format("--- Block Distribution (%,d blocks) ---", total), NamedTextColor.GOLD));
            for (var entry : sorted) {
                double pct = (double) entry.getValue() * 100.0 / total;
                player.sendMessage(Component.text(String.format("• %s: %,d (%.2f%%)", entry.getKey(), entry.getValue(), pct), NamedTextColor.YELLOW));
            }
        });
        commands.add(distr);

        return commands;
    }
}
