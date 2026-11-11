// Test end-to-end del servidor de signaling con 2 clientes simulados.
// Verifica: conexión, ruteo de mensaje, entrega al destinatario.

const WebSocket = require('ws');

const URL = process.env.SIGNAL_URL || 'ws://localhost:8080';
const USER_A = 'test-user-AAAA';
const USER_B = 'test-user-BBBB';
const PAYLOAD = 'sdp:offer:test-payload-' + Date.now();

let passed = false;

function connect(userId) {
  return new Promise((resolve, reject) => {
    const ws = new WebSocket(`${URL}?userId=${userId}`);
    ws.on('open', () => resolve(ws));
    ws.on('error', reject);
    setTimeout(() => reject(new Error(`timeout conectando ${userId}`)), 3000);
  });
}

(async () => {
  console.log(`Test contra ${URL}`);
  console.log(`  User A: ${USER_A}`);
  console.log(`  User B: ${USER_B}`);

  const wsA = await connect(USER_A);
  console.log('[1/4] A conectado');

  const wsB = await connect(USER_B);
  console.log('[2/4] B conectado');

  // B escucha lo que reciba
  const received = new Promise((resolve, reject) => {
    wsB.on('message', (data) => {
      try {
        const json = JSON.parse(data.toString());
        console.log(`[3/4] B recibió: from=${json.from} payload="${json.payload}"`);
        if (json.from === USER_A && json.payload === PAYLOAD) {
          resolve(json);
        } else {
          reject(new Error(`payload inesperado`));
        }
      } catch (e) { reject(e); }
    });
    setTimeout(() => reject(new Error('timeout esperando mensaje en B')), 3000);
  });

  // A envía a B
  const msg = JSON.stringify({ to: USER_B, from: USER_A, payload: PAYLOAD });
  wsA.send(msg);
  console.log('[3/4] A envió señal a B');

  try {
    await received;
    console.log('[4/4] PASS — servidor ruteó correctamente');
    passed = true;
  } catch (e) {
    console.error('FAIL:', e.message);
  } finally {
    wsA.close();
    wsB.close();
    setTimeout(() => process.exit(passed ? 0 : 1), 200);
  }
})().catch((e) => {
  console.error('FAIL setup:', e.message);
  process.exit(1);
});
