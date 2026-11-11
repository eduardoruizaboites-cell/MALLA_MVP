// Servidor de signaling mínimo para MALLA MVP.
// Protocolo:
//   Cliente conecta a: ws://host:PORT?userId=XXXX
//   Cliente envía:     {"to": "USER_ID", "from": "USER_ID", "payload": "sdp:..."}
//   Servidor retransmite al destinatario como: {"from": "USER_ID", "payload": "sdp:..."}
// Es stateless (no persiste), fire-and-forget (si el destino no está, se descarta).
// No requiere autenticación en MVP (la autenticación real viene con firma de payload).

const { WebSocketServer } = require('ws');

const PORT = process.env.PORT || 8080;
const wss = new WebSocketServer({ port: PORT });

// Map<userId, WebSocket>
const clients = new Map();

function log(...args) {
  console.log(new Date().toISOString(), ...args);
}

wss.on('connection', (ws, req) => {
  const url = new URL(req.url, `http://${req.headers.host}`);
  const userId = url.searchParams.get('userId');

  if (!userId) {
    log('[REJECT] conexión sin userId');
    ws.close(1008, 'userId required');
    return;
  }

  // Si ya hay una conexión con ese userId, cerramos la vieja (reconnect normal)
  const existing = clients.get(userId);
  if (existing && existing.readyState === 1) {
    log(`[DUP] userId=${userId} reconectado, cerrando socket viejo`);
    try { existing.close(1000, 'replaced'); } catch (_) {}
  }

  clients.set(userId, ws);
  log(`[OPEN] userId=${userId} (total=${clients.size})`);

  ws.on('message', (data) => {
    let json;
    try { json = JSON.parse(data.toString()); } catch (e) {
      log(`[ERR] payload no es JSON de userId=${userId}: ${e.message}`);
      return;
    }

    const to = json.to;
    const from = json.from || userId; // fallback al userId de la conexión
    const payload = json.payload;

    if (!to || payload === undefined) {
      log(`[ERR] señal sin 'to' o 'payload' de userId=${userId}`);
      return;
    }

    const target = clients.get(to);
    if (!target || target.readyState !== 1) {
      log(`[DROP] userId=${userId} -> userId=${to} (destino no conectado)`);
      return;
    }

    const outbound = JSON.stringify({ from, payload });
    try {
      target.send(outbound);
      log(`[RELAY] ${from} -> ${to} (payload ${payload.length} chars)`);
    } catch (e) {
      log(`[ERR] fallo relaying ${from} -> ${to}: ${e.message}`);
    }
  });

  ws.on('close', () => {
    clients.delete(userId);
    log(`[CLOSE] userId=${userId} (total=${clients.size})`);
  });

  ws.on('error', (err) => {
    log(`[WS_ERR] userId=${userId}: ${err.message}`);
  });
});

// Heartbeat: ping a todos cada 25s, cerrar los que no responden
setInterval(() => {
  wss.clients.forEach((ws) => {
    if (ws.isAlive === false) return ws.terminate();
    ws.isAlive = false;
    try { ws.ping(); } catch (_) {}
  });
}, 25000);

wss.on('connection', (ws) => {
  ws.isAlive = true;
  ws.on('pong', () => { ws.isAlive = true; });
});

log(`Servidor de signaling MALLA escuchando en ws://0.0.0.0:${PORT}`);
