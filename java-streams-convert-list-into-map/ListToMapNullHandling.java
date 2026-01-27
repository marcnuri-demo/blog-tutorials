import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * ListToMapNullHandling.java - Handling null values in Collectors.toMap().
 *
 * IMPORTANT: Collectors.toMap() throws NullPointerException when the value mapper
 * returns null. This is because it uses HashMap.merge() internally which doesn't
 * accept null values.
 *
 * This example shows the problem and a workaround.
 *
 * @see https://blog.marcnuri.com/java-streams-convert-list-into-map
 */
public class ListToMapNullHandling {
    public static void main(String[] args) {
        // Repository with null description
        var repos = List.of(
            new Repository("repo1", "owner/repo1", "Has description", false),
            new Repository("repo2", "owner/repo2", null, false) // null description!
        );

        // This would throw NullPointerException:
        // repos.stream().collect(Collectors.toMap(Repository::name, Repository::description));

        System.out.println("Demonstrating null value handling...");

        // Workaround 1: Use forEach with put (accepts null values)
        Map<String, String> mapWithNulls = new HashMap<>();
        repos.forEach(repo -> mapWithNulls.put(repo.name(), repo.description()));
        System.out.println("Workaround 1 (forEach): " + mapWithNulls);

        // Workaround 2: Replace nulls with default value
        Map<String, String> mapWithDefaults = repos.stream()
            .collect(Collectors.toMap(
                Repository::name,
                repo -> repo.description() != null ? repo.description() : "No description"
            ));
        System.out.println("Workaround 2 (default value): " + mapWithDefaults);

        // Workaround 3: Custom collector using Collector.of()
        Map<String, String> customCollector = repos.stream()
            .collect(Collectors.collectingAndThen(
                Collectors.toList(),
                list -> {
                    Map<String, String> map = new HashMap<>();
                    list.forEach(repo -> map.put(repo.name(), repo.description()));
                    return map;
                }
            ));
        System.out.println("Workaround 3 (custom collector): " + customCollector);
    }
}
