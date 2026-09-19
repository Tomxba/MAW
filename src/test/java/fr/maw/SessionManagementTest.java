package fr.maw;

import fr.maw.clipboard.Clipboard;
import fr.maw.selection.Selection;
import fr.maw.session.PlayerSession;
import fr.maw.session.SessionManager;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.block.Block;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Session and Player State Tests")
public class SessionManagementTest {

    @Test
    @DisplayName("Should create distinct session per player UUID and retrieve same instance")
    public void should_create_and_retrieve_same_session_for_uuid() {
        // Arrange
        MawConfig config = MawConfig.defaultConfig();
        SessionManager manager = new SessionManager(config);
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();

        // Act
        PlayerSession session1 = manager.getSession(id1);
        PlayerSession session1Again = manager.getSession(id1);
        PlayerSession session2 = manager.getSession(id2);

        // Assert
        assertSame(session1, session1Again);
        assertNotSame(session1, session2);
        assertEquals(2, manager.getActiveSessionCount());
    }

    @Test
    @DisplayName("Should remove session correctly on player disconnect")
    public void should_remove_session_on_disconnect() {
        // Arrange
        SessionManager manager = new SessionManager(MawConfig.defaultConfig());
        UUID id = UUID.randomUUID();
        PlayerSession session = manager.getSession(id);
        assertEquals(1, manager.getActiveSessionCount());

        // Act
        manager.removeSession(id);

        // Assert
        assertEquals(0, manager.getActiveSessionCount());
    }

    @Test
    @DisplayName("Should manage Pos1, Pos2 and complete Selection")
    public void should_manage_selection_lifecycle() {
        // Arrange
        PlayerSession session = new PlayerSession(UUID.randomUUID(), MawConfig.defaultConfig());
        assertFalse(session.isSelectionComplete());

        // Act & Assert incomplete
        assertThrows(IllegalStateException.class, session::getSelection);

        // Set Pos1
        session.setPos1(new Vec(0, 10, 0));
        assertFalse(session.isSelectionComplete());

        // Set Pos2
        session.setPos2(new Vec(5, 15, 5));
        assertTrue(session.isSelectionComplete());

        Selection selection = session.getSelection();
        assertNotNull(selection);
        assertEquals(6 * 6 * 6, selection.getVolume());
    }

    @Test
    @DisplayName("Should store and retrieve Clipboard")
    public void should_store_and_retrieve_clipboard() {
        // Arrange
        PlayerSession session = new PlayerSession(UUID.randomUUID(), MawConfig.defaultConfig());
        assertNull(session.getClipboard());

        Clipboard clipboard = new Clipboard(
                Map.of(new Vec(0, 0, 0), Block.STONE),
                Vec.ZERO,
                new Vec(1, 1, 1)
        );

        // Act
        session.setClipboard(clipboard);

        // Assert
        assertSame(clipboard, session.getClipboard());
    }

    @Test
    @DisplayName("Should toggle and retrieve physics and entity flags")
    public void should_toggle_physics_and_entity_flags() {
        // Arrange
        PlayerSession session = new PlayerSession(UUID.randomUUID(), MawConfig.defaultConfig());

        // Act & Assert physics
        assertFalse(session.isUpdatePhysics());
        session.setUpdatePhysics(true);
        assertTrue(session.isUpdatePhysics());

        // Act & Assert entities
        assertTrue(session.isManageEntities());
        session.setManageEntities(false);
        assertFalse(session.isManageEntities());
    }
}
