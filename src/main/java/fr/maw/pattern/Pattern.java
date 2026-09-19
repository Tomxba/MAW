package fr.maw.pattern;

import net.minestom.server.instance.block.Block;

/**
 * Functional interface that determines the new Block to place at a given coordinate.
 */
@FunctionalInterface
public interface Pattern {

    /**
     * Evaluates the pattern at the given coordinate.
     *
     * @param x absolute world X
     * @param y absolute world Y
     * @param z absolute world Z
     * @param currentBlock the existing block at this position
     * @return the new Block to place
     */
    Block apply(int x, int y, int z, Block currentBlock);
}
