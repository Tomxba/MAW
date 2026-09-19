package fr.maw.command;

import fr.maw.pattern.Mask;
import fr.maw.pattern.PatternParser;
import fr.maw.selection.Selection;
import fr.maw.session.PlayerSession;
import fr.maw.session.SessionManager;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;

import java.util.List;

public final class InfoCommands {

    private InfoCommands() {}

    public static List<Command> create(SessionManager sessionManager) {
        Command size = new Command("//size", "/size", "size");
        size.setDefaultExecutor((sender, context) -> {
            if (!(sender instanceof Player player)) return;
            PlayerSession playerSession = sessionManager.getSession(player);
            if (!playerSession.isSelectionComplete()) {
                CommandHelper.sendError(player, "Make a selection first.");
                return;
            }

            Selection sel = playerSession.getSelection();
            CommandHelper.sendInfo(player, String.format(
                    "Selection: %dx%dx%d (%s blocks) [min: (%d, %d, %d), max: (%d, %d, %d)]",
                    sel.getWidthX(), sel.getHeightY(), sel.getLengthZ(),
                    String.format("%,d", sel.getVolume()),
                    sel.getMinX(), sel.getMinY(), sel.getMinZ(),
                    sel.getMaxX(), sel.getMaxY(), sel.getMaxZ()
            ));
        });

        Command count = new Command("//count", "/count", "count");
        count.setDefaultExecutor((sender, context) -> CommandHelper.sendError(sender, "Usage: //count <block/mask>"));
        var maskArg = ArgumentType.String("mask");
        count.addSyntax((sender, context) -> {
            if (!(sender instanceof Player player)) return;
            Instance instance = player.getInstance();
            if (instance == null) return;

            PlayerSession playerSession = sessionManager.getSession(player);
            if (!playerSession.isSelectionComplete()) {
                CommandHelper.sendError(player, "Make a selection first.");
                return;
            }

            String maskStr = context.get(maskArg);
            Mask mask;
            try {
                mask = PatternParser.parseMask(maskStr);
            } catch (Exception e) {
                CommandHelper.sendError(player, "Invalid mask: " + e.getMessage());
                return;
            }

            Selection sel = playerSession.getSelection();
            long matchCount = 0;
            for (Point p : sel) {
                Block b = instance.getBlock(p.blockX(), p.blockY(), p.blockZ());
                if (mask.test(p.blockX(), p.blockY(), p.blockZ(), b)) {
                    matchCount++;
                }
            }

            CommandHelper.sendSuccess(player, String.format("Found %,d matching blocks in selection.", matchCount));
        }, maskArg);

        return List.of(size, count);
    }
}
