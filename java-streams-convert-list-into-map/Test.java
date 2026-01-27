///usr/bin/env jbang "$0" "$@" ; exit $?
//JAVA 21+
//DEPS org.assertj:assertj-core:3.27.7
//SOURCES ./Repository.java
//SOURCES ./ListToMapBasic.java
//SOURCES ./ListToMapWithDuplicates.java
//SOURCES ./ListToMapOrdered.java
//SOURCES ./ListToMapUnmodifiable.java
//SOURCES ./ListToMapNullHandling.java

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test script to verify Java Streams List to Map examples work correctly.
 *
 * Run with: jbang Test.java
 *
 * @see https://blog.marcnuri.com/java-streams-convert-list-into-map
 */
@SuppressWarnings({"CallToPrintStackTrace"})
public final class Test {
    public static void main(String[] args) {
        try {
            testListToMapBasic();
            testListToMapWithDuplicates();
            testListToMapOrdered();
            testListToMapUnmodifiable();
            testListToMapNullHandling();

            System.out.println("\n=== All tests passed ===");
            System.exit(0);
        } catch (Exception | AssertionError ex) {
            ex.printStackTrace();
            System.exit(1);
        }
    }

    private static void testListToMapBasic() {
        System.out.println("=== Testing ListToMapBasic ===");
        String output = captureOutput(() -> ListToMapBasic.main(null));
        System.out.println(output);

        assertThat(output)
            .contains("Basic List to Map conversion:")
            .contains("kubernetes-client")
            .contains("jkube");
        System.out.println("PASSED\n");
    }

    private static void testListToMapWithDuplicates() {
        System.out.println("=== Testing ListToMapWithDuplicates ===");
        String output = captureOutput(() -> ListToMapWithDuplicates.main(null));
        System.out.println(output);

        assertThat(output)
            .contains("Keep first: Kubernetes client v6")
            .contains("Keep last: Kubernetes client v7");
        System.out.println("PASSED\n");
    }

    private static void testListToMapOrdered() {
        System.out.println("=== Testing ListToMapOrdered ===");
        String output = captureOutput(() -> ListToMapOrdered.main(null));
        System.out.println(output);

        assertThat(output)
            .contains("Insertion order (LinkedHashMap):")
            .contains("Alphabetical order (TreeMap):")
            .contains("Reverse order:");
        System.out.println("PASSED\n");
    }

    private static void testListToMapUnmodifiable() {
        System.out.println("=== Testing ListToMapUnmodifiable ===");
        String output = captureOutput(() -> ListToMapUnmodifiable.main(null));
        System.out.println(output);

        assertThat(output)
            .contains("Created immutable map with")
            .contains("Correctly threw UnsupportedOperationException");
        System.out.println("PASSED\n");
    }

    private static void testListToMapNullHandling() {
        System.out.println("=== Testing ListToMapNullHandling ===");
        String output = captureOutput(() -> ListToMapNullHandling.main(null));
        System.out.println(output);

        assertThat(output)
            .contains("Demonstrating null value handling")
            .contains("Workaround 1")
            .contains("Workaround 2")
            .contains("Workaround 3");
        System.out.println("PASSED\n");
    }

    private static String captureOutput(Runnable runnable) {
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
}
