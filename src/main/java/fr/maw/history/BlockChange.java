package fr.maw.history;

import net.minestom.server.instance.block.Block;

/**
 * An immutable record representing a single block state change at a specific coordinate.
 */
public record BlockChange(int x, int y, int z, Block previousBlock, Block newBlock) {

    public BlockChange {
        if (previousBlock == null) throw new NullPointerException("previousBlock cannot be null");
        if (newBlock == null) throw new NullPointerException("newBlock cannot be null");
    }

    public BlockChange inverse() {
        return new BlockChange(x, y, z, newBlock, previousBlock);
    }
}
