package fr.maw.pattern;

import net.minestom.server.instance.block.Block;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * A probabilistic pattern that picks blocks according to assigned weights (e.g. 60% stone, 40% dirt).
 */
public final class RandomPattern implements Pattern {

    public record WeightedBlock(Block block, double weight) {}

    private final Block[] blocks;
    private final double[] cumulativeWeights;
    private final double totalWeight;

    public RandomPattern(List<WeightedBlock> entries) {
        if (entries == null || entries.isEmpty()) {
            throw new IllegalArgumentException("Entries cannot be null or empty");
        }

        this.blocks = new Block[entries.size()];
        this.cumulativeWeights = new double[entries.size()];

        double sum = 0.0;
        for (int i = 0; i < entries.size(); i++) {
            WeightedBlock wb = entries.get(i);
            if (wb.weight() <= 0) {
                throw new IllegalArgumentException("Weight must be positive, got " + wb.weight());
            }
            sum += wb.weight();
            this.blocks[i] = wb.block();
            this.cumulativeWeights[i] = sum;
        }
        this.totalWeight = sum;
    }

    @Override
    public Block apply(int x, int y, int z, Block currentBlock) {
        double r = ThreadLocalRandom.current().nextDouble() * totalWeight;
        int index = Arrays.binarySearch(cumulativeWeights, r);
        if (index < 0) {
            index = -index - 1;
        }
        if (index >= blocks.length) {
            index = blocks.length - 1;
        }
        return blocks[index];
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private final List<WeightedBlock> entries = new ArrayList<>();

        public Builder add(Block block, double weight) {
            entries.add(new WeightedBlock(block, weight));
            return this;
        }

        public RandomPattern build() {
            return new RandomPattern(entries);
        }
    }

    @Override
    public String toString() {
        return "RandomPattern[totalWeight=" + totalWeight + ", entries=" + blocks.length + "]";
    }
}
