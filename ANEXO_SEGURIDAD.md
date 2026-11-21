# ANEXO A — PROTOCOLO DE SEGURIDAD Y VERIFICACIÓN CRIPTOGRÁFICA

Este anexo complementa el prompt maestro v5 de MALLA. Se activa siempre que se trabaje en la Prioridad #1 (Mensajería E2E, 1b-1e) o en el módulo de seguridad (1a).

## A.1 — Modelo de Amenazas (Threat Model)
**Lo que SÍ protegemos:**
- **Sniffing (Espionaje pasivo):** Un atacante en la red local o en internet no puede leer el contenido de los mensajes (AES-GCM).
- **Tampering (Alteración):** Un atacante no puede modificar el mensaje sin que el GCM falle (Auth Tag).
- **Replay (Repetición):** Reenvío de un mensaje antiguo es detectado y bloqueado (BloomFilter + timestamp en MessageReceiver).
- **Sybil/Spam (Inundación):** Mitigado a nivel de red (próximamente Hashcash).

**Lo que NO protegemos aún (Deuda técnica):**
- **MITM (Man-in-the-Middle):** Al no tener X3DH, un atacante podría interceptar las claves públicas en el primer encuentro (1a).
- **Forward Secrecy:** Al usar una sessionKey estática por contacto, si el Keystore se ve comprometido, el historial pasado se puede descifrar (1a con Double Ratchet).
- **Compromiso físico:** Si el celular está desbloqueado y en manos ajenas, la app no tiene un "modo pánico" que destruya la DB local.

## A.2 — Ejes de Auditoría Criptográfica (Extiende ROL 4)
Cada vez que se toque el módulo de criptografía, se evalúa contra estos 6 ejes:
1. **Confidencialidad:** ¿Viaja algo en texto plano por el cable/BLE/Air? (Debe ser 0%).
2. **Integridad:** ¿El cifrado es autenticado? (GCM es obligatorio, CBC no se permite).
3. **Autenticidad:** ¿Sabemos con certeza quién envía el mensaje? (Validación de firma Ed25519/ECDSA).
4. **Forward Secrecy:** ¿Cambia la clave de sesión tras cada N mensajes? (Actualmente: NO).
5. **Key Management:** ¿Las claves privadas viven en el Android Keystore? (SÍ, vía IdentityManager).
6. **Zero-Trust en Red:** ¿Confiamos ciegamente en el servidor de signaling o en los peers intermedios? (Debe ser NO).

## A.3 — Protocolo de Verificación de Cifrado (5 Niveles)
Para declarar que un cambio de seguridad está "terminado", debe pasar:
- **Nivel 1 (Estático):** Revisión de código (grep de plaintext, toByteArray, String() en flujos de red).
- **Nivel 2 (Unit Test):** Tests de CryptoEngine que verifiquen que el mismo texto con distintas semillas produce distintos ciphertexts.
- **Nivel 3 (Logcat):** En adb logcat, el content de un mensaje debe aparecer como Base64/Hex ilegible. Si aparece "Hola mundo", es un fallo crítico de seguridad.
- **Nivel 4 (Wire-level):** Inspección de tráfico (PCAP) en el socket TCP/WebSocket para confirmar que no viaja JSON en claro.
- **Nivel 5 (Device E2E):** Envío de mensaje entre los dos celulares con un proxy MITM (ej. Charles Proxy) intermedio. El proxy no debe poder leer el mensaje.

## A.4 — Criterios de Aceptación (Definition of Done)
- **1c (ACK Real):** El mensaje debe pasar de SENT a DELIVERED en Room tan pronto como el ACK criptográfico llegue.
- **1a (Double Ratchet + X3DH):** El handshake debe usar claves efímeras (X3DH). El historial pasado debe ser indescifrable si se compromete la clave actual (Forward Secrecy real).

## A.5 — Plantilla de "Entrada de Bitácora de Seguridad"
(A usar en R14 cada vez que se toque criptografía)
── ENTRADA — [FECHA] (SEGURIDAD: [TEMA]) ──
Cambio criptográfico: [Qué se modificó]
Ejes afectados: [Confidencialidad / Integridad / Auth / FS]
Nivel de verificación alcanzado: [1-5]
Vectores de ataque mitigados: [Sniffing / MITM / Replay]
Deuda restante de seguridad: [Qué sigue vulnerable]
