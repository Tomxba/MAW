package fr.maw.session;

import fr.maw.MawConfig;
import net.minestom.server.entity.Player;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry and manager for all active player WorldEdit sessions.
 */
public final class SessionManager {

    private final MawConfig config;
    private final Map<UUID, PlayerSession> sessions = new ConcurrentHashMap<>();

    public SessionManager(MawConfig config) {
        this.config = Objects.requireNonNull(config, "config cannot be null");
    }

    public PlayerSession getSession(UUID uuid) {
        return sessions.computeIfAbsent(uuid, id -> new PlayerSession(id, config));
    }

    public PlayerSession getSession(Player player) {
        return getSession(player.getUuid());
    }

    public void removeSession(UUID uuid) {
        sessions.remove(uuid);
    }

    public void removeSession(Player player) {
        removeSession(player.getUuid());
    }

    public void clear() {
        sessions.clear();
    }

    public int getActiveSessionCount() {
        return sessions.size();
    }
}
