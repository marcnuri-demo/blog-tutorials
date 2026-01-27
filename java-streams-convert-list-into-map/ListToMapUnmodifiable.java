import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * ListToMapUnmodifiable.java - Creating immutable maps with toUnmodifiableMap().
 *
 * Java 10+ provides Collectors.toUnmodifiableMap() to create immutable maps directly.
 * Any attempt to modify the resulting map throws UnsupportedOperationException.
 *
 * @see https://blog.marcnuri.com/java-streams-convert-list-into-map
 */
public class ListToMapUnmodifiable {
    public static void main(String[] args) {
        var repos = Repository.sampleData();

        // Create an unmodifiable/immutable map (Java 10+)
        Map<String, Repository> immutableMap = repos.stream()
            .collect(Collectors.toUnmodifiableMap(
                Repository::name,
                Function.identity()
            ));

        System.out.println("Created immutable map with " + immutableMap.size() + " entries");

        // Attempting to modify throws UnsupportedOperationException
        try {
            immutableMap.put("new-repo", new Repository("new-repo", "test/new", "Test", false));
            System.out.println("ERROR: Should have thrown exception");
        } catch (UnsupportedOperationException e) {
            System.out.println("Correctly threw UnsupportedOperationException when trying to modify");
        }
    }
}
