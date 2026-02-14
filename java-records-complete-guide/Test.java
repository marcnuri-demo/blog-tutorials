///usr/bin/env jbang "$0" "$@" ; exit $?
//JAVA 17+
//SOURCES ./RecordDefensiveCopy.java

/**
 * Test.java - Runs all Java Records examples
 *
 * Run with: jbang Test.java
 *
 * @see https://blog.marcnuri.com/java-records-complete-guide
 */
public class Test {
    public static void main(String[] args) {
        System.out.println("=".repeat(60));
        System.out.println("Running: RecordDefensiveCopy");
        System.out.println("=".repeat(60));
        RecordDefensiveCopy.main(null);

        System.out.println("\n" + "=".repeat(60));
        System.out.println("All examples completed successfully!");
        System.out.println("=".repeat(60));
    }
}
