package fr.maw.async;

import java.util.concurrent.*;

/**
 * High performance asynchronous execution engine for MAW calculations and background chunk preparation.
 */
public final class MawAsyncEngine {

    private final ExecutorService executorService;

    public MawAsyncEngine(int workerThreads) {
        // Use Java 21+ Virtual Threads executor for zero thread contention and massive concurrency
        this.executorService = Executors.newVirtualThreadPerTaskExecutor();
    }

    /**
     * Submits an asynchronous task to the calculation engine.
     */
    public <T> CompletableFuture<T> supplyAsync(Callable<T> task) {
        CompletableFuture<T> future = new CompletableFuture<>();
        executorService.submit(() -> {
            try {
                T result = task.call();
                future.complete(result);
            } catch (Throwable t) {
                future.completeExceptionally(t);
            }
        });
        return future;
    }

    /**
     * Submits an asynchronous runnable.
     */
    public CompletableFuture<Void> runAsync(Runnable runnable) {
        return CompletableFuture.runAsync(runnable, executorService);
    }

    /**
     * Shuts down the async executor.
     */
    public void shutdown() {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(3, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
