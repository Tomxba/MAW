package fr.maw;

import fr.maw.pattern.*;
import net.minestom.server.instance.block.Block;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class PatternAndMaskTest {

    @Test
    public void testParseSingleBlockPattern() {
        Pattern p1 = PatternParser.parsePattern("stone");
        assertInstanceOf(SingleBlockPattern.class, p1);
        assertEquals(Block.STONE, p1.apply(0, 0, 0, Block.AIR));

        Pattern p2 = PatternParser.parsePattern("minecraft:dirt");
        assertInstanceOf(SingleBlockPattern.class, p2);
        assertEquals(Block.DIRT, p2.apply(0, 0, 0, Block.AIR));
    }

    @Test
    public void testParseBlockStatePattern() {
        Pattern p = PatternParser.parsePattern("oak_stairs[facing=north,half=top]");
        Block result = p.apply(0, 0, 0, Block.AIR);

        assertEquals("north", result.getProperty("facing"));
        assertEquals("top", result.getProperty("half"));
    }

    @Test
    public void testRandomPatternDistribution() {
        Pattern pattern = PatternParser.parsePattern("80%stone,20%dirt");
        assertInstanceOf(RandomPattern.class, pattern);

        Map<String, Integer> counts = new HashMap<>();
        int iterations = 10_000;
        for (int i = 0; i < iterations; i++) {
            Block b = pattern.apply(i, 0, 0, Block.AIR);
            counts.put(b.name(), counts.getOrDefault(b.name(), 0) + 1);
        }

        int stoneCount = counts.getOrDefault("minecraft:stone", 0);
        int dirtCount = counts.getOrDefault("minecraft:dirt", 0);

        // Expect ~80% stone, ~20% dirt with reasonable variance margin
        assertTrue(stoneCount > 7000 && stoneCount < 9000, "Stone count should be ~80%: " + stoneCount);
        assertTrue(dirtCount > 1000 && dirtCount < 3000, "Dirt count should be ~20%: " + dirtCount);
    }

    @Test
    public void testMasks() {
        Mask airMask = PatternParser.parseMask("#air");
        assertTrue(airMask.test(0, 0, 0, Block.AIR));
        assertFalse(airMask.test(0, 0, 0, Block.STONE));

        Mask existingMask = PatternParser.parseMask("#existing");
        assertFalse(existingMask.test(0, 0, 0, Block.AIR));
        assertTrue(existingMask.test(0, 0, 0, Block.STONE));

        Mask stoneMask = PatternParser.parseMask("stone");
        assertTrue(stoneMask.test(0, 0, 0, Block.STONE));
        assertFalse(stoneMask.test(0, 0, 0, Block.DIRT));

        Mask notStoneMask = PatternParser.parseMask("!stone");
        assertFalse(notStoneMask.test(0, 0, 0, Block.STONE));
        assertTrue(notStoneMask.test(0, 0, 0, Block.DIRT));

        Mask multiMask = PatternParser.parseMask("stone,dirt");
        assertTrue(multiMask.test(0, 0, 0, Block.STONE));
        assertTrue(multiMask.test(0, 0, 0, Block.DIRT));
        assertFalse(multiMask.test(0, 0, 0, Block.BEDROCK));
    }
}
