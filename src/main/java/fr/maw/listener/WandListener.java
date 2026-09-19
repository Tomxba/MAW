package fr.maw.listener;

import fr.maw.MawConfig;
import fr.maw.session.PlayerSession;
import fr.maw.session.SessionManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Player;
import net.minestom.server.event.EventFilter;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.player.PlayerBlockBreakEvent;
import net.minestom.server.event.player.PlayerBlockInteractEvent;
import net.minestom.server.event.player.PlayerDisconnectEvent;
import net.minestom.server.item.ItemStack;

import java.util.Objects;

/**
 * Event listener handling selection wand interactions (left-click pos1, right-click pos2)
 * and player session cleanup.
 */
public final class WandListener {

    private final MawConfig config;
    private final SessionManager sessionManager;

    public WandListener(MawConfig config, SessionManager sessionManager) {
        this.config = Objects.requireNonNull(config, "config cannot be null");
        this.sessionManager = Objects.requireNonNull(sessionManager, "sessionManager cannot be null");
    }

    public void register(EventNode<?> parentNode) {
        var node = EventNode.type("maw-wand-node", EventFilter.PLAYER);

        // Left-click with wand -> pos1
        node.addListener(PlayerBlockBreakEvent.class, event -> {
            Player player = event.getPlayer();
            if (isHoldingWand(player)) {
                event.setCancelled(true);
                Point pos = event.getBlockPosition();
                PlayerSession session = sessionManager.getSession(player);
                session.setPos1(pos);

                String volumeInfo = session.isSelectionComplete()
                        ? String.format(" (%,d blocks)", session.getSelection().getVolume())
                        : "";

                player.sendMessage(Component.text(String.format("First position set to (%d, %d, %d)%s",
                        pos.blockX(), pos.blockY(), pos.blockZ(), volumeInfo), NamedTextColor.LIGHT_PURPLE));
            }
        });

        // Right-click with wand -> pos2
        node.addListener(PlayerBlockInteractEvent.class, event -> {
            Player player = event.getPlayer();
            if (isHoldingWand(player)) {
                event.setCancelled(true);
                Point pos = event.getBlockPosition();
                PlayerSession session = sessionManager.getSession(player);
                session.setPos2(pos);

                String volumeInfo = session.isSelectionComplete()
                        ? String.format(" (%,d blocks)", session.getSelection().getVolume())
                        : "";

                player.sendMessage(Component.text(String.format("Second position set to (%d, %d, %d)%s",
                        pos.blockX(), pos.blockY(), pos.blockZ(), volumeInfo), NamedTextColor.LIGHT_PURPLE));
            }
        });

        // Clean up session on disconnect
        node.addListener(PlayerDisconnectEvent.class, event -> {
            sessionManager.removeSession(event.getPlayer());
        });

        @SuppressWarnings({"rawtypes", "unchecked"})
        EventNode rawNode = parentNode;
        rawNode.addChild(node);
    }

    private boolean isHoldingWand(Player player) {
        ItemStack item = player.getItemInMainHand();
        if (item.isAir()) return false;
        return item.material().name().equalsIgnoreCase(config.wandItemNamespace());
    }
}
