#!/usr/bin/env node
import {createAgent} from 'langchain';
import {ChatGoogleGenerativeAI} from '@langchain/google-genai';
import {loadMcpTools} from '@langchain/mcp-adapters';
import {Client} from '@modelcontextprotocol/sdk/client/index.js';
import {StreamableHTTPClientTransport} from '@modelcontextprotocol/sdk/client/streamableHttp.js';
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

const initHttpClient = async () => {
  const httpClient = new Client({
    name: 'blog.marcnuri.com'
  });
  const transport = new StreamableHTTPClientTransport(new URL('http://localhost:8080/mcp'));
  await httpClient.connect(transport);
  return httpClient;
};

const assistant = async () => {
  console.log('Starting kubernetes-mcp-server in STDIO mode');
  const stdioClient = await initStdioClient();
  console.log('Available tools:');
  const toolList = await stdioClient.listTools();
  toolList.tools.forEach(tool => console.log(` - ${tool.name}: ${tool.description}`));
  if (process.argv.includes('--assistant')) {
    const model = new ChatGoogleGenerativeAI({
      apiKey: process.env['GOOGLE_API_KEY'],
      model: 'gemini-2.5-flash',
    });
    const tools = await loadMcpTools('kubernetes-mcp-server', stdioClient);
    const agent = createAgent({
      model,
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
