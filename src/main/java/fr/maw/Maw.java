package fr.maw;

import fr.maw.async.MawAsyncEngine;
import fr.maw.async.TickDispatcher;
import fr.maw.command.*;
import fr.maw.listener.WandListener;
import fr.maw.session.SessionManager;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandManager;
import net.minestom.server.command.builder.Command;
import net.minestom.server.event.EventNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Main entry point for the MAW (Minestom Async WorldEdit) library.
 * Easy 1-line integration into any Minestom server.
 */
public final class Maw {

    private static final Logger LOGGER = LoggerFactory.getLogger(Maw.class);
    private static volatile Maw instance;

    private final MawConfig config;
    private final MawAsyncEngine asyncEngine;
    private final SessionManager sessionManager;
    private final TickDispatcher tickDispatcher;
    private final WandListener wandListener;
    private final List<Command> registeredCommands = new ArrayList<>();

    private boolean enabled = false;

    private Maw(MawConfig config) {
        this.config = Objects.requireNonNull(config, "config cannot be null");
        this.asyncEngine = new MawAsyncEngine(config.asyncWorkerThreads());
        this.sessionManager = new SessionManager(config);
        this.tickDispatcher = new TickDispatcher(config);
        this.wandListener = new WandListener(config, sessionManager);
    }

    /**
     * Initializes MAW with custom configuration.
     *
     * @param parentNode the server event node to attach wand listeners to
     * @param config the configuration options
     * @return the singleton MAW instance
     */
    public static synchronized Maw init(EventNode<?> parentNode, MawConfig config) {
        if (instance != null) {
            LOGGER.warn("MAW is already initialized! Returning existing instance.");
            return instance;
        }

        instance = new Maw(config);
        instance.enable(parentNode);
        return instance;
    }

    /**
     * Initializes MAW with default configuration.
     *
     * @param parentNode the server event node to attach wand listeners to
     * @return the singleton MAW instance
     */
    public static Maw init(EventNode<?> parentNode) {
        return init(parentNode, MawConfig.defaultConfig());
    }

    /**
     * Retrieves the active MAW instance.
     */
    public static Maw getInstance() {
        Maw current = instance;
        if (current == null) {
            throw new IllegalStateException("MAW has not been initialized yet! Call Maw.init(...) first.");
        }
        return current;
    }

    /**
     * Enables MAW, registers event listeners and registers all WorldEdit commands.
     */
    public synchronized void enable(EventNode<?> parentNode) {
        if (enabled) return;

        // 1. Register wand and interaction listeners
        if (parentNode != null) {
            wandListener.register(parentNode);
        }

        // 2. Register all WorldEdit commands
        CommandManager commandManager = MinecraftServer.getCommandManager();

        registeredCommands.add(new WandCommand(config));
        registeredCommands.addAll(PosCommands.create(sessionManager));
        registeredCommands.add(new SetCommand(config, sessionManager, asyncEngine, tickDispatcher));
        registeredCommands.add(new ReplaceCommand(config, sessionManager, asyncEngine, tickDispatcher));
        registeredCommands.addAll(WallsFacesHollowCommands.create(config, sessionManager, asyncEngine, tickDispatcher));
        registeredCommands.addAll(SphereCylCommands.create(config, sessionManager, asyncEngine, tickDispatcher));
        registeredCommands.addAll(ClipboardCommands.create(config, sessionManager, asyncEngine, tickDispatcher));
        registeredCommands.addAll(HistoryCommands.create(sessionManager, asyncEngine, tickDispatcher));
        registeredCommands.addAll(InfoCommands.create(sessionManager));

        for (Command cmd : registeredCommands) {
            commandManager.register(cmd);
        }

        enabled = true;
        LOGGER.info("MAW (Minestom Async WorldEdit) enabled successfully with {} commands.", registeredCommands.size());
    }

    /**
     * Disables MAW, unregisters commands and shuts down background worker threads.
     */
    public synchronized void disable() {
        if (!enabled) return;

        CommandManager commandManager = MinecraftServer.getCommandManager();
        for (Command cmd : registeredCommands) {
            commandManager.unregister(cmd);
        }
        registeredCommands.clear();

        sessionManager.clear();
        asyncEngine.shutdown();

        enabled = false;
        LOGGER.info("MAW (Minestom Async WorldEdit) disabled.");
    }

    public MawConfig getConfig() {
        return config;
    }

    public MawAsyncEngine getAsyncEngine() {
        return asyncEngine;
    }

    public SessionManager getSessionManager() {
        return sessionManager;
    }

    public TickDispatcher getTickDispatcher() {
        return tickDispatcher;
    }

    public boolean isEnabled() {
        return enabled;
    }
}
