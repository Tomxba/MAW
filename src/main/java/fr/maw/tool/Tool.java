package fr.maw.tool;

import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.BlockFace;

/**
 * Interface for interactive player tools bound to items.
 */
public interface Tool {

    /**
     * Executes the tool action.
     *
     * @param player the player using the tool
     * @param instance the instance world
     * @param targetBlock the target block position (or null if raycast hit nothing)
     * @param face the clicked face (or null if clicked in air)
     * @param clickType left or right click
     * @return true if the event was handled and should be cancelled
     */
    boolean execute(Player player, Instance instance, Point targetBlock, BlockFace face, HandClickType clickType);
}
