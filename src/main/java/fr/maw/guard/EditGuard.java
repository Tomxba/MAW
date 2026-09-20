package fr.maw.guard;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;

/**
 * Decides who may change what with MAW.
 * <p>
 * By default MAW lets every player use every command on any instance, and write any block anywhere:
 * that is {@link #ALLOW_ALL}. A server that opens MAW to players it does not fully trust (a shared
 * build server, for instance) plugs its own guard in with
 * {@link fr.maw.MawConfig.Builder#editGuard(EditGuard)}, to answer three questions:
 * <ul>
 *   <li>{@link #canEdit}: may this player use MAW's commands in this instance at all?</li>
 *   <li>{@link #allowsChange}: may this player write this block at this position? It confines an
 *       operation to a region, and keeps forbidden blocks out of a world.</li>
 * </ul>
 * A block the guard refuses is skipped, not written: the rest of the operation goes on, and the player
 * is told how many were refused.
 */
public interface EditGuard {

    /** The default: everything is allowed, as if there were no guard. */
    EditGuard ALLOW_ALL = new EditGuard() {
    };

    /**
     * Whether the player may use MAW's commands in {@code instance}. Called on the server thread, when
     * a MAW command is typed; a refused command is not run and {@link #denialMessage} is shown.
     */
    default boolean canEdit(Player player, Instance instance) {
        return true;
    }

    /** What the player is told when {@link #canEdit} refuses. */
    default Component denialMessage(Player player, Instance instance) {
        return Component.text("You cannot edit here.", NamedTextColor.RED);
    }

    /**
     * Whether the player may put {@code block} at ({@code x}, {@code y}, {@code z}) of {@code instance}.
     * Called for every block an operation writes, from worker threads and possibly millions of times:
     * it must be fast and thread-safe.
     */
    default boolean allowsChange(Player player, Instance instance, int x, int y, int z, Block block) {
        return true;
    }
}
