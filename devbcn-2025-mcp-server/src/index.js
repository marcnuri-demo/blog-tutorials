import express from 'express';
import {McpServer, ResourceTemplate} from '@modelcontextprotocol/sdk/server/mcp.js';
import {StdioServerTransport} from '@modelcontextprotocol/sdk/server/stdio.js';
import {StreamableHTTPServerTransport} from '@modelcontextprotocol/sdk/server/streamableHttp.js';

const SERVER_NAME = 'devbcn-2025-mcp-server';
const RESOURCE_PREFIX = 'devbcn://2025/';
const HTTP_PORT = 3000; // Set to 0 to use a random port

const SESSIONIZE_ID = 'kdiixcgx';

// Create an MCP server
const server = new McpServer({
  name: SERVER_NAME,
  version: '1.0.0'
});

server.registerResource(
  'devbcn-2025-speakers',
  `${RESOURCE_PREFIX}speakers`,
  {
    title: 'DevBcn 2025 Speakers',
    description: 'List of speakers for DevBcn (Formerly JBCNConf) 2025',
  },
  async (uri) => ({
    contents: [{
      uri: uri.href,
      text: await fetch(` https://sessionize.com/api/v2/${SESSIONIZE_ID}/view/Speakers`).then(res => res.text()),
    }]
  })
);

server.registerResource(
  'devbcn-2025-sessions',
  `${RESOURCE_PREFIX}sessions`,
  {
    title: 'DevBcn 2025 Sessions',
    description: 'List of sessions for DevBcn (Formerly JBCNConf) 2025',
  },
  async (uri) => ({
    contents: [{
      uri: uri.href,
      text: await fetch(` https://sessionize.com/api/v2/${SESSIONIZE_ID}/view/Sessions`).then(res => res.text()),
    }]
  })
);

server.registerResource(
  'devbcn-2025-schedule',
  `${RESOURCE_PREFIX}schedule`,
  {
    title: 'DevBcn 2025 Schedule',
    description: 'Schedule for DevBcn (Formerly JBCNConf) 2025',
  },
  async (uri) => ({
    contents: [{
      uri: uri.href,
      text: await fetch(` https://sessionize.com/api/v2/${SESSIONIZE_ID}/view/GridSmart`).then(res => res.text()),
    }]
  })
);

try {
  // HTTP
  const expressApp = express();
  expressApp.use(express.json());
  expressApp.post('/mcp', async (req, res) => {
    const httpTransport = new StreamableHTTPServerTransport({
      sessionIdGenerator: undefined
    });
    res.on('close', () => {
      httpTransport.close();
    });
    await server.connect(httpTransport);
    await httpTransport.handleRequest(req, res, req.body);
  });
  const expressServer = expressApp.listen(HTTP_PORT, () => {
    const address = expressServer.address();
    console.log(`DevBCN 2025 MCP HTTP Server is listening on http://localhost:${address.port}/mcp`);
  });

  // STDIO
  const stdioTransport = new StdioServerTransport();
  await server.connect(stdioTransport);
  console.log('DevBCN 2025 MCP Server is running!');
} catch (error) {
  console.error('Error starting MCP server:', error);
  process.exit(1);
}
