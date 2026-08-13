// signal-server.js
const WebSocket = require('ws');
const http = require('http');
const url = require('url');

const PORT = process.env.PORT || 8080;
const clients = new Map(); // userId -> Set<WebSocket>

const server = http.createServer((req, res) => {
  res.writeHead(200);
  res.end('MALLA Signal Server');
});

const wss = new WebSocket.Server({ server });

wss.on('connection', (socket, req) => {
  const query = url.parse(req.url, true).query;
  const userId = query.userId || 'anonymous';
  socket.userId = userId;

  if (!clients.has(userId)) {
    clients.set(userId, new Set());
  }
  clients.get(userId).add(socket);
  console.log(`[+] ${userId} conectado`);

  socket.on('message', (data) => {
    try {
      const message = JSON.parse(data.toString());
      const targetClients = clients.get(message.to) || new Set();
      const payload = JSON.stringify({
        from: message.from,
        payload: message.payload,
      });
      for (const client of targetClients) {
        if (client.readyState === WebSocket.OPEN) {
          client.send(payload);
        }
      }
    } catch (e) {
      console.error('Error en mensaje:', e);
    }
  });

  socket.on('close', () => {
    const userSockets = clients.get(socket.userId);
    if (userSockets) {
      userSockets.delete(socket);
      if (userSockets.size === 0) {
        clients.delete(socket.userId);
      }
    }
    console.log(`[-] ${socket.userId} desconectado`);
  });
});

server.listen(PORT, () => {
  console.log(`Servidor de señalización MALLA escuchando en puerto ${PORT}`);
});
