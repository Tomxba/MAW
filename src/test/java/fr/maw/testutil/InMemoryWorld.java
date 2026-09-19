package fr.maw.testutil;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.block.Block;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Fake in-memory world implementing {@link Block.Getter} for decoupled, ultra-fast unit and integration tests.
 * Follows the Fake Object pattern from testing skills.
 */
public class InMemoryWorld implements Block.Getter {
    private final Map<Point, Block> blocks = new HashMap<>();
    private final Block defaultBlock;

    public InMemoryWorld(Block defaultBlock) {
        this.defaultBlock = defaultBlock;
    }

    public InMemoryWorld() {
        this(Block.AIR);
    }

    public void setBlock(int x, int y, int z, Block block) {
        blocks.put(new Vec(x, y, z), block);
    }

    public void setBlock(Point p, Block block) {
        setBlock(p.blockX(), p.blockY(), p.blockZ(), block);
    }

    @Override
    public Block getBlock(int x, int y, int z, Condition condition) {
        return blocks.getOrDefault(new Vec(x, y, z), defaultBlock);
    }

    public Map<Point, Block> getBlocks() {
        return Collections.unmodifiableMap(blocks);
    }

    public int getStoredBlockCount() {
        return blocks.size();
    }

    public void clear() {
        blocks.clear();
    }
}
