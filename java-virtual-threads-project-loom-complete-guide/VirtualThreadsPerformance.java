/**
 * VirtualThreadsPerformance.java - Performance benchmark
 *
 * Compares virtual threads vs platform threads for I/O-bound workloads.
 * Demonstrates the massive scalability improvement with virtual threads.
 *
 * Run standalone with: java VirtualThreadsPerformance.java (Java 21+)
 * Or as part of Test.java: jbang Test.java
 *
 * @see https://blog.marcnuri.com/java-virtual-threads-project-loom-complete-guide
 */

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

public class VirtualThreadsPerformance {

    private static final int TASK_COUNT = 10_000;
    private static final Duration SIMULATED_IO_DELAY = Duration.ofMillis(100);
    // Simulates a realistic platform thread pool size (typical web server configuration)
    private static final int PLATFORM_THREAD_POOL_SIZE = 200;

    public static void main(String[] args) throws Exception {
        System.out.println("=== Virtual Threads vs Platform Threads Benchmark ===\n");
        System.out.printf("Tasks: %,d | Simulated I/O delay: %dms | Platform pool: %d threads%n%n",
            TASK_COUNT, SIMULATED_IO_DELAY.toMillis(), PLATFORM_THREAD_POOL_SIZE);

        // Warm up
        runWithVirtualThreads(100);
        runWithPlatformThreads(100);

        // Benchmark
        System.out.println("Platform Threads (fixed pool of " + PLATFORM_THREAD_POOL_SIZE + "):");
        long platformTime = runWithPlatformThreads(TASK_COUNT);
        System.out.printf("  Time: %,d ms%n%n", platformTime);

        System.out.println("Virtual Threads:");
        long virtualTime = runWithVirtualThreads(TASK_COUNT);
        System.out.printf("  Time: %,d ms%n%n", virtualTime);

        System.out.printf("Speedup: %.1fx faster with Virtual Threads%n",
            (double) platformTime / virtualTime);
    }

    static long runWithVirtualThreads(int count) throws Exception {
        Instant start = Instant.now();
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            IntStream.range(0, count)
                .forEach(i -> executor.submit(VirtualThreadsPerformance::simulateIO));
        }
        return Duration.between(start, Instant.now()).toMillis();
    }

    static long runWithPlatformThreads(int count) throws Exception {
        Instant start = Instant.now();
        try (ExecutorService executor = Executors.newFixedThreadPool(PLATFORM_THREAD_POOL_SIZE)) {
            IntStream.range(0, count)
                .forEach(i -> executor.submit(VirtualThreadsPerformance::simulateIO));
        }
        return Duration.between(start, Instant.now()).toMillis();
    }

    static void simulateIO() {
        try {
            Thread.sleep(SIMULATED_IO_DELAY);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}