/**
 * VirtualThreadsBasics.java - Four ways to create virtual threads
 *
 * Demonstrates the four main methods to create virtual threads in Java 21+.
 *
 * @see https://blog.marcnuri.com/java-virtual-threads-project-loom-complete-guide
 */

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.IntStream;

public class VirtualThreadsBasics {

    public static void main(String[] args) throws Exception {
        System.out.println("=== Java Virtual Threads: Four Creation Methods ===\n");

        method1_startVirtualThread();
        method2_ofVirtualStart();
        method3_virtualThreadPerTaskExecutor();
        method4_threadFactory();

        System.out.println("\n=== All demonstrations completed ===");
    }

    /**
     * Method 1: Thread.startVirtualThread()
     * The simplest way to start a virtual thread immediately.
     */
    private static void method1_startVirtualThread() throws InterruptedException {
        System.out.println("--- Method 1: Thread.startVirtualThread() ---");

        Thread vt = Thread.startVirtualThread(() -> {
            System.out.println("  Running in: " + Thread.currentThread());
            System.out.println("  Is virtual: " + Thread.currentThread().isVirtual());
            simulateBlockingWork("startVirtualThread", 100);
        });

        vt.join();
        System.out.println("  Thread completed\n");
    }

    /**
     * Method 2: Thread.ofVirtual().start()
     * Builder pattern for more control over thread configuration.
     */
    private static void method2_ofVirtualStart() throws InterruptedException {
        System.out.println("--- Method 2: Thread.ofVirtual().start() ---");

        Thread vt = Thread.ofVirtual()
            .name("my-named-virtual-thread")
            .start(() -> {
                System.out.println("  Thread name: " + Thread.currentThread().getName());
                System.out.println("  Is virtual: " + Thread.currentThread().isVirtual());
                simulateBlockingWork("ofVirtual", 100);
            });

        vt.join();
        System.out.println("  Thread completed\n");
    }

    /**
     * Method 3: Executors.newVirtualThreadPerTaskExecutor()
     * Production-ready approach using ExecutorService.
     */
    private static void method3_virtualThreadPerTaskExecutor() {
        System.out.println("--- Method 3: Executors.newVirtualThreadPerTaskExecutor() ---");

        int taskCount = 10;
        long start = System.currentTimeMillis();

        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            IntStream.range(0, taskCount).forEach(i -> {
                executor.submit(() -> {
                    simulateBlockingWork("executor-task-" + i, 100);
                    return i;
                });
            });
        } // Implicit close() waits for all tasks to complete

        long elapsed = System.currentTimeMillis() - start;
        System.out.println("  " + taskCount + " tasks completed in " + elapsed + " ms");
        System.out.println("  (All ran concurrently, so ~100ms total, not " + (taskCount * 100) + "ms)\n");
    }

    /**
     * Method 4: ThreadFactory for virtual threads
     * Useful when libraries require a ThreadFactory.
     */
    private static void method4_threadFactory() throws InterruptedException {
        System.out.println("--- Method 4: ThreadFactory for virtual threads ---");

        AtomicLong counter = new AtomicLong(1);

        ThreadFactory factory = Thread.ofVirtual()
            .name("worker-", 0)  // Prefix "worker-", starts at 0
            .factory();

        Thread t1 = factory.newThread(() -> {
            System.out.println("  " + Thread.currentThread().getName() + " started");
            simulateBlockingWork("factory-thread-1", 50);
        });

        Thread t2 = factory.newThread(() -> {
            System.out.println("  " + Thread.currentThread().getName() + " started");
            simulateBlockingWork("factory-thread-2", 50);
        });

        t1.start();
        t2.start();
        t1.join();
        t2.join();

        System.out.println("  Both factory-created threads completed\n");
    }

    /**
     * Simulates a blocking operation (like I/O or network call)
     */
    private static void simulateBlockingWork(String taskName, long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted during " + taskName, e);
        }
    }
}
