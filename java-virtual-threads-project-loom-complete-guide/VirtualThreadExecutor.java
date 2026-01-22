/**
 * VirtualThreadExecutor.java - Method 3: Executors.newVirtualThreadPerTaskExecutor()
 *
 * Production-ready approach using ExecutorService.
 *
 * @see https://blog.marcnuri.com/java-virtual-threads-project-loom-complete-guide
 */

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

public class VirtualThreadExecutor {
    public static void main(String[] args) {
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            IntStream.range(0, 10_000).forEach(i -> {
                executor.submit(() -> {
                    Thread.sleep(1000);
                    return i;
                });
            });
        } // executor.close() is called implicitly, waits for tasks to complete
        System.out.println("All tasks completed");
    }
}