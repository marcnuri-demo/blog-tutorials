import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * ListToMapBasic.java - Basic List to Map conversion with no duplicate keys.
 *
 * Demonstrates the simplest way to convert a List into a Map using Collectors.toMap().
 *
 * @see https://blog.marcnuri.com/java-streams-convert-list-into-map
 */
public class ListToMapBasic {
    public static void main(String[] args) {
        var repos = Repository.sampleData();

        Map<String, Repository> repoMap = repos.stream()
            .collect(Collectors.toMap(Repository::name, Function.identity()));

        System.out.println("Basic List to Map conversion:");
        repoMap.forEach((name, repo) ->
            System.out.println("  " + name + " -> " + repo.description()));
    }
}
