package fr.maw.async;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * High performance asynchronous execution engine for MAW calculations and background chunk preparation.
 */
public final class MawAsyncEngine {

    private final ExecutorService executorService;
    private final AtomicInteger activeTasks = new AtomicInteger();

    public MawAsyncEngine(int workerThreads) {
        // Use Java 21+ Virtual Threads executor for zero thread contention and massive concurrency
        this.executorService = Executors.newVirtualThreadPerTaskExecutor();
    }

    /**
     * Submits an asynchronous task to the calculation engine.
     */
    public <T> CompletableFuture<T> supplyAsync(Callable<T> task) {
        CompletableFuture<T> future = new CompletableFuture<>();
        activeTasks.incrementAndGet();
        try {
            executorService.submit(() -> {
                try {
                    T result = task.call();
                    future.complete(result);
                } catch (Throwable t) {
                    future.completeExceptionally(t);
                } finally {
                    activeTasks.decrementAndGet();
                }
            });
        } catch (RejectedExecutionException e) {
            activeTasks.decrementAndGet();
            throw e;
        }
        return future;
    }

    /**
     * Submits an asynchronous runnable.
     */
    public CompletableFuture<Void> runAsync(Runnable runnable) {
        activeTasks.incrementAndGet();
        try {
            return CompletableFuture.runAsync(() -> {
                try {
                    runnable.run();
                } finally {
                    activeTasks.decrementAndGet();
                }
            }, executorService);
        } catch (RejectedExecutionException e) {
            activeTasks.decrementAndGet();
            throw e;
        }
    }

    /**
     * Whether a task submitted to this engine is still running (computing an operation).
     */
    public boolean hasActiveTasks() {
        return activeTasks.get() > 0;
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
