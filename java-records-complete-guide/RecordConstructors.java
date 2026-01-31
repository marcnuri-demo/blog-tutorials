///usr/bin/env jbang "$0" "$@" ; exit $?
//JAVA 17+

/**
 * RecordConstructors.java - Constructor variations for records
 *
 * @see https://blog.marcnuri.com/java-records-complete-guide
 */
public class RecordConstructors {

    // Record with compact constructor for validation and normalization
    public record BlogUrl(String value) {
        public BlogUrl {
            if (value == null || !value.startsWith("https://")) {
                throw new IllegalArgumentException("Invalid blog URL: " + value);
            }
            // Normalize: remove trailing slash
            value = value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
        }
    }

    // Record with custom constructors
    public record ReadingTime(int minutes, int seconds) {
        public ReadingTime {
            if (minutes < 0 || seconds < 0) {
                throw new IllegalArgumentException("Reading time cannot be negative");
            }
        }

        // Custom constructor for minutes only
        public ReadingTime(int minutes) {
            this(minutes, 0);
        }
    }

    public static void main(String[] args) {
        // Compact constructor with normalization
        BlogUrl url = new BlogUrl("https://blog.marcnuri.com/java-records-complete-guide/");
        System.out.println("Normalized URL: " + url.value());

        // Custom constructor
        ReadingTime time = new ReadingTime(15);
        System.out.println("Reading time: " + time);
    }
}
