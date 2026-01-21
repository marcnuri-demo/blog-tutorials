/**
 * VirtualThreadsHttpClient.java - Real-world HTTP client example
 *
 * Demonstrates using virtual threads to fetch multiple URLs concurrently.
 * Shows practical usage with Java's built-in HTTP client.
 *
 * @see https://blog.marcnuri.com/java-virtual-threads-project-loom-complete-guide
 */

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class VirtualThreadsHttpClient {

    private static final HttpClient httpClient = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(10))
        .build();

    private static final List<String> URLS = List.of(
        "https://api.github.com/users/octocat",
        "https://api.github.com/repos/openjdk/jdk",
        "https://api.github.com/orgs/spring-projects",
        "https://api.github.com/repos/kubernetes/kubernetes",
        "https://api.github.com/repos/docker/compose",
        "https://api.github.com/users/torvalds",
        "https://api.github.com/repos/microsoft/vscode",
        "https://api.github.com/repos/facebook/react"
    );

    public static void main(String[] args) throws Exception {
        System.out.println("=== Virtual Threads HTTP Client Demo ===\n");
        System.out.println("Fetching " + URLS.size() + " URLs concurrently using virtual threads...\n");

        long start = System.currentTimeMillis();

        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            // Submit all fetch tasks
            List<Future<FetchResult>> futures = new ArrayList<>();
            for (String url : URLS) {
                futures.add(executor.submit(() -> fetchUrl(url)));
            }

            // Collect results
            int successCount = 0;
            int errorCount = 0;
            long totalBytes = 0;

            for (Future<FetchResult> future : futures) {
                FetchResult result = future.get();
                if (result.success) {
                    successCount++;
                    totalBytes += result.contentLength;
                    System.out.printf("  [OK] %s - %d bytes in %d ms%n",
                        shortenUrl(result.url), result.contentLength, result.durationMs);
                } else {
                    errorCount++;
                    System.out.printf("  [ERROR] %s - %s%n",
                        shortenUrl(result.url), result.error);
                }
            }

            long elapsed = System.currentTimeMillis() - start;

            System.out.println("\n--- Summary ---");
            System.out.println("  Total URLs: " + URLS.size());
            System.out.println("  Successful: " + successCount);
            System.out.println("  Errors: " + errorCount);
            System.out.println("  Total bytes: " + totalBytes);
            System.out.println("  Total time: " + elapsed + " ms");
            System.out.println("\n  Note: All requests ran concurrently!");
            System.out.println("  Without virtual threads (sequential): would take ~" +
                (URLS.size() * 500) + "+ ms");
        }
    }

    private static FetchResult fetchUrl(String url) {
        long start = System.currentTimeMillis();
        try {
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("User-Agent", "Java Virtual Threads Demo")
                .header("Accept", "application/json")
                .GET()
                .timeout(Duration.ofSeconds(30))
                .build();

            HttpResponse<String> response = httpClient.send(request,
                HttpResponse.BodyHandlers.ofString());

            long duration = System.currentTimeMillis() - start;

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                return new FetchResult(url, true, response.body().length(), duration, null);
            } else {
                return new FetchResult(url, false, 0, duration,
                    "HTTP " + response.statusCode());
            }
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - start;
            return new FetchResult(url, false, 0, duration, e.getMessage());
        }
    }

    private static String shortenUrl(String url) {
        return url.replace("https://api.github.com/", "github:");
    }

    record FetchResult(
        String url,
        boolean success,
        int contentLength,
        long durationMs,
        String error
    ) {}
}
