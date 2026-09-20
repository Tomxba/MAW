package fr.maw.listener;

import fr.maw.command.CommandHelper;
import fr.maw.session.PlayerSession;
import fr.maw.session.SessionManager;
import fr.maw.tool.HandClickType;
import fr.maw.tool.Tool;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Player;
import net.minestom.server.event.EventFilter;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.player.PlayerBlockBreakEvent;
import net.minestom.server.event.player.PlayerBlockInteractEvent;
import net.minestom.server.event.player.PlayerUseItemEvent;
import net.minestom.server.event.player.PlayerUseItemOnBlockEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.item.ItemStack;

import java.util.Objects;

/**
 * Event listener for player tool and brush interactions.
 */
public final class ToolListener {

    private final SessionManager sessionManager;

    public ToolListener(SessionManager sessionManager) {
        this.sessionManager = Objects.requireNonNull(sessionManager, "sessionManager cannot be null");
    }

    public void register(EventNode<?> parentNode) {
        var node = EventNode.type("maw-tool-node", EventFilter.PLAYER);

        // 1. Right click on block with tool
        node.addListener(PlayerUseItemOnBlockEvent.class, event -> {
            Player player = event.getPlayer();
            Instance instance = player.getInstance();
            if (instance == null) return;

            ItemStack item = event.getItemStack();
            if (item.isAir()) return;

            PlayerSession session = sessionManager.getSession(player);
            Tool tool = session.getTool(item.material().name());
            if (tool != null) {
                boolean handled = tool.execute(player, instance, event.getPosition(), event.getBlockFace(), HandClickType.RIGHT_CLICK);
                if (handled) {
                    // Handled tool interaction
                }
            }
        });

        // 2. Block interact fallback
        node.addListener(PlayerBlockInteractEvent.class, event -> {
            Player player = event.getPlayer();
            Instance instance = player.getInstance();
            if (instance == null) return;

            ItemStack item = player.getItemInMainHand();
            if (item.isAir()) return;

            PlayerSession session = sessionManager.getSession(player);
            Tool tool = session.getTool(item.material().name());
            if (tool != null) {
                event.setCancelled(true);
                tool.execute(player, instance, event.getBlockPosition(), event.getBlockFace(), HandClickType.RIGHT_CLICK);
            }
        });

        // 3. Right click in air with tool (raycast target)
        node.addListener(PlayerUseItemEvent.class, event -> {
            Player player = event.getPlayer();
            Instance instance = player.getInstance();
            if (instance == null) return;

            ItemStack item = event.getItemStack();
            if (item.isAir()) return;

            PlayerSession session = sessionManager.getSession(player);
            Tool tool = session.getTool(item.material().name());
            if (tool != null) {
                event.setCancelled(true);
                Point target = CommandHelper.getTargetBlock(player, session.getBrushRange());
                if (target != null) {
                    tool.execute(player, instance, target, null, HandClickType.RIGHT_CLICK);
                }
            }
        });

        // 4. Left click with tool
        node.addListener(PlayerBlockBreakEvent.class, event -> {
            Player player = event.getPlayer();
            Instance instance = player.getInstance();
            if (instance == null) return;

            ItemStack item = player.getItemInMainHand();
            if (item.isAir()) return;

            PlayerSession session = sessionManager.getSession(player);
            Tool tool = session.getTool(item.material().name());
            if (tool != null) {
                boolean handled = tool.execute(player, instance, event.getBlockPosition(), null, HandClickType.LEFT_CLICK);
                if (handled) {
                    event.setCancelled(true);
                }
            }
        });

        @SuppressWarnings({"rawtypes", "unchecked"})
        EventNode rawNode = parentNode;
        rawNode.addChild(node);
    }
}
