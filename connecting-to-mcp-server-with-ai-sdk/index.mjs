#!/usr/bin/env node
import {generateText, stepCountIs} from 'ai';
import {createMCPClient} from '@ai-sdk/mcp';
import {StdioClientTransport} from '@modelcontextprotocol/sdk/client/stdio.js';
import {createGoogleGenerativeAI} from '@ai-sdk/google';

const npx = process.platform === 'win32' ? 'npx.cmd' : 'npx';

const initStdioClient = async () => {
  const transport = new StdioClientTransport({
    command: npx,
    args: ['-y', 'kubernetes-mcp-server@latest']
  });
  return createMCPClient({name: 'blog.marcnuri.com', transport});
};

const initHttpClient = async () => {
  return createMCPClient({
    name: 'blog.marcnuri.com',
    transport: {
      type: 'http',
      url: 'http://localhost:8080/mcp'
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
    const model = google('gemini-2.5-flash');
    const listPods = await generateText({
      model,
      tools,
      stopWhen: stepCountIs(10),
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
