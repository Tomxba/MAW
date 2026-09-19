package fr.maw.pattern;

import net.minestom.server.instance.block.Block;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Standard implementations and factory methods for masks.
 */
public final class Masks {

    private Masks() {}

    public static final Mask ALWAYS_TRUE = (x, y, z, block) -> true;
    public static final Mask ALWAYS_FALSE = (x, y, z, block) -> false;

    public static final Mask AIR = (x, y, z, block) -> block != null && block.air();
    public static final Mask EXISTING = (x, y, z, block) -> block != null && !block.air();

    public static Mask ofBlock(Block target) {
        return (x, y, z, block) -> block != null && block.compare(target);
    }

    public static Mask ofBlocks(Collection<Block> targets) {
        Set<String> targetNames = new HashSet<>();
        for (Block b : targets) {
            targetNames.add(b.name());
        }
        return (x, y, z, block) -> block != null && targetNames.contains(block.name());
    }
}
