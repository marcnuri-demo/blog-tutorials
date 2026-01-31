///usr/bin/env jbang "$0" "$@" ; exit $?
//JAVA 17+

/**
 * RecordBasics.java - Basic record declaration and usage
 *
 * @see https://blog.marcnuri.com/java-records-complete-guide
 */
public class RecordBasics {

    // Simple record representing a blog author
    public record Author(String name, String email) {}

    // Record representing a blog post
    public record BlogPost(String slug, String title, Author author) {}

    public static void main(String[] args) {
        // Create instances using the canonical constructor
        Author marc = new Author("Marc Nuri", "author@blog.marcnuri.com");
        BlogPost post = new BlogPost("java-records-complete-guide", "Java Records Complete Guide", marc);

        // Access components using accessor methods
        System.out.println("Author: " + marc.name());
        System.out.println("Email: " + marc.email());
        System.out.println("Post slug: " + post.slug());

        // Records implement equals() based on components
        Author samePerson = new Author("Marc Nuri", "author@blog.marcnuri.com");
        System.out.println("Authors equal: " + marc.equals(samePerson)); // true

        // Useful toString() for free
        System.out.println(post); // BlogPost[slug=java-records-complete-guide, title=Java Records Complete Guide, author=Author[name=Marc Nuri, email=author@blog.marcnuri.com]]
    }
}
