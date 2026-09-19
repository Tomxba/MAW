package fr.maw.pattern;

import net.minestom.server.instance.block.Block;
import java.util.Objects;

/**
 * A pattern that always resolves to a constant block.
 */
public final class SingleBlockPattern implements Pattern {

    private final Block block;

    public SingleBlockPattern(Block block) {
        this.block = Objects.requireNonNull(block, "block cannot be null");
    }

    public Block getBlock() {
        return block;
    }

    @Override
    public Block apply(int x, int y, int z, Block currentBlock) {
        return block;
    }

    @Override
    public String toString() {
        return "SingleBlockPattern[" + block.name() + "]";
    }
}
