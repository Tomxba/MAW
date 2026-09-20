package fr.maw.guard;

import net.minestom.server.entity.Player;
import net.minestom.server.event.EventFilter;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.player.PlayerCommandEvent;

import java.util.HashSet;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

/**
 * Stops a MAW command that the {@link EditGuard} refuses, and tells the player why. It listens to the
 * commands players type, before Minestom runs them.
 */
public final class CommandGuardListener {

    private final EditGuard guard;
    private final Set<String> commandNames = new HashSet<>();

    /**
     * @param commandNames the names and aliases of MAW's commands, as registered (case is ignored)
     */
    public CommandGuardListener(EditGuard guard, Iterable<String> commandNames) {
        this.guard = Objects.requireNonNull(guard, "guard cannot be null");
        for (String name : commandNames) {
            this.commandNames.add(name.toLowerCase(Locale.ROOT));
        }
    }

    /** Whether the command line typed by a player, without its first slash, is one of MAW's commands. */
    public boolean isMawCommand(String commandLine) {
        if (commandLine == null) {
            return false;
        }
        String line = commandLine.stripLeading();
        int space = line.indexOf(' ');
        String name = (space < 0 ? line : line.substring(0, space)).toLowerCase(Locale.ROOT);
        return commandNames.contains(name);
    }

    public void register(EventNode<?> parentNode) {
        EventNode<net.minestom.server.event.trait.PlayerEvent> node = EventNode.type("maw-guard-node", EventFilter.PLAYER);
        node.addListener(PlayerCommandEvent.class, this::onCommand);

        @SuppressWarnings({"rawtypes", "unchecked"})
        EventNode rawNode = parentNode;
        rawNode.addChild(node);
    }

    void onCommand(PlayerCommandEvent event) {
        if (!isMawCommand(event.getCommand())) {
            return;
        }
        Player player = event.getPlayer();
        if (!guard.canEdit(player, player.getInstance())) {
            event.setCancelled(true);
            player.sendMessage(guard.denialMessage(player, player.getInstance()));
        }
    }
}
