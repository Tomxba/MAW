package fr.maw.pattern;

import net.minestom.server.instance.block.Block;

/**
 * Predicate to determine whether a block at a given coordinate should be affected.
 */
@FunctionalInterface
public interface Mask {

    /**
     * Tests whether the coordinate and existing block match the mask.
     */
    boolean test(int x, int y, int z, Block currentBlock);

    default Mask negate() {
        return (x, y, z, currentBlock) -> !test(x, y, z, currentBlock);
    }

    default Mask and(Mask other) {
        return (x, y, z, currentBlock) -> test(x, y, z, currentBlock) && other.test(x, y, z, currentBlock);
    }

    default Mask or(Mask other) {
        return (x, y, z, currentBlock) -> test(x, y, z, currentBlock) || other.test(x, y, z, currentBlock);
    }
}
