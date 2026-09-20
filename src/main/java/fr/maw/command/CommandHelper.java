package fr.maw.command;

import fr.maw.MawConfig;
import fr.maw.async.AsyncEditSession;
import fr.maw.guard.EditGuard;
import fr.maw.selection.Selection;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.command.CommandSender;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;

/**
 * Shared utility methods for MAW WorldEdit commands.
 */
public final class CommandHelper {

    private CommandHelper() {}

    public static void sendError(CommandSender sender, String message) {
        sender.sendMessage(Component.text("❌ " + message, NamedTextColor.RED));
    }

    public static void sendSuccess(CommandSender sender, String message) {
        sender.sendMessage(Component.text("✔ " + message, NamedTextColor.GREEN));
    }

    public static void sendInfo(CommandSender sender, String message) {
        sender.sendMessage(Component.text("ℹ " + message, NamedTextColor.LIGHT_PURPLE));
    }

    /**
     * Raycasts from player's eye location up to maxDistance to find the targeted block.
     */
    public static Point getTargetBlock(Player player, double maxDistance) {
        Instance instance = player.getInstance();
        if (instance == null) return null;

        Point eyePos = player.getPosition().add(0, player.getEyeHeight(), 0);
        Vec direction = player.getPosition().direction().normalize();

        double step = 0.25;
        for (double d = 0; d <= maxDistance; d += step) {
            Point check = eyePos.add(direction.mul(d));
            int bx = check.blockX();
            int by = check.blockY();
            int bz = check.blockZ();

            Block b = instance.getBlock(bx, by, bz);
            if (b != null && !b.air()) {
                return new Vec(bx, by, bz);
            }
        }
        return null;
    }

    /**
     * Opens the edit session of a player's operation, under the rules of the {@link EditGuard} of the
     * configuration: a block it refuses is skipped, and the player is told how many once the operation ends.
     * With the default guard (allow everything) the session has no rule and costs nothing more.
     */
    public static AsyncEditSession newSession(MawConfig config, Player player, Instance instance, int maxBlocks, Selection selection) {
        AsyncEditSession session = new AsyncEditSession(instance, maxBlocks, selection);
        EditGuard guard = config.editGuard();
        if (guard != EditGuard.ALLOW_ALL) {
            session.setGate(
                    (x, y, z, block) -> guard.allowsChange(player, instance, x, y, z, block),
                    refused -> sendInfo(player, String.format("%,d block(s) were not changed: you cannot edit there or with these blocks.", refused)));
        }
        return session;
    }

    public static boolean hasFlag(String[] args, String flag) {
        for (String a : args) {
            if (a.equalsIgnoreCase(flag)) return true;
        }
        return false;
    }

    public static String cleanFlags(String input) {
        if (input == null) return "";
        return input.replaceAll("(?i)\\s*-(u|e|a|h)\\b", "").trim();
    }
}
