///usr/bin/env jbang "$0" "$@" ; exit $?
//JAVA 21+
//DEPS org.assertj:assertj-core:3.27.6
//SOURCES ./VirtualThreadsBasics.java
//SOURCES ./VirtualThreadsHttpClient.java
//SOURCES ./VirtualThreadsPerformance.java

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test script to verify Virtual Threads examples work correctly.
 *
 * @see https://blog.marcnuri.com/java-virtual-threads-project-loom-complete-guide
 */
@SuppressWarnings({"CallToPrintStackTrace"})
public final class Test {
    public static void main(String[] args) {
        try {
            testVirtualThreadsBasics();
            testVirtualThreadsPerformance();
            testVirtualThreadsHttpClient();

            System.out.println("\n=== All tests passed ===");
            System.exit(0);
        } catch (Exception | AssertionError ex) {
            ex.printStackTrace();
            System.exit(1);
        }
    }

    private static void testVirtualThreadsBasics() throws Exception {
        System.out.println("=== Running VirtualThreadsBasics ===\n");
        String output = captureOutput(() -> VirtualThreadsBasics.main(null));
        System.out.println(output);

        assertThat(output)
            .contains("Method 1: Thread.startVirtualThread()")
            .contains("Method 2: Thread.ofVirtual().start()")
            .contains("Method 3: Executors.newVirtualThreadPerTaskExecutor()")
            .contains("Method 4: ThreadFactory for virtual threads")
            .contains("Is virtual: true")
            .contains("All demonstrations completed");
    }

    private static void testVirtualThreadsPerformance() throws Exception {
        System.out.println("\n=== Running VirtualThreadsPerformance ===\n");
        String output = captureOutput(() -> VirtualThreadsPerformance.main(null));
        System.out.println(output);

        assertThat(output)
            .contains("Virtual Threads vs Platform Threads Performance Benchmark")
            .contains("Benchmark: Virtual Threads")
            .contains("Benchmark: Platform Threads")
            .contains("faster");
    }

    private static void testVirtualThreadsHttpClient() throws Exception {
        System.out.println("\n=== Running VirtualThreadsHttpClient ===\n");
        String output = captureOutput(() -> VirtualThreadsHttpClient.main(null));
        System.out.println(output);

        assertThat(output)
            .contains("Virtual Threads HTTP Client")
            .contains("Fetching")
            .contains("Total time:");
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
