#!/usr/bin/env node
import {
  generateText,
  experimental_createMCPClient as createMcpClient
} from 'ai';
import {
  Experimental_StdioMCPTransport as StdioClientTransport
} from 'ai/mcp-stdio';
import {createOpenAICompatible} from '@ai-sdk/openai-compatible';

const npx = process.platform === 'win32' ? 'npx.cmd' : 'npx';

const assistant = async () => {
  console.log('🔌 Starting kubernetes-mcp-server in STDIO mode');
  // https://github.com/manusa/kubernetes-mcp-server
  const transport = new StdioClientTransport({
    command: npx,
    args: ['-y', 'kubernetes-mcp-server@latest']
  });
  const stdioClient = await createMcpClient({name: 'blog.marcnuri.com', transport});
  try {
    // Model running locally with Podman Desktop AI Lab
    const localGranite = createOpenAICompatible({
      baseURL: 'http://localhost:3000/'
    });
    const model = localGranite('ibm-granite/granite-3.2-8b-instruct');
    const tools = await stdioClient.tools();
    // const systemPrompt = 'You are a helpful AI assistant.';
    const systemPrompt =
      'Knowledge Cutoff Date: April 2024.\n' +
      `Today's Date: ${new Date().toISOString().split('T')[0]}.\n` +
      'You are Granite, developed by IBM. You are a helpful AI assistant with access to the tools listed next. ' +
      "When a tool is required to answer the user's query, respond with `<tool_call>` followed by a JSON object of the tool used. " +
      'For example: `<tool_call> {"name":"function_name","arguments":{"arg1":"value"}} </tool_call>:' +
      'The user will respond with the output of the tool execution response so you can continue with the rest of the initial user prompt (continue).\n' +
      'If a tool does not exist in the provided list of tools, notify the user that you do not have the ability to fulfill the request.';
    console.log('⚡ Generating text...');
    const result = await generateText({
      model,
      tools,
      system: systemPrompt,
      temperature: 0,
      maxSteps: 2,
      messages: [{
        role: 'user',
        content: 'List my running Pods in all namespaces, format output as Markdown table'
      }]
    });
    for (const step of result.steps) {
      for (const toolCall of step.toolCalls) {
        console.log(`⚙️ Tool call: ${toolCall.toolName}`);
      }
    }
    console.log(result.text);
  } catch (err) {
    await stdioClient.close();
    throw err;
  }
};

assistant()
  .then(() => {
    process.exit(0);
  })
  .catch(err => {
    console.error('Error:', err);
    process.exit(1);
  });
