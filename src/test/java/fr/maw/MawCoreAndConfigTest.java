package fr.maw;

import fr.maw.async.MawAsyncEngine;
import fr.maw.async.TickDispatcher;
import fr.maw.session.SessionManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("MAW Core and Configuration Tests")
public class MawCoreAndConfigTest {

    @Test
    @DisplayName("Should use sensible defaults when creating default config")
    public void should_use_sensible_defaults_when_creating_default_config() {
        // Arrange & Act
        MawConfig config = MawConfig.defaultConfig();

        // Assert
        assertEquals(10_000_000, config.maxBlocksPerOperation());
        assertEquals(20, config.maxHistoryPerPlayer());
        assertEquals(5, config.timeBudgetPerTickMs());
        assertEquals(20, config.maxChunksPerTick());
        assertFalse(config.defaultUpdatePhysics());
        assertTrue(config.defaultManageEntities());
        assertEquals("minecraft:wooden_axe", config.wandItemNamespace());
        assertTrue(config.asyncWorkerThreads() >= 2);
    }

    @Test
    @DisplayName("Should accept custom values via Builder")
    public void should_accept_custom_values_when_built_with_builder() {
        // Arrange & Act
        MawConfig custom = MawConfig.builder()
                .maxBlocksPerOperation(500_000)
                .maxHistoryPerPlayer(10)
                .timeBudgetPerTickMs(2)
                .maxChunksPerTick(5)
                .defaultUpdatePhysics(true)
                .defaultManageEntities(false)
                .wandItemNamespace("minecraft:iron_axe")
                .asyncWorkerThreads(4)
                .build();

        // Assert
        assertEquals(500_000, custom.maxBlocksPerOperation());
        assertEquals(10, custom.maxHistoryPerPlayer());
        assertEquals(2, custom.timeBudgetPerTickMs());
        assertEquals(5, custom.maxChunksPerTick());
        assertTrue(custom.defaultUpdatePhysics());
        assertFalse(custom.defaultManageEntities());
        assertEquals("minecraft:iron_axe", custom.wandItemNamespace());
        assertEquals(4, custom.asyncWorkerThreads());
    }

    @Test
    @DisplayName("Should throw NullPointerException when wand namespace is null")
    public void should_throw_null_pointer_when_wand_namespace_is_null() {
        // Arrange, Act & Assert
        assertThrows(NullPointerException.class, () -> {
            MawConfig.builder().wandItemNamespace(null).build();
        });
    }

    @Test
    @DisplayName("Should correctly initialize Maw singleton and provide core components")
    public void should_initialize_maw_singleton_and_provide_components() {
        // Arrange
        MawConfig config = MawConfig.builder()
                .maxBlocksPerOperation(100_000)
                .build();

        // Act
        Maw maw = Maw.init(config);

        // Assert
        assertSame(config, maw.getConfig());
        assertNotNull(maw.getSessionManager());
        assertInstanceOf(SessionManager.class, maw.getSessionManager());
        assertNotNull(maw.getAsyncEngine());
        assertInstanceOf(MawAsyncEngine.class, maw.getAsyncEngine());
        assertNotNull(maw.getTickDispatcher());
        assertInstanceOf(TickDispatcher.class, maw.getTickDispatcher());
        assertSame(maw, Maw.getInstance());
    }
}
