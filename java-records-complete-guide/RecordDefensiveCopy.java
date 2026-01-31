///usr/bin/env jbang "$0" "$@" ; exit $?
//JAVA 17+

import java.util.ArrayList;
import java.util.List;

/**
 * RecordDefensiveCopy.java - Defensive copying for mutable components
 *
 * @see https://blog.marcnuri.com/java-records-complete-guide
 */
public class RecordDefensiveCopy {

    // Record without defensive copy - demonstrates the mutability trap
    public record UnsafeBlog(String name, List<String> posts) {}

    // Record with defensive copy - proper immutability
    public record SafeBlog(String name, List<String> posts) {
        public SafeBlog {
            posts = List.copyOf(posts); // Immutable copy
        }
    }

    public static void main(String[] args) {
        // Demonstrate the mutability trap
        System.out.println("=== Mutability Trap ===");
        List<String> posts = new ArrayList<>();
        posts.add("java-records-complete-guide");
        posts.add("java-virtual-threads-project-loom-complete-guide");

        UnsafeBlog unsafeBlog = new UnsafeBlog("blog.marcnuri.com", posts);
        System.out.println("Before: " + unsafeBlog);

        // The original list can still be modified!
        posts.add("java-pattern-matching-for-instanceof");
        System.out.println("After adding to original list: " + unsafeBlog);

        // Even the accessor returns the mutable list
        unsafeBlog.posts().clear();
        System.out.println("After clearing via accessor: " + unsafeBlog);

        // Demonstrate the defensive copy solution
        System.out.println("\n=== Defensive Copy Solution ===");
        List<String> safePosts = new ArrayList<>();
        safePosts.add("java-records-complete-guide");
        safePosts.add("java-virtual-threads-project-loom-complete-guide");

        SafeBlog safeBlog = new SafeBlog("blog.marcnuri.com", safePosts);
        System.out.println("Before: " + safeBlog);

        // Modifying the original list has no effect
        safePosts.add("java-pattern-matching-for-instanceof");
        System.out.println("After adding to original list: " + safeBlog);

        // The accessor returns an unmodifiable list
        try {
            safeBlog.posts().clear();
        } catch (UnsupportedOperationException e) {
            System.out.println("Cannot modify via accessor: " + e.getClass().getSimpleName());
        }
        System.out.println("Blog remains unchanged: " + safeBlog);
    }
}
