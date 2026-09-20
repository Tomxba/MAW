package fr.maw;

import fr.maw.async.MawAsyncEngine;
import fr.maw.async.TickDispatcher;
import fr.maw.command.*;
import fr.maw.guard.CommandGuardListener;
import fr.maw.guard.EditGuard;
import fr.maw.listener.WandListener;
import fr.maw.session.SessionManager;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandManager;
import net.minestom.server.command.builder.Command;
import net.minestom.server.event.EventNode;
import net.minestom.server.instance.Instance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
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
    private final fr.maw.listener.ToolListener toolListener;
    private final fr.maw.schematic.SchematicManager schematicManager;
    private final List<Command> registeredCommands = new ArrayList<>();

    private boolean enabled = false;

    private Maw(MawConfig config) {
        this.config = Objects.requireNonNull(config, "config cannot be null");
        this.asyncEngine = new MawAsyncEngine(config.asyncWorkerThreads());
        this.sessionManager = new SessionManager(config);
        this.tickDispatcher = new TickDispatcher(config);
        this.wandListener = new WandListener(config, sessionManager);
        this.toolListener = new fr.maw.listener.ToolListener(sessionManager);
        this.schematicManager = fr.maw.schematic.SchematicManager.defaultManager();
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
     * Initializes MAW with custom configuration (without attaching event node immediately).
     */
    public static Maw init(MawConfig config) {
        return init(null, config);
    }

    /**
     * Initializes MAW with default configuration.
     */
    public static Maw init() {
        return init(null, MawConfig.defaultConfig());
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
            toolListener.register(parentNode);
        }

        // 2. Register all WorldEdit commands
        registeredCommands.add(new WandCommand(config));
        registeredCommands.addAll(PosCommands.create(sessionManager));
        registeredCommands.add(new SetCommand(config, sessionManager, asyncEngine, tickDispatcher));
        registeredCommands.add(new ReplaceCommand(config, sessionManager, asyncEngine, tickDispatcher));
        registeredCommands.addAll(WallsFacesHollowCommands.create(config, sessionManager, asyncEngine, tickDispatcher));
        registeredCommands.addAll(SphereCylCommands.create(config, sessionManager, asyncEngine, tickDispatcher));
        registeredCommands.addAll(ClipboardCommands.create(config, sessionManager, asyncEngine, tickDispatcher));
        registeredCommands.addAll(HistoryCommands.create(sessionManager, asyncEngine, tickDispatcher));
        registeredCommands.addAll(InfoCommands.create(sessionManager));

        // New Extended WorldEdit Suites
        registeredCommands.addAll(BrushCommands.create(config, sessionManager, asyncEngine, tickDispatcher));
        registeredCommands.addAll(ToolCommands.create(config, sessionManager, asyncEngine, tickDispatcher));
        registeredCommands.addAll(SelectionModifyCommands.create(sessionManager));
        registeredCommands.addAll(RegionOpsCommands.create(config, sessionManager, asyncEngine, tickDispatcher));
        registeredCommands.addAll(ShapeCommands.create(config, sessionManager, asyncEngine, tickDispatcher));
        registeredCommands.addAll(SchematicCommands.create(sessionManager, schematicManager));
        registeredCommands.addAll(EnvironmentCommands.create(config, sessionManager, asyncEngine, tickDispatcher));
        registeredCommands.addAll(BiomeCommands.create(sessionManager));
        registeredCommands.addAll(NavigationCommands.create());
        registeredCommands.addAll(GlobalMaskCommand.create(sessionManager));

        // 3. With a guard, refuse MAW's commands to a player who may not edit where they stand
        if (config.editGuard() != EditGuard.ALLOW_ALL) {
            if (parentNode != null) {
                List<String> commandNames = new ArrayList<>();
                for (Command cmd : registeredCommands) {
                    commandNames.addAll(Arrays.asList(cmd.getNames()));
                }
                new CommandGuardListener(config.editGuard(), commandNames).register(parentNode);
            } else {
                LOGGER.warn("An EditGuard is set but MAW got no event node: EditGuard#canEdit cannot be enforced "
                        + "(only EditGuard#allowsChange is). Pass the server event node to Maw.init(...).");
            }
        }

        try {
            CommandManager commandManager = MinecraftServer.getCommandManager();
            if (commandManager != null) {
                for (Command cmd : registeredCommands) {
                    commandManager.register(cmd);
                }
            }
        } catch (Throwable t) {
            LOGGER.debug("MinecraftServer CommandManager unavailable in this environment: {}", t.getMessage());
        }

        enabled = true;
        LOGGER.info("MAW (Minestom Async WorldEdit) enabled successfully with {} commands.", registeredCommands.size());
    }

    /**
     * Disables MAW, unregisters commands and shuts down background worker threads.
     */
    public synchronized void disable() {
        if (!enabled) return;

        try {
            CommandManager commandManager = MinecraftServer.getCommandManager();
            if (commandManager != null) {
                for (Command cmd : registeredCommands) {
                    commandManager.unregister(cmd);
                }
            }
        } catch (Throwable t) {
            LOGGER.debug("MinecraftServer CommandManager unavailable during shutdown: {}", t.getMessage());
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

    /**
     * Whether an operation is still running or applying its changes to {@code instance} (possibly over
     * several ticks). While it is, the world is between two states and should not be saved: wait until
     * this is false. An operation still being computed counts for every instance, since it has not said
     * yet where it will write.
     */
    public boolean isBusy(Instance instance) {
        return asyncEngine.hasActiveTasks() || tickDispatcher.hasPending(instance);
    }

    /** The guard of the configuration ({@link EditGuard#ALLOW_ALL} unless one was set). */
    public EditGuard getGuard() {
        return config.editGuard();
    }

    public fr.maw.listener.ToolListener getToolListener() {
        return toolListener;
    }

    public fr.maw.schematic.SchematicManager getSchematicManager() {
        return schematicManager;
    }
}
