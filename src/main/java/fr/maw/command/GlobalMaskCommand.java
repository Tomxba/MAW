package fr.maw.command;

import fr.maw.pattern.Mask;
import fr.maw.pattern.PatternParser;
import fr.maw.session.PlayerSession;
import fr.maw.session.SessionManager;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.entity.Player;

import java.util.ArrayList;
import java.util.List;

public final class GlobalMaskCommand {

    private GlobalMaskCommand() {}

    public static List<Command> create(SessionManager sessionManager) {
        List<Command> commands = new ArrayList<>();

        // 1. //gmask [mask]
        Command gmask = new Command("//gmask", "/gmask", "gmask");
        gmask.setDefaultExecutor((sender, context) -> {
            if (!(sender instanceof Player player)) return;
            PlayerSession session = sessionManager.getSession(player);
            session.setGlobalMask(null);
            CommandHelper.sendSuccess(player, "Global mask disabled.");
        });

        var maskArg = ArgumentType.String("mask");
        gmask.addSyntax((sender, context) -> {
            if (!(sender instanceof Player player)) return;
            PlayerSession session = sessionManager.getSession(player);
            String raw = context.get(maskArg);
            try {
                Mask m = PatternParser.parseMask(raw);
                session.setGlobalMask(m);
                CommandHelper.sendSuccess(player, "Global mask set to: " + raw);
            } catch (Exception e) {
                CommandHelper.sendError(player, "Invalid mask: " + e.getMessage());
            }
        }, maskArg);
        commands.add(gmask);

        // 2. //fast
        Command fast = new Command("//fast", "/fast", "fast");
        fast.setDefaultExecutor((sender, context) -> {
            if (!(sender instanceof Player player)) return;
            PlayerSession session = sessionManager.getSession(player);
            boolean newMode = !session.isFastMode();
            session.setFastMode(newMode);
            if (newMode) {
                session.setUpdatePhysics(false);
                session.setManageEntities(false);
                CommandHelper.sendSuccess(player, "Fast mode ENABLED (physics and entity updates skipped).");
            } else {
                CommandHelper.sendSuccess(player, "Fast mode DISABLED.");
            }
        });
        commands.add(fast);

        return commands;
    }
}
