#!/usr/bin/env node
import {createReactAgent} from '@langchain/langgraph/prebuilt';
import {ChatOpenAI} from '@langchain/openai';
import {loadMcpTools} from '@langchain/mcp-adapters';
import {Client} from '@modelcontextprotocol/sdk/client/index.js';
import {SSEClientTransport} from '@modelcontextprotocol/sdk/client/sse.js';
import {StdioClientTransport} from '@modelcontextprotocol/sdk/client/stdio.js';

const npx = process.platform === 'win32' ? 'npx.cmd' : 'npx';

const initStdioClient = async () => {
  const stdioClient = new Client({
    name: 'blog.marcnuri.com'
  });
  const transport = new StdioClientTransport({
    command: npx,
    args: ['-y', 'kubernetes-mcp-server@latest']
  });
  await stdioClient.connect(transport);
  return stdioClient;
};

const initSseClient = async () => {
  const sseClient = new Client({
    name: 'blog.marcnuri.com'
  });
  const transport = new SSEClientTransport(new URL('http://localhost:8080/sse'));
  await sseClient.connect(transport);
  return sseClient;
};

const assistant = async () => {
  console.log('Starting kubernetes-mcp-server in STDIO mode');
  const stdioClient = await initStdioClient();
  console.log('Available tools:');
  const toolList = await stdioClient.listTools();
  toolList.tools.forEach(tool => console.log(` - ${tool.name}: ${tool.description}`));
  if (process.argv.includes('--assistant')) {
    const model = new ChatOpenAI({
      configuration: {
        apiKey: process.env['GITHUB_TOKEN'],
        baseURL: 'https://models.inference.ai.azure.com',
      },
      model: 'gpt-4o-mini',
    });
    const tools = await loadMcpTools('kubernetes-mcp-server', stdioClient);
    const agent = createReactAgent({
      llm: model,
      tools,
    });
    const listPods = await agent.invoke({
      messages:[{
        role: 'user',
        content: 'List all pods in my cluster and output as markdown table'
      }]
    });
    console.log(listPods.messages.slice(-1)[0].content);
  }
  await stdioClient.close();
};

assistant()
  .then(() => {
    console.log('done');
  })
  .catch(err => {
    console.error('Error:', err);
  });
