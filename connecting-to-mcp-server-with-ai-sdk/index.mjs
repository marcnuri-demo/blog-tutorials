#!/usr/bin/env node
import {
  generateText,
  experimental_createMCPClient as createMcpClient
} from 'ai';
import {
  Experimental_StdioMCPTransport as StdioClientTransport
} from 'ai/mcp-stdio';
import {createGoogleGenerativeAI} from '@ai-sdk/google';

const npx = process.platform === 'win32' ? 'npx.cmd' : 'npx';

const initStdioClient = async () => {
  const transport = new StdioClientTransport({
    command: npx,
    args: ['-y', 'kubernetes-mcp-server@latest']
  });
  return createMcpClient({name: 'blog.marcnuri.com', transport});
};

const initSseClient = async () => {
  return createMcpClient({
    name: 'blog.marcnuri.com',
    transport: {
      type: 'sse',
      url: `http://localhost:8080/sse`
    }
  });
};

const assistant = async () => {
  console.log('Starting kubernetes-mcp-server in STDIO mode');
  const stdioClient = await initStdioClient();
  console.log('Available tools:');
  const tools = await stdioClient.tools();
  Object.entries(tools).forEach(([name, tool]) => console.log(` - ${name}: ${tool.description}`));
  if (process.argv.includes('--assistant')) {
    const google = createGoogleGenerativeAI({
      apiKey: process.env['GOOGLE_API_KEY']
    });
    const model = google('gemini-2.0-flash');
    const listPods = await generateText({
      model,
      tools,
      maxSteps: 10,
      messages: [{
        role: 'user',
        content: 'List all pods in my cluster and output as markdown table'
      }]
    });
    console.log(listPods.text);
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
