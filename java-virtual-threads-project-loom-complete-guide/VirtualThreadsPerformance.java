/**
 * VirtualThreadsPerformance.java - Performance benchmark
 *
 * Compares virtual threads vs platform threads for I/O-bound workloads.
 * Demonstrates the massive scalability improvement with virtual threads.
 *
 * @see https://blog.marcnuri.com/java-virtual-threads-project-loom-complete-guide
 */

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class VirtualThreadsPerformance {

    private static final int TASK_COUNT = 10_000;
    private static final int BLOCKING_DURATION_MS = 100;
    private static final int PLATFORM_THREAD_POOL_SIZE = 100;

    public static void main(String[] args) throws Exception {
        System.out.println("=== Virtual Threads vs Platform Threads Performance Benchmark ===\n");
        System.out.println("Configuration:");
        System.out.println("  Tasks: " + TASK_COUNT);
        System.out.println("  Blocking duration per task: " + BLOCKING_DURATION_MS + " ms");
        System.out.println("  Platform thread pool size: " + PLATFORM_THREAD_POOL_SIZE);
        System.out.println();

        // Warmup
        System.out.println("Warming up JVM...");
        runWithVirtualThreads(100);
        runWithPlatformThreads(100);
        System.out.println("Warmup complete.\n");

        // Benchmark virtual threads
        System.out.println("--- Benchmark: Virtual Threads ---");
        long virtualStart = System.currentTimeMillis();
        int virtualCompleted = runWithVirtualThreads(TASK_COUNT);
        long virtualElapsed = System.currentTimeMillis() - virtualStart;
        System.out.println("  Completed: " + virtualCompleted + " tasks");
        System.out.println("  Time: " + virtualElapsed + " ms");
        System.out.println("  Throughput: " + (virtualCompleted * 1000L / virtualElapsed) + " tasks/sec\n");

        // Benchmark platform threads
        System.out.println("--- Benchmark: Platform Threads (pool size: " + PLATFORM_THREAD_POOL_SIZE + ") ---");
        long platformStart = System.currentTimeMillis();
        int platformCompleted = runWithPlatformThreads(TASK_COUNT);
        long platformElapsed = System.currentTimeMillis() - platformStart;
        System.out.println("  Completed: " + platformCompleted + " tasks");
        System.out.println("  Time: " + platformElapsed + " ms");
        System.out.println("  Throughput: " + (platformCompleted * 1000L / platformElapsed) + " tasks/sec\n");

        // Comparison
        System.out.println("--- Results ---");
        double speedup = (double) platformElapsed / virtualElapsed;
        System.out.printf("  Virtual threads were %.1fx faster%n", speedup);
        System.out.println("\n  Theoretical minimum with virtual threads: ~" + BLOCKING_DURATION_MS + " ms");
        System.out.println("  Theoretical minimum with " + PLATFORM_THREAD_POOL_SIZE + " platform threads: ~"
            + (TASK_COUNT * BLOCKING_DURATION_MS / PLATFORM_THREAD_POOL_SIZE) + " ms");
    }

    private static int runWithVirtualThreads(int taskCount) throws InterruptedException {
        AtomicInteger completed = new AtomicInteger(0);
        CountDownLatch latch = new CountDownLatch(taskCount);

        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            for (int i = 0; i < taskCount; i++) {
                executor.submit(() -> {
                    try {
                        simulateBlockingIO();
                        completed.incrementAndGet();
                    } finally {
                        latch.countDown();
                    }
                });
            }
            latch.await();
        }

        return completed.get();
    }

    private static int runWithPlatformThreads(int taskCount) throws InterruptedException {
        AtomicInteger completed = new AtomicInteger(0);
        CountDownLatch latch = new CountDownLatch(taskCount);

        try (ExecutorService executor = Executors.newFixedThreadPool(PLATFORM_THREAD_POOL_SIZE)) {
            for (int i = 0; i < taskCount; i++) {
                executor.submit(() -> {
                    try {
                        simulateBlockingIO();
                        completed.incrementAndGet();
                    } finally {
                        latch.countDown();
                    }
                });
            }
            latch.await();
        }

        return completed.get();
    }

    private static void simulateBlockingIO() {
        try {
            Thread.sleep(BLOCKING_DURATION_MS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
