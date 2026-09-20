package fr.maw;

import fr.maw.async.AsyncEditSession;
import fr.maw.async.MawAsyncEngine;
import fr.maw.async.TickDispatcher;
import fr.maw.guard.CommandGuardListener;
import fr.maw.guard.EditGuard;
import fr.maw.history.ChangeSet;
import fr.maw.testutil.InMemoryWorld;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.player.PlayerCommandEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.block.Block;
import net.minestom.server.network.packet.server.SendablePacket;
import net.minestom.server.network.player.GameProfile;
import net.minestom.server.network.player.PlayerConnection;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("EditGuard: who may change what")
public class EditGuardTest {

    private static boolean minestomInitialized;

    private static synchronized void initMinestom() {
        if (!minestomInitialized) {
            MinecraftServer.init();
            minestomInitialized = true;
        }
    }

    private static Player newPlayer(String name) {
        initMinestom();
        PlayerConnection connection = new PlayerConnection() {
            @Override
            public void sendPacket(SendablePacket packet) {
                // discarded
            }

            @Override
            public SocketAddress getRemoteAddress() {
                return new InetSocketAddress("127.0.0.1", 0);
            }

            @Override
            public boolean isOnline() {
                return true;
            }
        };
        return new Player(connection, new GameProfile(UUID.randomUUID(), name));
    }

    // ---- configuration --------------------------------------------------------------------------

    @Test
    @DisplayName("Without a guard, everything is allowed")
    public void should_allow_everything_by_default() {
        MawConfig config = MawConfig.defaultConfig();

        assertSame(EditGuard.ALLOW_ALL, config.editGuard());
        assertTrue(EditGuard.ALLOW_ALL.canEdit(null, null));
        assertTrue(EditGuard.ALLOW_ALL.allowsChange(null, null, 0, 0, 0, Block.COMMAND_BLOCK));
    }

    @Test
    @DisplayName("A guard set on the builder is the one of the configuration")
    public void should_keep_the_guard_of_the_builder() {
        EditGuard guard = new EditGuard() {
        };

        assertSame(guard, MawConfig.builder().editGuard(guard).build().editGuard());
        assertThrows(NullPointerException.class, () -> MawConfig.builder().editGuard(null).build());
    }

    // ---- blocks --------------------------------------------------------------------------------

    @Test
    @DisplayName("A refused block is skipped: not recorded, and not counted against the limit")
    public void should_skip_refused_blocks_without_using_the_limit() {
        InMemoryWorld world = new InMemoryWorld(Block.AIR);
        AsyncEditSession session = new AsyncEditSession(world, 2, null);
        session.setGate((x, y, z, block) -> x < 2, count -> { });

        // Two blocks are allowed, eight are refused: the limit of two is not exceeded.
        for (int x = 0; x < 10; x++) {
            session.setBlock(x, 0, 0, Block.STONE);
        }

        assertEquals(2, session.getChangedCount());
        assertEquals(8, session.getRefusedCount());
        ChangeSet changes = session.createChangeSet();
        assertEquals(2, changes.size());
        assertTrue(changes.getChanges().stream().allMatch(change -> change.x() < 2), "only the allowed positions are recorded");
    }

    @Test
    @DisplayName("The gate sees the position and the block")
    public void should_pass_position_and_block_to_the_gate() {
        InMemoryWorld world = new InMemoryWorld(Block.AIR);
        AsyncEditSession session = new AsyncEditSession(world, 100, null);
        session.setGate((x, y, z, block) -> !block.compare(Block.COMMAND_BLOCK) && y >= 0, count -> { });

        session.setBlock(0, 5, 0, Block.STONE);
        session.setBlock(0, 5, 1, Block.COMMAND_BLOCK);
        session.setBlock(0, -1, 2, Block.STONE);

        assertEquals(1, session.getChangedCount());
        assertEquals(2, session.getRefusedCount());
    }

    @Test
    @DisplayName("Without a gate nothing is refused")
    public void should_refuse_nothing_without_a_gate() {
        InMemoryWorld world = new InMemoryWorld(Block.AIR);
        AsyncEditSession session = new AsyncEditSession(world, 100, null);

        session.setBlock(0, 0, 0, Block.COMMAND_BLOCK);

        assertEquals(1, session.getChangedCount());
        assertEquals(0, session.getRefusedCount());
    }

    @Test
    @DisplayName("The player is told how many blocks were refused, once the operation ends")
    public void should_report_the_refused_count_on_commit() {
        InMemoryWorld world = new InMemoryWorld(Block.AIR);
        AtomicLong reported = new AtomicLong(-1);
        AsyncEditSession session = new AsyncEditSession(world, 100, null);
        session.setGate((x, y, z, block) -> false, reported::set);
        for (int x = 0; x < 7; x++) {
            session.setBlock(x, 0, 0, Block.STONE);
        }

        session.commit(new TickDispatcher(MawConfig.defaultConfig()), false, false).join();

        assertEquals(7, reported.get());
    }

    @Test
    @DisplayName("Nothing is reported when nothing was refused")
    public void should_not_report_when_nothing_was_refused() {
        InMemoryWorld world = new InMemoryWorld(Block.AIR);
        AtomicLong reported = new AtomicLong(-1);
        AsyncEditSession session = new AsyncEditSession(world, 100, null);
        session.setGate((x, y, z, block) -> true, reported::set);

        session.commit(new TickDispatcher(MawConfig.defaultConfig()), false, false).join();

        assertEquals(-1, reported.get());
    }

