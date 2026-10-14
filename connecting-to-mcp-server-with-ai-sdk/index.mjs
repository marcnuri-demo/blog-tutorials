#!/usr/bin/env node
import {generateText, isStepCount} from 'ai';
import {createMCPClient} from '@ai-sdk/mcp';
import {StdioClientTransport} from '@modelcontextprotocol/client/stdio';
import {ollama} from 'ollama-ai-provider-v2';

const initStdioClient = async () => {
  const transport = new StdioClientTransport({
    command: 'npx',
    args: ['-y', 'kubernetes-mcp-server@latest']
  });
  return createMCPClient({clientName: 'blog.marcnuri.com', transport});
};

const initHttpClient = async () => {
  return createMCPClient({
    clientName: 'blog.marcnuri.com',
    transport: {
      type: 'http',
      url: 'http://localhost:8080/mcp'
    }
  });
};

const assistant = async () => {
  const useHttp = process.argv.includes('--http');
  console.log(`Connecting to kubernetes-mcp-server (${useHttp ? 'Streamable HTTP' : 'STDIO'})`);
  const mcpClient = useHttp ? await initHttpClient() : await initStdioClient();
  try {
    const tools = await mcpClient.tools();
    console.log('Available tools:');
    Object.entries(tools).forEach(([name, tool]) => console.log(` - ${name}: ${tool.description}`));
    if (process.argv.includes('--assistant')) {
      const model = ollama('qwen3:4b-instruct');
      const result = await generateText({
        model,
        tools,
        stopWhen: isStepCount(10),
        messages: [{
          role: 'user',
          content: 'List all pods in my cluster and output their name and namespace as a Markdown table'
        }]
      });
      console.log(result.text);
    }
  } finally {
    await mcpClient.close();
  }
};

assistant().catch(err => {
  console.error('Error:', err);
  process.exitCode = 1;
});
