/// usr/bin/env jbang "$0" "$@" ; exit $?
//JAVA 21+
//DEPS dev.langchain4j:langchain4j-mcp:1.0.0-beta2
//DEPS dev.langchain4j:langchain4j-google-ai-gemini:1.0.0-beta1
//DEPS dev.langchain4j:langchain4j-github-models:1.0.0-beta2
//DEPS org.slf4j:slf4j-simple:2.0.17
//FILES ./simplelogger.properties

import dev.langchain4j.mcp.McpToolProvider;
import dev.langchain4j.mcp.client.DefaultMcpClient;
import dev.langchain4j.mcp.client.McpClient;
import dev.langchain4j.mcp.client.transport.http.HttpMcpTransport;
import dev.langchain4j.mcp.client.transport.stdio.StdioMcpTransport;
import dev.langchain4j.model.github.GitHubModelsChatModel;
import dev.langchain4j.service.AiServices;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.time.Duration;
import java.util.Arrays;

// Run with the following command
// GITHUB_TOKEN=ghp_YOUR_TOKEN jbang ./LangChainMcpClient.java
// Or with the following command to start the assistant integration example (you need a Kubernetes cluster .kube/context)
// GITHUB_TOKEN=ghp_YOUR_TOKEN jbang ./LangChainMcpClient.java --assistant
public final class LangChainMcpClient {

  private static final String NPX = System.getProperty("os.name").toLowerCase().contains("win") ? "npx.cmd" : "npx";

  private static McpClient initStdioClient(String... command) {
    return new DefaultMcpClient.Builder()
      // Optional client name to identify with the server, defaults to "langchain4j"
      .clientName("blog.marcnuri.com")
      // Optional MCP Protocol version, defaults to 2024-11-05
      .protocolVersion("2024-11-05")
      // Optional timeout for each individual tool execution, defaults to 60 seconds
      .toolExecutionTimeout(Duration.ofSeconds(10))
      // STDIO transport
      .transport(new StdioMcpTransport.Builder()
        // The command to execute the MCP server
        .command(Arrays.asList(command))
        // Optional, should the MCP server communication events be logged to the logger
        // Check simplelogger.properties to enable output to the console
        .logEvents(true)
        .build())
      .build();
  }

  private static McpClient initSseClient(String sseUrl) {
    return new DefaultMcpClient.Builder()
      // Optional client name to identify with the server, defaults to "langchain4j"
      .clientName("blog.marcnuri.com")
      // Optional MCP Protocol version, defaults to 2024-11-05
      .protocolVersion("2024-11-05")
      // Optional timeout for each individual tool execution, defaults to 60 seconds
      .toolExecutionTimeout(Duration.ofSeconds(10))
      // SSE transport
      .transport(new HttpMcpTransport.Builder()
        // The URL to connect to the MCP server
        .sseUrl(sseUrl)
        // Optional HTTP connect, read, and write timeouts, defaults to 60 seconds
        .timeout(Duration.ofSeconds(10))
        // Optional, should the MCP server requests be logged to the logger
        // Check simplelogger.properties to enable output to the console
        .logRequests(true)
        // Optional, should the MCP server responses be logged to the logger
        // Check simplelogger.properties to enable output to the console
        .logResponses(true)
        .build())
      .build();
  }

  public static void main(String[] args) {
    try {
      checkRequirements();
      System.out.println("Starting kubernetes-mcp-server in STDIO mode...");
      try (var stdioClient = initStdioClient(NPX, "-y", "kubernetes-mcp-server@latest")) {
        System.out.println("Available tools:");
        stdioClient.listTools().stream()
          .map(t -> " - " + t.name())
          .forEach(System.out::println);
        if (args.length > 0 && args[0].equals("--assistant")) {
          final var assistant = assistantIntegrationExample(stdioClient);
          System.out.println(assistant.chat("Run a Pod with the image marcnuri/chuck-norris and expose port 8080"));
          System.out.println(assistant.chat("List the Pods running in my cluster as a markdown table"));
        }
      }
      System.out.println("Starting kubernetes-mcp-server in SSE mode...");
      // Start the MCP server in a separate process
      final var process = new ProcessBuilder(NPX, "-y", "kubernetes-mcp-server@latest", "--sse-port=8080")
        .inheritIO()
        .start();
      waitForPort("localhost", 8080, Duration.ofSeconds(10));
      try (var stdioClient = initSseClient("http://localhost:8080/sse")) {
        System.out.println("Available tools:");
        stdioClient.listTools().stream()
          .map(t -> " - " + t.name())
          .forEach(System.out::println);
      } finally {
        killProcess(ProcessHandle.of(process.pid()).orElseThrow());
      }
    } catch (Exception e) {
      System.err.println("LangChain MCP Client failed: " + e.getMessage());
    }
  }

  private interface Assistant {
    String chat(String userMessage);
  }

  private static Assistant assistantIntegrationExample(McpClient client) {
    return AiServices.builder(Assistant.class)
      // A bug in Google's API server prevents the use of the GoogleAiGeminiChatModel with tools
      // --* GenerateContentRequest.tools[0].function_declarations[0].parameters.properties[params].properties: should be non-empty for OBJECT type--
//      .chatLanguageModel(GoogleAiGeminiChatModel.builder()
//        .apiKey(System.getenv("GOOGLE_API_KEY"))
//        .modelName("gemini-1.5-flash")
//        .build())
      .chatLanguageModel(GitHubModelsChatModel.builder()
        .gitHubToken(System.getenv("GITHUB_TOKEN"))
        .modelName("gpt-4o-mini")
        .build())
      .toolProvider(McpToolProvider.builder().mcpClients(client).build())
      .build();
  }

  private static void checkRequirements() {
    // Check if npx is available by running npx --version
    try {
      new ProcessBuilder(NPX, "--version").start().waitFor();
    } catch (Exception e) {
      throw new RuntimeException("npx is required to run the LangChain MCP server");
    }
  }

  private static void waitForPort(String host, int port, Duration timeout) throws InterruptedException {
    long start = System.currentTimeMillis();
    while (System.currentTimeMillis() - start < timeout.toMillis()) {
      try (Socket socket = new Socket()) {
        socket.connect(new InetSocketAddress(host, port), 1000);
        return;
      } catch (Exception e) {
        Thread.sleep(100);
      }
    }
    throw new RuntimeException("Timeout waiting for port " + port);
  }

  private static void killProcess(ProcessHandle process) {
    process.children().forEach(LangChainMcpClient::killProcess);
    process.destroyForcibly();
    try {
      if (System.getProperty("os.name").toLowerCase().contains("win")) {
        Runtime.getRuntime().exec(new String[]{"taskkill.exe", "/T", "/F", "/PID", "" + process.pid()});
      } else {
        Runtime.getRuntime().exec(new String[]{"kill", "-9 ", "" + process.pid()});
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}




