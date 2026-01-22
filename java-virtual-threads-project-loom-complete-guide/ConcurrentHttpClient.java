/**
 * ConcurrentHttpClient.java - Real-world HTTP client example
 *
 * Demonstrates using virtual threads to fetch multiple URLs concurrently.
 *
 * @see https://blog.marcnuri.com/java-virtual-threads-project-loom-complete-guide
 */

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ConcurrentHttpClient {
    private static final HttpClient client = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(10))
        .build();

    public static void main(String[] args) throws Exception {
        List<String> urls = List.of(
            "https://api.github.com/users/octocat",
            "https://api.github.com/repos/openjdk/jdk",
            "https://api.github.com/orgs/spring-projects",
            "https://api.github.com/users/marcnuri-demo"
        );

        long start = System.currentTimeMillis();

        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            List<Future<String>> futures = urls.stream()
                .map(url -> executor.submit(() -> fetchUrl(url)))
                .toList();

            for (Future<String> future : futures) {
                String response = future.get();
                System.out.println("Received " + response.length() + " bytes");
            }
        }

        long elapsed = System.currentTimeMillis() - start;
        System.out.printf("Completed %d requests in %d ms%n", urls.size(), elapsed);
    }

    private static String fetchUrl(String url) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("User-Agent", "Java Virtual Threads Demo")
            .GET()
            .build();

        HttpResponse<String> response = client.send(request,
            HttpResponse.BodyHandlers.ofString());

        return response.body();
    }
}