package fr.maw;

import fr.maw.pattern.Mask;
import fr.maw.pattern.Pattern;
import fr.maw.pattern.PatternParser;
import fr.maw.pattern.RandomPattern;
import net.minestom.server.instance.block.Block;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Pattern and Mask Parser Edge Cases Tests")
public class PatternAndMaskEdgeCasesTest {

    @Test
    @DisplayName("Should throw IllegalArgumentException when pattern is empty or null")
    public void should_throw_when_pattern_is_empty_or_null() {
        assertThrows(IllegalArgumentException.class, () -> PatternParser.parsePattern(null));
        assertThrows(IllegalArgumentException.class, () -> PatternParser.parsePattern(""));
        assertThrows(IllegalArgumentException.class, () -> PatternParser.parsePattern("   "));
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when block is unknown")
    public void should_throw_when_block_is_unknown() {
        assertThrows(IllegalArgumentException.class, () -> PatternParser.parsePattern("non_existent_block_12345"));
        assertThrows(IllegalArgumentException.class, () -> PatternParser.parseBlock("invalid_block"));
    }

    @Test
    @DisplayName("Should parse complex block states with multiple comma-separated properties")
    public void should_parse_complex_block_states_with_multiple_properties() {
        Pattern pattern = PatternParser.parsePattern("oak_door[facing=east,half=upper,hinge=left,open=true]");
        Block block = pattern.apply(0, 0, 0, Block.AIR);

        assertEquals("east", block.getProperty("facing"));
        assertEquals("upper", block.getProperty("half"));
        assertEquals("left", block.getProperty("hinge"));
        assertEquals("true", block.getProperty("open"));
    }

    @Test
    @DisplayName("Should parse weighted pattern with whitespace around commas and percentages")
    public void should_parse_weighted_pattern_with_whitespace() {
        Pattern pattern = PatternParser.parsePattern("  60% stone ,  40% dirt  ");
        assertInstanceOf(RandomPattern.class, pattern);

        Block b = pattern.apply(0, 0, 0, Block.AIR);
        assertTrue(b.equals(Block.STONE) || b.equals(Block.DIRT));
    }

    @Test
    @DisplayName("Should parse multi-block mask correctly")
    public void should_parse_multi_block_mask() {
        Mask mask = PatternParser.parseMask("stone,dirt,cobblestone");

        assertTrue(mask.test(0, 0, 0, Block.STONE));
        assertTrue(mask.test(0, 0, 0, Block.DIRT));
        assertTrue(mask.test(0, 0, 0, Block.COBBLESTONE));
        assertFalse(mask.test(0, 0, 0, Block.GOLD_BLOCK));
        assertFalse(mask.test(0, 0, 0, Block.AIR));
    }

    @Test
    @DisplayName("Should parse negated multi-block mask")
    public void should_parse_negated_multi_block_mask() {
        Mask mask = PatternParser.parseMask("!stone,dirt");

        // Negated: true for anything EXCEPT stone and dirt
        assertFalse(mask.test(0, 0, 0, Block.STONE));
        assertFalse(mask.test(0, 0, 0, Block.DIRT));
        assertTrue(mask.test(0, 0, 0, Block.DIAMOND_BLOCK));
        assertTrue(mask.test(0, 0, 0, Block.AIR));
    }

    @Test
    @DisplayName("Should return ALWAYS_TRUE when mask is null or blank")
    public void should_return_always_true_when_mask_is_blank() {
        Mask mask1 = PatternParser.parseMask(null);
        Mask mask2 = PatternParser.parseMask("  ");

        assertTrue(mask1.test(0, 0, 0, Block.AIR));
        assertTrue(mask1.test(0, 0, 0, Block.STONE));
        assertTrue(mask2.test(0, 0, 0, Block.DIRT));
    }
}
