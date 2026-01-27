import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * ListToMapOrdered.java - Maintaining key ordering with different Map implementations.
 *
 * Demonstrates:
 * - LinkedHashMap: preserves insertion order from the original list
 * - TreeMap: sorts keys alphabetically or by a custom comparator
 *
 * @see https://blog.marcnuri.com/java-streams-convert-list-into-map
 */
public class ListToMapOrdered {
    public static void main(String[] args) {
        var repos = Repository.sampleData();

        // LinkedHashMap preserves insertion order
        LinkedHashMap<String, Repository> insertionOrder = repos.stream()
            .collect(Collectors.toMap(
                Repository::name,
                Function.identity(),
                (existing, replacement) -> existing,
                LinkedHashMap::new
            ));

        // TreeMap sorts keys alphabetically
        TreeMap<String, Repository> alphabetical = repos.stream()
            .collect(Collectors.toMap(
                Repository::name,
                Function.identity(),
                (existing, replacement) -> existing,
                TreeMap::new
            ));

        // TreeMap with reverse order
        Map<String, Repository> reverseOrder = repos.stream()
            .collect(Collectors.toMap(
                Repository::name,
                Function.identity(),
                (existing, replacement) -> existing,
                () -> new TreeMap<>(Comparator.reverseOrder())
            ));

        System.out.println("Insertion order (LinkedHashMap): " + insertionOrder.keySet());
        System.out.println("Alphabetical order (TreeMap): " + alphabetical.keySet());
        System.out.println("Reverse order: " + reverseOrder.keySet());
    }
}
