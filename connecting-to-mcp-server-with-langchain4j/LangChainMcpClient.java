/// usr/bin/env jbang "$0" "$@" ; exit $?
//JAVA 17+
//DEPS dev.langchain4j:langchain4j-mcp:1.22.0-beta32
//DEPS dev.langchain4j:langchain4j-ollama:1.22.0
//DEPS org.slf4j:slf4j-simple:2.0.20
//FILES ./simplelogger.properties

import dev.langchain4j.mcp.McpToolProvider;
import dev.langchain4j.mcp.client.DefaultMcpClient;
import dev.langchain4j.mcp.client.McpClient;
import dev.langchain4j.mcp.client.transport.http.StreamableHttpMcpTransport;
import dev.langchain4j.mcp.client.transport.stdio.StdioMcpTransport;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.tool.ToolArgumentsErrorHandler;
import dev.langchain4j.service.tool.ToolExecutionErrorHandler;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.file.Files;
import java.time.Duration;
import java.util.List;

// Lists the tools of the Kubernetes MCP Server over STDIO and over Streamable HTTP:
//   jbang ./LangChainMcpClient.java
// Runs the assistant example with a local Ollama model (ollama pull qwen3:4b-instruct)
// against the cluster of your current kubeconfig context:
//   jbang ./LangChainMcpClient.java --assistant
public final class LangChainMcpClient {

  private static final String NPX = System.getProperty("os.name").toLowerCase().contains("win") ? "npx.cmd" : "npx";

  static McpClient initStdioClient(String... command) {
    return DefaultMcpClient.builder()
      // Optional client name to identify with the server, defaults to "langchain4j"
      .clientName("blog.marcnuri.com")
      // Optional timeout for each individual tool execution, defaults to 60 seconds
      .toolExecutionTimeout(Duration.ofSeconds(30))
      .transport(StdioMcpTransport.builder()
        // The command that starts the MCP server
        .command(List.of(command))
        // Optional, log the MCP messages (DEBUG level of the "MCP" logger)
        .logEvents(true)
        .build())
      .build();
  }

  static McpClient initHttpClient(String url) {
    return DefaultMcpClient.builder()
      .clientName("blog.marcnuri.com")
      .toolExecutionTimeout(Duration.ofSeconds(30))
      // Skip protocol detection: over HTTP, the Kubernetes MCP Server speaks 2026-07-28 only with stateless = true
      .protocolVersion("2025-11-25")
      .transport(StreamableHttpMcpTransport.builder()
        // The URL of the MCP server's endpoint
        .url(url)
        // Optional, log the MCP requests and responses (INFO level of the "MCP" logger)
        .logRequests(true)
        .logResponses(true)
        .build())
      .build();
  }

  interface Assistant {
    String chat(String userMessage);
  }

  static Assistant assistant(McpClient client) {
    return AiServices.builder(Assistant.class)
      .chatModel(OllamaChatModel.builder()
        .baseUrl("http://localhost:11434")
        .modelName("qwen3:4b-instruct")
        .timeout(Duration.ofMinutes(5))
        .build())
      .toolProvider(McpToolProvider.builder().mcpClients(client).build())
      .toolArgumentsErrorHandler(ToolArgumentsErrorHandler.sendExceptionMessageToLlm())
      .toolExecutionErrorHandler(ToolExecutionErrorHandler.failInvocationUnlessVisibleToLlm())
      .build();
  }

  public static void main(String[] args) throws Exception {
    System.out.println("Starting the Kubernetes MCP Server over STDIO...");
    try (var client = initStdioClient(NPX, "-y", "kubernetes-mcp-server@latest")) {
      if (args.length > 0 && args[0].equals("--assistant")) {
        final var assistant = assistant(client);
        System.out.println(assistant.chat("Run a Pod with the image marcnuri/chuck-norris and expose port 8080"));
        System.out.println(assistant.chat("List all pods in my cluster and output their name and namespace as a Markdown table"));
        return;
      }
      System.out.println("Available tools:");
      client.listTools().forEach(t -> System.out.println(" - " + t.name()));
    }
    System.out.println("Starting the Kubernetes MCP Server over Streamable HTTP...");
    // HTTP mode is only configurable through a TOML file (no --port / --bind-address flags)
    final var config = Files.createTempFile("kubernetes-mcp-server", ".toml");
    config.toFile().deleteOnExit();
    Files.writeString(config, "port = \"8080\"\nbind_address = \"127.0.0.1\"\n");
    final var server = new ProcessBuilder(NPX, "-y", "kubernetes-mcp-server@latest", "--config", config.toString())
      .inheritIO()
      .start();
    try {
      waitForPort("127.0.0.1", 8080, Duration.ofSeconds(60));
      try (var client = initHttpClient("http://127.0.0.1:8080/mcp")) {
        System.out.println("Available tools:");
        client.listTools().forEach(t -> System.out.println(" - " + t.name()));
      }
    } finally {
      // npx starts the server as a child process: stop it too
      server.descendants().forEach(ProcessHandle::destroy);
      server.destroy();
    }
  }

  private static void waitForPort(String host, int port, Duration timeout) throws InterruptedException {
    final long deadline = System.currentTimeMillis() + timeout.toMillis();
    while (System.currentTimeMillis() < deadline) {
      try (var socket = new Socket()) {
        socket.connect(new InetSocketAddress(host, port), 1000);
        return;
      } catch (Exception e) {
        Thread.sleep(200);
      }
    }
    throw new IllegalStateException("Timeout waiting for port " + port);
  }
}
