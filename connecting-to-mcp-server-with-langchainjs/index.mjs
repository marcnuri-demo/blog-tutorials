// LangChain.js MCP Adapters: Connect Agents to MCP Servers
// https://blog.marcnuri.com/connecting-to-mcp-server-with-langchainjs
//
// node index.mjs              connects to the Kubernetes MCP Server, lists its tools and exits
// node index.mjs --assistant  also runs the agent with a local Ollama model (ollama pull qwen3:4b-instruct)
import {createAgent} from 'langchain';
import {ChatOllama} from '@langchain/ollama';
import {MCPAdapter} from '@langchain/mcp-adapters';

const adapter = new MCPAdapter({
  servers: {
    kubernetes: {
      command: 'npx',
      args: ['-y', 'kubernetes-mcp-server@latest']
    }
    // Streamable HTTP alternative (start the server with `npx -y kubernetes-mcp-server@latest --port 8080 --bind-address 127.0.0.1`):
    // kubernetes: {url: 'http://localhost:8080/mcp'}
  }
});

try {
  const tools = await adapter.listTools();
  console.log(`Loaded ${tools.length} tools: ${tools.map(tool => tool.name).join(', ')}`);
  if (process.argv.includes('--assistant')) {
    const agent = createAgent({
      model: new ChatOllama({model: 'qwen3:4b-instruct'}),
      tools
    });
    const listPods = await agent.invoke({
      messages: [{
        role: 'user',
        content: 'List all pods in my cluster and output their name and namespace as a Markdown table'
      }]
    });
    console.log(listPods.messages.at(-1).text);
  }
} finally {
  await adapter.close();
}
