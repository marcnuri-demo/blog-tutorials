import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * ListToMapWithDuplicates.java - Handling duplicate keys with merge function.
 *
 * When the list contains duplicate keys, a merge function is required
 * to specify how to combine the values.
 *
 * @see https://blog.marcnuri.com/java-streams-convert-list-into-map
 */
public class ListToMapWithDuplicates {
    public static void main(String[] args) {
        // List with duplicate names (same project, different versions/forks)
        var repos = List.of(
            new Repository("kubernetes-client", "fabric8io/kubernetes-client", "Kubernetes client v6", false),
            new Repository("kubernetes-client", "fabric8io/kubernetes-client", "Kubernetes client v7", false)
        );

        // Keep the first value encountered (ignore duplicates)
        Map<String, Repository> keepFirst = repos.stream()
            .collect(Collectors.toMap(
                Repository::name,
                Function.identity(),
                (existing, replacement) -> existing
            ));

        // Keep the last value encountered
        Map<String, Repository> keepLast = repos.stream()
            .collect(Collectors.toMap(
                Repository::name,
                Function.identity(),
                (existing, replacement) -> replacement
            ));

        System.out.println("Keep first: " + keepFirst.get("kubernetes-client").description());
        System.out.println("Keep last: " + keepLast.get("kubernetes-client").description());
    }
}