    @Test
    @DisplayName("A chunk that is not loaded reads as air, not as an error")
    public void should_read_an_unloaded_chunk_as_air() {
        initMinestom();
        InstanceContainer instance = MinecraftServer.getInstanceManager().createInstanceContainer();
        AsyncEditSession session = new AsyncEditSession(instance, 100, null);

        assertEquals(Block.AIR, session.getBlock(1000, 70, -1000));
        session.setGate((x, y, z, block) -> false, count -> { });
        session.setBlock(1000, 70, -1000, Block.STONE);

        assertEquals(1, session.getRefusedCount(), "a refused block of an unloaded chunk is refused, not a failure");
    }

    // ---- commands ------------------------------------------------------------------------------

    @Test
    @DisplayName("A typed command is recognised as MAW's by its name or alias, whatever the case")
    public void should_recognise_maw_commands() {
        CommandGuardListener listener = new CommandGuardListener(EditGuard.ALLOW_ALL, List.of("//set", "/set", "set", "//WAND"));

        assertTrue(listener.isMawCommand("/set stone"));
        assertTrue(listener.isMawCommand("set stone"));
        assertTrue(listener.isMawCommand("  /SET stone"));
        assertTrue(listener.isMawCommand("//wand"));
        assertFalse(listener.isMawCommand("atelier create"));
        assertFalse(listener.isMawCommand("settle"));
        assertFalse(listener.isMawCommand(""));
        assertFalse(listener.isMawCommand(null));
    }

    @Test
    @DisplayName("A MAW command is cancelled for a player the guard refuses, and only for them")
    public void should_cancel_the_command_of_a_refused_player() {
        Player refused = newPlayer("Refused");
        Player allowed = newPlayer("Allowed");
        EditGuard guard = new EditGuard() {
            @Override
            public boolean canEdit(Player player, Instance instance) {
                return player == allowed;
            }
        };
        new CommandGuardListener(guard, List.of("/set", "set")).register(MinecraftServer.getGlobalEventHandler());

        PlayerCommandEvent refusedSet = new PlayerCommandEvent(refused, "/set stone");
        PlayerCommandEvent allowedSet = new PlayerCommandEvent(allowed, "/set stone");
        PlayerCommandEvent refusedOther = new PlayerCommandEvent(refused, "atelier list");
        EventDispatcher.call(refusedSet);
        EventDispatcher.call(allowedSet);
        EventDispatcher.call(refusedOther);

        assertTrue(refusedSet.isCancelled());
        assertFalse(allowedSet.isCancelled());
        assertFalse(refusedOther.isCancelled(), "a command that is not MAW's is none of the guard's business");
    }

    // ---- busy ----------------------------------------------------------------------------------

    @Test
    @DisplayName("The engine says whether a task is still running")
    public void should_tell_when_a_task_is_running() throws Exception {
        MawAsyncEngine engine = new MawAsyncEngine(2);
        CountDownLatch started = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        assertFalse(engine.hasActiveTasks());

        CompletableFuture<Void> task = engine.runAsync(() -> {
            started.countDown();
            try {
                release.await(10, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        assertTrue(started.await(5, TimeUnit.SECONDS));
        assertTrue(engine.hasActiveTasks(), "running");

        release.countDown();
        task.get(5, TimeUnit.SECONDS);
        assertFalse(engine.hasActiveTasks(), "done");
        engine.shutdown();
    }

    @Test
    @DisplayName("An instance is busy while changes are still being applied to it over several ticks")
    public void should_tell_when_changes_are_still_being_applied() throws InterruptedException {
        initMinestom();
        InstanceContainer instance = MinecraftServer.getInstanceManager().createInstanceContainer();
        for (int chunkX = 0; chunkX < 3; chunkX++) {
            instance.loadChunk(chunkX, 0).join();
        }
        // One chunk per tick: three chunks take three ticks.
        MawConfig config = MawConfig.builder().maxChunksPerTick(1).build();
        TickDispatcher dispatcher = new TickDispatcher(config);
        AsyncEditSession session = new AsyncEditSession(instance, 1000, null);
        session.setBlock(0, 64, 0, Block.STONE);
        session.setBlock(16, 64, 0, Block.STONE);
        session.setBlock(32, 64, 0, Block.STONE);

        CompletableFuture<?> result = session.commit(dispatcher, false, false);

        assertFalse(result.isDone(), "the next slices wait for the next ticks");
        assertTrue(dispatcher.hasPending(instance), "busy");
        assertFalse(dispatcher.hasPending(MinecraftServer.getInstanceManager().createInstanceContainer()), "another instance is not");

        // A batch is written on Minestom's own threads and reports back on the next tick of its instance: a
        // server does tick its instances, a test has to.
        for (int tick = 0; tick < 500 && !result.isDone(); tick++) {
            MinecraftServer.getSchedulerManager().processTick();
            instance.tick(System.currentTimeMillis());
            Thread.sleep(5);
        }
        assertTrue(result.isDone());
        assertFalse(dispatcher.hasPending(instance), "idle again");
        assertFalse(dispatcher.hasPending(null));
    }
}
