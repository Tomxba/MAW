package fr.maw;

import fr.maw.MawConfig;
import fr.maw.async.MawAsyncEngine;
import fr.maw.async.TickDispatcher;
import fr.maw.pattern.Masks;
import fr.maw.pattern.SingleBlockPattern;
import fr.maw.session.SessionManager;
import fr.maw.tool.HandClickType;
import fr.maw.tool.brush.*;
import fr.maw.tool.specialized.*;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Brushes and Specialized Tools Unit Tests")
public class ToolsAndBrushesTest {

    private final MawConfig config = MawConfig.defaultConfig();
    private final SessionManager sessionManager = new SessionManager(config);
    private final MawAsyncEngine asyncEngine = new MawAsyncEngine(4);
    private final TickDispatcher dispatcher = new TickDispatcher(config);

    @Test
    @DisplayName("Should configure and mutate SphereBrush properties")
    public void should_configure_sphere_brush() {
        SingleBlockPattern pattern = new SingleBlockPattern(Block.STONE);
        SphereBrush brush = new SphereBrush(config, sessionManager, asyncEngine, dispatcher, pattern, 5, false);

        assertEquals(5, brush.getRadius());
        assertFalse(brush.isHollow());
        assertEquals(pattern, brush.getPattern());

        brush.setRadius(10);
        assertEquals(10, brush.getRadius());

        brush.setHollow(true);
        assertTrue(brush.isHollow());

        brush.setMask(Masks.ofBlock(Block.DIRT));
        assertNotNull(brush.getMask());
    }

    @Test
    @DisplayName("Should configure and mutate CylinderBrush properties")
    public void should_configure_cylinder_brush() {
        SingleBlockPattern pattern = new SingleBlockPattern(Block.SAND);
        CylinderBrush brush = new CylinderBrush(config, sessionManager, asyncEngine, dispatcher, pattern, 4, 3, false);

        assertEquals(4, brush.getRadius());
        assertEquals(3, brush.getHeight());
        assertFalse(brush.isHollow());

        brush.setHeight(6);
        assertEquals(6, brush.getHeight());
    }

    @Test
    @DisplayName("Should configure and mutate SmoothBrush properties")
    public void should_configure_smooth_brush() {
        SmoothBrush brush = new SmoothBrush(config, sessionManager, asyncEngine, dispatcher, 4, 2);

        assertEquals(4, brush.getRadius());
        assertEquals(2, brush.getIterations());

        brush.setIterations(5);
        assertEquals(5, brush.getIterations());
    }

    @Test
    @DisplayName("Should configure ReplacerTool pattern")
    public void should_configure_replacer_tool() {
        SingleBlockPattern pattern1 = new SingleBlockPattern(Block.DIRT);
        SingleBlockPattern pattern2 = new SingleBlockPattern(Block.DIAMOND_BLOCK);

        ReplacerTool tool = new ReplacerTool(config, sessionManager, asyncEngine, dispatcher, pattern1);
        assertEquals(pattern1, tool.getPattern());

        tool.setPattern(pattern2);
        assertEquals(pattern2, tool.getPattern());

        // Left click should not do anything or return appropriate result
        assertFalse(tool.execute(null, null, null, BlockFace.TOP, HandClickType.LEFT_CLICK));
    }

    @Test
    @DisplayName("Should configure TreeTool type")
    public void should_configure_tree_tool() {
        TreeTool tool = new TreeTool(config, sessionManager, asyncEngine, dispatcher, "birch");
        assertEquals("birch", tool.getTreeType());

        tool.setTreeType("spruce");
        assertEquals("spruce", tool.getTreeType());
    }

    @Test
    @DisplayName("Should verify HandClickType enum contracts")
    public void should_support_hand_click_types() {
        assertEquals(2, HandClickType.values().length);
        assertEquals(HandClickType.LEFT_CLICK, HandClickType.valueOf("LEFT_CLICK"));
        assertEquals(HandClickType.RIGHT_CLICK, HandClickType.valueOf("RIGHT_CLICK"));
    }
}
