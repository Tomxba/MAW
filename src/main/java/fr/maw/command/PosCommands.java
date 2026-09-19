package fr.maw.command;

import fr.maw.session.PlayerSession;
import fr.maw.session.SessionManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Player;

import java.util.List;

public final class PosCommands {

    private PosCommands() {}

    public static List<Command> create(SessionManager sessionManager) {
        Command pos1 = new Command("//pos1", "/pos1", "pos1");
        pos1.setDefaultExecutor((sender, context) -> {
            if (!(sender instanceof Player player)) {
                CommandHelper.sendError(sender, "Only players can use this command.");
                return;
            }
            Point pos = new Vec(player.getPosition().blockX(), player.getPosition().blockY(), player.getPosition().blockZ());
            setPos1(player, sessionManager, pos);
        });
        var pos1Arg = ArgumentType.RelativeBlockPosition("coordinates");
        pos1.addSyntax((sender, context) -> {
            if (!(sender instanceof Player player)) return;
            Point pos = context.get(pos1Arg).from(player);
            setPos1(player, sessionManager, pos);
        }, pos1Arg);

        Command pos2 = new Command("//pos2", "/pos2", "pos2");
        pos2.setDefaultExecutor((sender, context) -> {
            if (!(sender instanceof Player player)) {
                CommandHelper.sendError(sender, "Only players can use this command.");
                return;
            }
            Point pos = new Vec(player.getPosition().blockX(), player.getPosition().blockY(), player.getPosition().blockZ());
            setPos2(player, sessionManager, pos);
        });
        var pos2Arg = ArgumentType.RelativeBlockPosition("coordinates");
        pos2.addSyntax((sender, context) -> {
            if (!(sender instanceof Player player)) return;
            Point pos = context.get(pos2Arg).from(player);
            setPos2(player, sessionManager, pos);
        }, pos2Arg);

        Command hpos1 = new Command("//hpos1", "/hpos1", "hpos1");
        hpos1.setDefaultExecutor((sender, context) -> {
            if (!(sender instanceof Player player)) {
                CommandHelper.sendError(sender, "Only players can use this command.");
                return;
            }
            Point target = CommandHelper.getTargetBlock(player, 100);
            if (target == null) {
                CommandHelper.sendError(player, "No block in sight within 100 blocks.");
                return;
            }
            setPos1(player, sessionManager, target);
        });

        Command hpos2 = new Command("//hpos2", "/hpos2", "hpos2");
        hpos2.setDefaultExecutor((sender, context) -> {
            if (!(sender instanceof Player player)) {
                CommandHelper.sendError(sender, "Only players can use this command.");
                return;
            }
            Point target = CommandHelper.getTargetBlock(player, 100);
            if (target == null) {
                CommandHelper.sendError(player, "No block in sight within 100 blocks.");
                return;
            }
            setPos2(player, sessionManager, target);
        });

        return List.of(pos1, pos2, hpos1, hpos2);
    }

    private static void setPos1(Player player, SessionManager sessionManager, Point pos) {
        PlayerSession session = sessionManager.getSession(player);
        session.setPos1(pos);

        String volumeInfo = session.isSelectionComplete()
                ? String.format(" (%,d blocks)", session.getSelection().getVolume())
                : "";

        player.sendMessage(Component.text(String.format("First position set to (%d, %d, %d)%s",
                pos.blockX(), pos.blockY(), pos.blockZ(), volumeInfo), NamedTextColor.LIGHT_PURPLE));
    }

    private static void setPos2(Player player, SessionManager sessionManager, Point pos) {
        PlayerSession session = sessionManager.getSession(player);
        session.setPos2(pos);

        String volumeInfo = session.isSelectionComplete()
                ? String.format(" (%,d blocks)", session.getSelection().getVolume())
                : "";

        player.sendMessage(Component.text(String.format("Second position set to (%d, %d, %d)%s",
                pos.blockX(), pos.blockY(), pos.blockZ(), volumeInfo), NamedTextColor.LIGHT_PURPLE));
    }
}
