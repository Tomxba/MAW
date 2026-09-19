package fr.maw;

import java.util.Objects;

/**
 * Configuration for the MAW (Minestom Async WorldEdit) engine.
 */
public final class MawConfig {

    private final int maxBlocksPerOperation;
    private final int maxHistoryPerPlayer;
    private final int timeBudgetPerTickMs;
    private final int maxChunksPerTick;
    private final boolean defaultUpdatePhysics;
    private final boolean defaultManageEntities;
    private final String wandItemNamespace;
    private final int asyncWorkerThreads;

    private MawConfig(Builder builder) {
        this.maxBlocksPerOperation = builder.maxBlocksPerOperation;
        this.maxHistoryPerPlayer = builder.maxHistoryPerPlayer;
        this.timeBudgetPerTickMs = builder.timeBudgetPerTickMs;
        this.maxChunksPerTick = builder.maxChunksPerTick;
        this.defaultUpdatePhysics = builder.defaultUpdatePhysics;
        this.defaultManageEntities = builder.defaultManageEntities;
        this.wandItemNamespace = Objects.requireNonNull(builder.wandItemNamespace, "wandItemNamespace cannot be null");
        this.asyncWorkerThreads = builder.asyncWorkerThreads;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static MawConfig defaultConfig() {
        return builder().build();
    }

    public int maxBlocksPerOperation() {
        return maxBlocksPerOperation;
    }

    public int maxHistoryPerPlayer() {
        return maxHistoryPerPlayer;
    }

    public int timeBudgetPerTickMs() {
        return timeBudgetPerTickMs;
    }

    public int maxChunksPerTick() {
        return maxChunksPerTick;
    }

    public boolean defaultUpdatePhysics() {
        return defaultUpdatePhysics;
    }

    public boolean defaultManageEntities() {
        return defaultManageEntities;
    }

    public String wandItemNamespace() {
        return wandItemNamespace;
    }

    public int asyncWorkerThreads() {
        return asyncWorkerThreads;
    }

    public static final class Builder {
        private int maxBlocksPerOperation = 10_000_000;
        private int maxHistoryPerPlayer = 20;
        private int timeBudgetPerTickMs = 5;
        private int maxChunksPerTick = 20;
        private boolean defaultUpdatePhysics = false;
        private boolean defaultManageEntities = true;
        private String wandItemNamespace = "minecraft:wooden_axe";
        private int asyncWorkerThreads = Math.max(2, Runtime.getRuntime().availableProcessors() - 1);

        public Builder maxBlocksPerOperation(int maxBlocksPerOperation) {
            this.maxBlocksPerOperation = maxBlocksPerOperation;
            return this;
        }

        public Builder maxHistoryPerPlayer(int maxHistoryPerPlayer) {
            this.maxHistoryPerPlayer = maxHistoryPerPlayer;
            return this;
        }

        public Builder timeBudgetPerTickMs(int timeBudgetPerTickMs) {
            this.timeBudgetPerTickMs = timeBudgetPerTickMs;
            return this;
        }

        public Builder maxChunksPerTick(int maxChunksPerTick) {
            this.maxChunksPerTick = maxChunksPerTick;
            return this;
        }

        public Builder defaultUpdatePhysics(boolean defaultUpdatePhysics) {
            this.defaultUpdatePhysics = defaultUpdatePhysics;
            return this;
        }

        public Builder defaultManageEntities(boolean defaultManageEntities) {
            this.defaultManageEntities = defaultManageEntities;
            return this;
        }

        public Builder wandItemNamespace(String wandItemNamespace) {
            this.wandItemNamespace = wandItemNamespace;
            return this;
        }

        public Builder asyncWorkerThreads(int asyncWorkerThreads) {
            this.asyncWorkerThreads = asyncWorkerThreads;
            return this;
        }

        public MawConfig build() {
            return new MawConfig(this);
        }
    }
}
