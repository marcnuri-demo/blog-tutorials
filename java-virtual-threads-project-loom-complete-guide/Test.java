///usr/bin/env jbang "$0" "$@" ; exit $?
//JAVA 21+
//DEPS org.assertj:assertj-core:3.27.6
//SOURCES ./StartVirtualThread.java
//SOURCES ./OfVirtualStart.java
//SOURCES ./VirtualThreadExecutor.java
//SOURCES ./VirtualThreadFactory.java
//SOURCES ./ConcurrentHttpClient.java
//SOURCES ./VirtualThreadsPerformance.java

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test script to verify Virtual Threads examples work correctly.
 *
 * Run with: jbang Test.java
 *
 * @see https://blog.marcnuri.com/java-virtual-threads-project-loom-complete-guide
 */
@SuppressWarnings({"CallToPrintStackTrace"})
public final class Test {
    public static void main(String[] args) {
        try {
            testStartVirtualThread();
            testOfVirtualStart();
            testVirtualThreadExecutor();
            testVirtualThreadFactory();
            testConcurrentHttpClient();
            testVirtualThreadsPerformance();

            System.out.println("\n=== All tests passed ===");
            System.exit(0);
        } catch (Exception | AssertionError ex) {
            ex.printStackTrace();
            System.exit(1);
        }
    }

    private static void testStartVirtualThread() throws Exception {
        System.out.println("=== Testing StartVirtualThread ===");
        String output = captureOutput(() -> StartVirtualThread.main(null));
        System.out.println(output);

        assertThat(output)
            .contains("Running in:")
            .contains("Is virtual: true");
        System.out.println("PASSED\n");
    }

    private static void testOfVirtualStart() throws Exception {
        System.out.println("=== Testing OfVirtualStart ===");
        String output = captureOutput(() -> OfVirtualStart.main(null));
        System.out.println(output);

        assertThat(output)
            .contains("Thread name: my-virtual-thread");
        System.out.println("PASSED\n");
    }

    private static void testVirtualThreadExecutor() throws Exception {
        System.out.println("=== Testing VirtualThreadExecutor ===");
        String output = captureOutput(() -> VirtualThreadExecutor.main(null));
        System.out.println(output);

        assertThat(output)
            .contains("All tasks completed");
        System.out.println("PASSED\n");
    }

    private static void testVirtualThreadFactory() throws Exception {
        System.out.println("=== Testing VirtualThreadFactory ===");
        String output = captureOutput(() -> VirtualThreadFactory.main(null));
        System.out.println(output);

        assertThat(output)
            .contains("worker-");
        System.out.println("PASSED\n");
    }

    private static void testConcurrentHttpClient() throws Exception {
        System.out.println("=== Testing ConcurrentHttpClient ===");
        String output = captureOutput(() -> ConcurrentHttpClient.main(null));
        System.out.println(output);

        assertThat(output)
            .contains("Received")
            .contains("bytes")
            .contains("Completed")
            .contains("requests in");
        System.out.println("PASSED\n");
    }

    private static void testVirtualThreadsPerformance() throws Exception {
        System.out.println("=== Testing VirtualThreadsPerformance ===");
        String output = captureOutput(() -> VirtualThreadsPerformance.main(null));
        System.out.println(output);

        assertThat(output)
            .contains("Virtual Threads vs Platform Threads Benchmark")
            .contains("Platform Threads (fixed pool of")
            .contains("Virtual Threads:")
            .contains("Speedup:")
            .contains("faster with Virtual Threads");
        System.out.println("PASSED\n");
    }

    private static String captureOutput(ThrowingRunnable runnable) throws Exception {
        PrintStream originalOut = System.out;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (PrintStream capture = new PrintStream(baos)) {
            System.setOut(capture);
            runnable.run();
        } finally {
            System.setOut(originalOut);
        }
        return baos.toString();
    }

    @FunctionalInterface
    interface ThrowingRunnable {
        void run() throws Exception;
    }
}