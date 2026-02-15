///usr/bin/env jbang "$0" "$@" ; exit $?
//JAVA 17+
//DEPS org.springframework:spring-context:6.2.3
//DEPS org.assertj:assertj-core:3.27.7
//SOURCES ./SingletonVsPrototype.java
//SOURCES ./PrototypeInSingleton.java
//SOURCES ./ObjectProviderSolution.java
//SOURCES ./ScopedProxySolution.java

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test script to verify Spring Bean Scopes examples work correctly.
 *
 * Run with: jbang Test.java
 *
 * @see https://blog.marcnuri.com/spring-bean-scopes-guide-to-understand-the-different-bean-scopes
 * @see https://blog.marcnuri.com/spring-bean-scopes-singleton-prototypes
 */
@SuppressWarnings("CallToPrintStackTrace")
public final class Test {
    public static void main(String[] args) {
        try {
            testSingletonVsPrototype();
            testPrototypeInSingleton();
            testObjectProviderSolution();
            testScopedProxySolution();

            System.out.println("\n=== All tests passed ===");
            System.exit(0);
        } catch (Exception | AssertionError ex) {
            ex.printStackTrace();
            System.exit(1);
        }
    }

    private static void testSingletonVsPrototype() {
        System.out.println("=== Testing SingletonVsPrototype ===");
        String output = captureOutput(() -> SingletonVsPrototype.main(null));
        System.out.println(output);

        assertThat(output)
            .contains("Singleton - same instance: true")
            .contains("Prototype - same instance: false");
        System.out.println("PASSED\n");
    }

    private static void testPrototypeInSingleton() {
        System.out.println("=== Testing PrototypeInSingleton ===");
        String output = captureOutput(() -> PrototypeInSingleton.main(null));
        System.out.println(output);

        assertThat(output)
            .contains("Singleton - same instance: true")
            .contains("Injected prototype - same instance: true");
        System.out.println("PASSED\n");
    }

    private static void testObjectProviderSolution() {
        System.out.println("=== Testing ObjectProviderSolution ===");
        String output = captureOutput(() -> ObjectProviderSolution.main(null));
        System.out.println(output);

        assertThat(output)
            .contains("ObjectProvider - same instance: false")
            .contains("UUID 1:")
            .contains("UUID 2:");
        System.out.println("PASSED\n");
    }

    private static void testScopedProxySolution() {
        System.out.println("=== Testing ScopedProxySolution ===");
        String output = captureOutput(() -> ScopedProxySolution.main(null));
        System.out.println(output);

        assertThat(output)
            .contains("Scoped proxy - same UUID: false")
            .contains("UUID 1:")
            .contains("UUID 2:")
            .contains("Is CGLIB proxy: true");
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
